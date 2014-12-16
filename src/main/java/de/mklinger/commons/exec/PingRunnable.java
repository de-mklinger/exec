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

public class PingRunnable extends ErrorHandlingRunnable {
	private Thread runningThread;
	private final Pingable pingable;

	public PingRunnable(final Pingable pingable) {
		this.pingable = pingable;
	}

	@Override
	public void doRun() {
		runningThread = Thread.currentThread();
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
		runningThread.interrupt();
	}
}
