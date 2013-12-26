package de.mklinger.commons.exec;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marc Klinger - mklinger[at]mklinger[dot]de
 */
public class CommandLine {
	private static final Logger LOG = LoggerFactory.getLogger(CommandLine.class);
	private static final ExecutorService threadPool = Executors.newCachedThreadPool(new DeamonThreadCmdThreadFactory());

	private final List<String> command;
	private final ProcessBuilder pb;
	private final int expectedExitCode = 0;
	private OutputStream stdout;
	private OutputStream stderr;
	private NullFile stdOutNullFile = null;
	private NullFile stdErrNullFile = null;
	private PipeRunnable stdoutPipe = null;
	private PipeRunnable stderrPipe = null;
	private Process process;
	private byte[] stdinBytes;
	private Map<String, String> environment;
	private long timeout;
	private long startTime;

	public CommandLine(final Object... commandLineParts) {
		command = new ArrayList<>();
		addArguments(command, commandLineParts);
		this.pb = new ProcessBuilder(command);
	}

	public CommandLine stdout(final OutputStream stdout) {
		this.stdout = stdout;
		return this;
	}

	public CommandLine stderr(final OutputStream stderr) {
		this.stderr = stderr;
		return this;
	}

	public CommandLine stdin(final byte[] stdinBytes) {
		this.stdinBytes = stdinBytes;
		return this;
	}

	public CommandLine directory(final File directory) {
		pb.directory(directory);
		return this;
	}

	public CommandLine environment(final String name, final String value) {
		if (environment == null) {
			environment = new HashMap<String, String>();
		}
		environment.put(name, value);
		return this;
	}

	public CommandLine timeout(final long timeout) {
		this.timeout = timeout;
		return this;
	}

	public void execute() throws CommandLineException {
		try {
			start();
			waitFor();
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new CommandLineException("Interrupted");
		} finally {
			close();
		}
	}

	public void start() throws CommandLineException {
		LOG.debug("Executing: {}", command);
		if (stdout == null) {
			try {
				stdOutNullFile = new NullFile();
			} catch (final IOException e) {
				throw new CommandLineException("Error creating null file", e);
			}
			pb.redirectOutput(Redirect.to(stdOutNullFile.getFile()));
		}
		if (stderr == null) {
			try {
				stdErrNullFile = new NullFile();
			} catch (final IOException e) {
				throw new CommandLineException("Error creating null file", e);
			}
			pb.redirectError(Redirect.to(stdErrNullFile.getFile()));
		}

		try {
			process = pb.start();
			startTime = System.currentTimeMillis();
		} catch (final IOException e) {
			throw new CommandLineException(e);
		}

		if (stdout != null) {
			stdoutPipe = new PipeRunnable(process.getInputStream(), stdout);
			threadPool.execute(stdoutPipe);
		}
		if (stderr != null) {
			stderrPipe = new PipeRunnable(process.getErrorStream(), stderr);
			threadPool.execute(stderrPipe);
		}

		if (stdinBytes != null) {
			try {
				final OutputStream pout = process.getOutputStream();
				pout.write(stdinBytes);
				pout.close();
			} catch (final IOException e) {
				throw new CommandLineException("Error writing to stdin", e);
			}
		}
	}

	public int waitFor() throws CommandLineException, InterruptedException {
		final int exitValue;
		try {
			try {
				exitValue = doWaitFor();
			} finally {
				if (stdoutPipe != null) {
					stdoutPipe.waitFor(500);
					if (stdoutPipe.getError() != null) {
						throw new CommandLineException("Error reading stdout", stdoutPipe.getError());
					}
				}
				if (stderrPipe != null) {
					stderrPipe.waitFor(500);
					if (stderrPipe.getError() != null) {
						throw new CommandLineException("Error reading stderr", stderrPipe.getError());
					}
				}
			}
		} finally {
			stdoutPipe = null;
			stderrPipe = null;
			process = null;
		}

		if (exitValue != expectedExitCode) {
			throw new ExitCodeException("Error executing command: " + command + ". Exit value: " + exitValue, expectedExitCode, exitValue);
		}
		return exitValue;
	}

	private int doWaitFor() throws InterruptedException, CommandLineException {
		if (timeout < 1) {
			return process.waitFor();
		} else {
			while (System.currentTimeMillis() < startTime + timeout) {
				try {
					return process.exitValue();
				} catch (final IllegalThreadStateException e) {
					Thread.sleep(10);
				}
			}
			process.destroy();
			throw new CommandLineException("Timeout: command execution took longer than " + timeout + "ms");
		}
	}

	public void close() {
		if (stdOutNullFile != null) {
			stdOutNullFile.cleanup();
			stdOutNullFile = null;
		}
		if (stdErrNullFile != null) {
			stdErrNullFile.cleanup();
			stdErrNullFile = null;
		}
	}

	public void destroy() {
		if (process != null) {
			process.destroy();
		}
	}

	private void addArguments(final List<String> command, final Object... arguments) {
		for (final Object o : arguments) {
			if (o == null) {
				continue;
			}
			if (o.getClass().isArray()) {
				addArguments(command, (Object[])o);
			} else if (Collection.class.isAssignableFrom(o.getClass())) {
				final Collection<?> c = (Collection<?>)o;
				final Object[] array = new Object[c.size()];
				int idx = 0;
				for (final Object object : c) {
					array[idx] = object;
					idx++;
				}
				addArguments(command,  array);
			} else {
				command.add(String.valueOf(o));
			}
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

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append("[command=");
		if (command != null) {
			boolean first = true;
			for (final String part : command) {
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
