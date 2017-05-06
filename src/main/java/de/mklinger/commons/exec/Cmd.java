/*
 * Copyright 2013-present mklinger GmbH - http://www.mklinger.de
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mklinger.commons.exec;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A command. It may be not be started yet, it may be running right now or it may be terminated.
 * Other parties (i.e. Threads) may be waiting for it to terminate or doing progress.
 * <p>
 * Instances of this class should usually be created using {@link CmdBuilder}.
 * </p>
 *
 * @author Marc Klinger - mklinger[at]mklinger[dot]de - klingerm
 */
public class Cmd {
	private static final Logger LOG = LoggerFactory.getLogger(Cmd.class);

	private static final ExecutorProvider DEFAULT_EXECUTOR_PROVIDER = new DefaultExecutorProvider();

	private static final long PIPE_RUNNABLE_START_TIMEOUT = 1000;
	private static final long PIPE_RUNNABLE_STOP_TIMEOUT = 60000;

	private static final Set<Cmd> destroyOnShutdownCmds = new HashSet<>();
	static {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				synchronized (destroyOnShutdownCmds) {
					for (final Iterator<Cmd> iterator = destroyOnShutdownCmds.iterator(); iterator.hasNext();) {
						final Cmd cmd = iterator.next();
						cmd.destroy();
						iterator.remove();
					}
				}
			}
		});
	}

	private final CmdSettings cmdSettings;
	private NullFile stdOutNullFile = null;
	private NullFile stdErrNullFile = null;
	private PipeRunnable stdoutPipe = null;
	private PipeRunnable stderrPipe = null;
	private PingRunnable pingRunnable;
	private Process process;
	private long startTime;

	public Cmd(final CmdSettings cmdSettings) {
		this.cmdSettings = cmdSettings;
	}

	/**
	 * Start the process and wait for it to exit.
	 */
	public void execute() throws CommandLineException {
		try {
			start();
			waitFor();
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new CommandLineInterruptedException(e);
		} finally {
			close();
		}
	}

	/**
	 * Start the process and return immediately. Cmds started with this
	 * method must be {@link #close() closed} or {@link #destroy() destroyed}
	 * at some point in future.
	 */
	public void start() throws CommandLineException {
		final List<String> command = cmdSettings.getCommand();
		if (command == null || cmdSettings.getCommand().isEmpty()) {
			throw new IllegalArgumentException("Missing command");
		}
		LOG.debug("Executing: {}", command);

		final ProcessBuilder pb = new ProcessBuilder(command);
		if (cmdSettings.getDirectory() != null) {
			pb.directory(cmdSettings.getDirectory());
		}

		if (cmdSettings.getEnvironment() != null) {
			pb.environment().clear();
			pb.environment().putAll(cmdSettings.getEnvironment());
		}

		if (cmdSettings.getStdout() == null) {
			if (cmdSettings.getStdoutFile() != null) {
				pb.redirectOutput(Redirect.to(cmdSettings.getStdoutFile()));
			} else {
				try {
					stdOutNullFile = new NullFile();
				} catch (final IOException e) {
					throw new CommandLineException("Error creating null file", e);
				}
				pb.redirectOutput(Redirect.to(stdOutNullFile.getFile()));
			}
		}

		if (!cmdSettings.isRedirectErrorStream() && cmdSettings.getStderr() == null) {
			if (cmdSettings.getStderrFile() != null) {
				pb.redirectError(Redirect.to(cmdSettings.getStderrFile()));
			} else {
				try {
					stdErrNullFile = new NullFile();
				} catch (final IOException e) {
					throw new CommandLineException("Error creating null file", e);
				}
				pb.redirectError(Redirect.to(stdErrNullFile.getFile()));
			}
		}

		if (cmdSettings.isRedirectErrorStream()) {
			pb.redirectErrorStream(true);
		}

		try {
			process = pb.start();
		} catch (final IOException e) {
			throw new CommandLineException(e);
		}
		startTime = System.currentTimeMillis();
		if (cmdSettings.isDestroyOnShutdown()) {
			synchronized (destroyOnShutdownCmds) {
				destroyOnShutdownCmds.add(this);
			}
		}

		if (cmdSettings.getPingable() != null) {
			pingRunnable = new PingRunnable(cmdSettings.getPingable());
			execute(pingRunnable);
		}

		if (cmdSettings.getStdout() != null) {
			stdoutPipe = new PipeRunnable(process.getInputStream(), cmdSettings.getStdout());
			execute(stdoutPipe);
			stdoutPipe.waitForStart(PIPE_RUNNABLE_START_TIMEOUT);
		}
		if (!cmdSettings.isRedirectErrorStream() && cmdSettings.getStderr() != null) {
			stderrPipe = new PipeRunnable(process.getErrorStream(), cmdSettings.getStderr());
			execute(stderrPipe);
			stderrPipe.waitForStart(PIPE_RUNNABLE_START_TIMEOUT);
		}

		if (cmdSettings.getStdinBytes() != null) {
			try {
				final OutputStream pout = process.getOutputStream();
				pout.write(cmdSettings.getStdinBytes());
				pout.close();
			} catch (final IOException e) {
				throw new CommandLineException("Error writing to stdin", e);
			}
		}
	}

	private void execute(final Runnable runnable) {
		ExecutorProvider executorProvider = cmdSettings.getExecutorProvider();
		if (executorProvider == null) {
			executorProvider = DEFAULT_EXECUTOR_PROVIDER;
		}
		executorProvider.getExecutor().execute(runnable);
	}

	//  public Integer getPid() {
	//      if (process != null && process.getClass().getName().equals("java.lang.UNIXProcess")) {
	//          try {
	//              final Class<?> proc = process.getClass();
	//              final Field field = proc.getDeclaredField("pid");
	//              field.setAccessible(true);
	//              return (Integer) field.get(process);
	//          } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
	//              LOG.warn("Could not get PID for process", e);
	//          }
	//      }
	//      return null;
	//  }

	/**
	 * Wait for an already {@link #start() started} process.
	 * @return The process exit value.
	 */
	public int waitFor() throws CommandLineException, InterruptedException {
		final int exitValue;
		try {
			try {
				Exception throwedException = null;
				try {
					try {
						exitValue = doWaitFor();
						if (cmdSettings.isDestroyOnShutdown()) {
							synchronized (destroyOnShutdownCmds) {
								destroyOnShutdownCmds.remove(this);
							}
						}
					} catch (final Exception e) {
						throwedException = handleExecutionException(throwedException, e);
						throw throwedException;
					} finally {
						stopPipe(stdoutPipe, "stdout", PIPE_RUNNABLE_STOP_TIMEOUT, throwedException);
					}
				} finally {
					stopPipe(stderrPipe, "stderr", PIPE_RUNNABLE_STOP_TIMEOUT, throwedException);
				}
			} finally {
				if (pingRunnable != null) {
					pingRunnable.interrupt();
				}
			}
		} catch (CommandLineException | InterruptedException | RuntimeException e) {
			throw e;
		} catch (final Exception e) {
			throw new CommandLineException(e);
		} finally {
			pingRunnable = null;
			stdoutPipe = null;
			stderrPipe = null;
			process = null;
		}

		if (exitValue != cmdSettings.getExpectedExitValue()) {
			throw new ExitCodeException("Error executing command: " + cmdSettings.getCommand() + ". Exit value: " + exitValue, cmdSettings.getExpectedExitValue(), exitValue);
		}
		return exitValue;
	}

	private void stopPipe(final PipeRunnable pipe, final String name, final long timeout, Exception throwedException) throws Exception {
		try {
			if (pipe != null) {
				pipe.waitForStop(timeout);
				if (pipe.getError() != null) {
					final CommandLineException ex = new CommandLineException("Error reading " + name, pipe.getError());
					if (throwedException != null) {
						throwedException.addSuppressed(ex);
					} else {
						throwedException = ex;
						throw ex;
					}
				}
			}
		} catch (final Exception e) {
			throwedException = handleExecutionException(throwedException, e);
			throw throwedException;
		} finally {
			if (pipe != null) {
				pipe.interrupt();
			}
		}
	}

	private Exception handleExecutionException(final Exception mainException, final Exception newException) {
		final Exception e = withSuppressed(mainException, newException);
		if (cmdSettings.isDestroyOnError()) {
			LOG.debug("Destroying on error", e);
			destroy();
		}
		if (newException instanceof InterruptedException) {
			Thread.currentThread().interrupt();
		}
		return e;
	}

	private Exception withSuppressed(final Exception mainException, final Exception newException) {
		if (mainException == null) {
			return newException;
		} else {
			if (mainException != newException) {
				mainException.addSuppressed(newException);
			}
			return mainException;
		}
	}

	private int doWaitFor() throws InterruptedException, CommandLineException {
		if (cmdSettings.getTimeout() < 1 && !cmdSettings.isDestroyOnError()) {
			return process.waitFor();
		} else {
			while (cmdSettings.getTimeout() < 1 || System.currentTimeMillis() < startTime + cmdSettings.getTimeout()) {
				if (cmdSettings.isDestroyOnError()) {
					checkErrorHandlingRunnables();
				}
				try {
					return process.exitValue();
				} catch (final IllegalThreadStateException e) {
					Thread.sleep(10);
				}
			}
			destroyProcess();
			throw new CommandLineException("Timeout: command execution took longer than " + cmdSettings.getTimeout() + "ms");
		}
	}

	private void checkErrorHandlingRunnables() throws CommandLineException {
		if (stderrPipe != null && stderrPipe.getError() != null) {
			throw new CommandLineException("Error in stderr pipe", stderrPipe.getError());
		}
		if (stdoutPipe != null && stdoutPipe.getError() != null) {
			throw new CommandLineException("Error in stdout pipe", stdoutPipe.getError());
		}
		if (pingRunnable != null && pingRunnable.getError() != null) {
			throw new CommandLineException("Error in ping runnable", pingRunnable.getError());
		}
	}

	public void close() {
		destroy();
	}

	public void destroy() {
		destroy(cmdSettings.isDestroyForcibly());
	}

	public void destroyForcibly() {
		destroy(true);
	}

	private void destroy(final boolean force) {
		Exception throwedException = null;

		try {
			if (force) {
				destroyProcessForcibly();
			} else {
				destroyProcess();
			}
		} catch (final Exception e) {
			throwedException = withSuppressed(throwedException, e);
		}

		try {
			if (pingRunnable != null) {
				pingRunnable.interrupt();
			}
		} catch (final Exception e) {
			throwedException = withSuppressed(throwedException, e);
		} finally {
			pingRunnable = null;
		}

		try {
			if (stdoutPipe != null) {
				stdoutPipe.interrupt();
				stdoutPipe.closeIn();
			}
		} catch (final Exception e) {
			throwedException = withSuppressed(throwedException, e);
		} finally {
			stdoutPipe = null;
		}

		try {
			if (stderrPipe != null) {
				stderrPipe.interrupt();
				stderrPipe.closeIn();
			}
		} catch (final Exception e) {
			throwedException = withSuppressed(throwedException, e);
		} finally {
			stderrPipe = null;
		}

		try {
			if (stdOutNullFile != null) {
				stdOutNullFile.cleanup();
			}
		} catch (final Exception e) {
			throwedException = withSuppressed(throwedException, e);
		} finally {
			stdOutNullFile = null;
		}

		try {
			if (stdErrNullFile != null) {
				stdErrNullFile.cleanup();
			}
		} catch (final Exception e) {
			throwedException = withSuppressed(throwedException, e);
		} finally {
			stdErrNullFile = null;
		}

		try {
			if (cmdSettings.isDestroyOnShutdown()) {
				synchronized (destroyOnShutdownCmds) {
					destroyOnShutdownCmds.remove(this);
				}
			}
		} catch (final Exception e) {
			throwedException = withSuppressed(throwedException, e);
		}

		if (throwedException != null) {
			if (throwedException instanceof RuntimeException) {
				throw (RuntimeException)throwedException;
			} else {
				throw new RuntimeException(throwedException);
			}
		}
	}

	private void destroyProcess() {
		if (process != null) {
			process.destroy();
		}
	}

	private void destroyProcessForcibly() {
		if (process != null) {
			process.destroyForcibly();
		}
	}

	public boolean isExecuting() {
		if (process == null) {
			return false;
		}
		try {
			process.exitValue();
			return false;
		} catch (final IllegalThreadStateException e) {
			return true;
		}
	}

	public int exitValue() {
		if (process == null) {
			throw new IllegalThreadStateException("No process");
		}
		return process.exitValue();
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append("[command=");
		if (cmdSettings.getCommand() != null) {
			boolean first = true;
			for (final String part : cmdSettings.getCommand()) {
				if (!first) {
					sb.append(' ');
				} else {
					first = false;
				}
				sb.append(part);
			}
		}
		return sb.toString();
	}
}
