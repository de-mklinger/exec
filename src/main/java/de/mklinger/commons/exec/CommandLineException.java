package de.mklinger.commons.exec;

/**
 * @author Marc Klinger - mklinger[at]mklinger[dot]de
 */
public class CommandLineException extends Exception {
	private static final long serialVersionUID = 1L;

	public CommandLineException() {
	}

	public CommandLineException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public CommandLineException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public CommandLineException(final String message) {
		super(message);
	}

	public CommandLineException(final Throwable cause) {
		super(cause);
	}
}
