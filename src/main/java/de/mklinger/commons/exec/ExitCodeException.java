package de.mklinger.commons.exec;

/**
 * @author Marc Klinger - mklinger[at]mklinger[dot]de
 */
public class ExitCodeException extends CommandLineException {

	private static final long serialVersionUID = 1L;

	private final int expectedExitCode;
	private final int exitCode;

	public ExitCodeException(final int expectedExitCode, final int exitCode) {
		this.expectedExitCode = expectedExitCode;
		this.exitCode = exitCode;
	}

	public ExitCodeException(final String message, final int expectedExitCode, final int exitCode) {
		super(message);
		this.expectedExitCode = expectedExitCode;
		this.exitCode = exitCode;
	}

	public int getExpectedExitCode() {
		return expectedExitCode;
	}

	public int getExitCode() {
		return exitCode;
	}
}
