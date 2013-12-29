package de.mklinger.commons.exec;

import java.util.concurrent.atomic.AtomicReference;

public abstract class ErrorHandlingRunnable implements Runnable, ErrorHandler {
	private final AtomicReference<Throwable> error = new AtomicReference<>();

	@Override
	public Throwable getError() {
		return error.get();
	}

	@Override
	public void run() {
		try {
			doRun();
		} catch (final Throwable e) {
			error.set(e);
		}
	}

	protected abstract void doRun() throws Exception;
}
