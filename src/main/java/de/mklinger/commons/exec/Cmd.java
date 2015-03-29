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
 * @author Marc Klinger - mklinger[at]mklinger[dot]de - klingerm
 */
public class Cmd {
	private static final Logger LOG = LoggerFactory.getLogger(Cmd.class);

	private static final ExecutorProvider DEFAULT_EXECUTOR_PROVIDER = new DefaultExecutorProvider();

	private static final long PIPE_RUNNABLE_TIMEOUT = 60000;

	private static final Set<Cmd> destroyOnShutdownCmds = new HashSet<>();
	static {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				synchronized (destroyOnShutdownCmds) {
					for (final Iterator<Cmd> iterator = destroyOnShutdownCmds.iterator(); iterator.hasNext();) {
						final Cmd cmd = iterator.next();
						cmd.destroyProcess();
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
			pb.environment().putAll(cmdSettings.getEnvironment());
		}

		if (cmdSettings.getStdout() == null) {
			try {
				stdOutNullFile = new NullFile();
			} catch (final IOException e) {
				throw new CommandLineException("Error creating null file", e);
			}
			pb.redirectOutput(Redirect.to(stdOutNullFile.getFile()));
		}
		if (cmdSettings.getStderr() == null) {
			try {
				stdErrNullFile = new NullFile();
			} catch (final IOException e) {
				throw new CommandLineException("Error creating null file", e);
			}
			pb.redirectError(Redirect.to(stdErrNullFile.getFile()));
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
		}
		if (cmdSettings.getStderr() != null) {
			stderrPipe = new PipeRunnable(process.getErrorStream(), cmdSettings.getStderr());
			execute(stderrPipe);
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
				try {
					try {
						exitValue = doWaitFor();
						if (cmdSettings.isDestroyOnShutdown()) {
							synchronized (destroyOnShutdownCmds) {
								destroyOnShutdownCmds.remove(this);
							}
						}
					} catch (final CommandLineException e) {
						if (cmdSettings.isDestroyOnError()) {
							destroy();
						}
						throw e;
					} catch (final InterruptedException e) {
						if (cmdSettings.isDestroyOnError()) {
							destroy();
						}
						throw e;
					} catch (final Exception e) {
						if (cmdSettings.isDestroyOnError()) {
							destroy();
						}
						throw new CommandLineException(e);
					} finally {
						if (stdoutPipe != null) {
							stdoutPipe.waitFor(PIPE_RUNNABLE_TIMEOUT);
							if (stdoutPipe.getError() != null) {
								throw new CommandLineException("Error reading stdout", stdoutPipe.getError());
							}
						}
					}
				} finally {
					if (stderrPipe != null) {
						stderrPipe.waitFor(PIPE_RUNNABLE_TIMEOUT);
						if (stderrPipe.getError() != null) {
							throw new CommandLineException("Error reading stderr", stderrPipe.getError());
						}
					}
				}
			} finally {
				if (pingRunnable != null) {
					pingRunnable.interrupt();
				}
			}
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
			process.destroy();
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
		destroyProcess();
		closeResources();
		if (cmdSettings.isDestroyOnShutdown()) {
			synchronized (destroyOnShutdownCmds) {
				destroyOnShutdownCmds.remove(this);
			}
		}
	}

	private void closeResources() {
		if (stdOutNullFile != null) {
			stdOutNullFile.cleanup();
			stdOutNullFile = null;
		}
		if (stdErrNullFile != null) {
			stdErrNullFile.cleanup();
			stdErrNullFile = null;
		}
	}

	private void destroyProcess() {
		if (process != null) {
			process.destroy();
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
