package de.mklinger.commons.exec;

public class PingRunnable extends ErrorHandlingRunnable {
	private final Pingable pingable;

	public PingRunnable(final Pingable pingable) {
		this.pingable = pingable;
	}

	@Override
	public void doRun() {
		while (true) {
			if (Thread.currentThread().isInterrupted()) {
				return;
			}
			pingable.ping();
			if (Thread.currentThread().isInterrupted()) {
				return;
			}
			try {
				Thread.sleep(500);
			} catch (final InterruptedException e) {
				// we expect to be interrupted when done.
				Thread.currentThread().interrupt();
				return;
			}
		}
	}

	public void interrupt() {
		Thread.currentThread().interrupt();
	}
}
