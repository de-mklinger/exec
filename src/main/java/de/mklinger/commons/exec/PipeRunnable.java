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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mklinger.commons.exec.io.IOUtils;

/**
 * @author Marc Klinger - mklinger[at]mklinger[dot]de
 */
public class PipeRunnable extends ErrorHandlingRunnable {
	private static final Logger LOG = LoggerFactory.getLogger(PipeRunnable.class);
	private final OutputStream out;
	private final InputStream in;
	private final AtomicBoolean running = new AtomicBoolean();
	private final AtomicBoolean started = new AtomicBoolean();
	private volatile Thread runningThread;

	public PipeRunnable(final InputStream in, final OutputStream out) {
		this.in = in;
		this.out = out;
	}

	@Override
	protected void doRun() throws IOException {
		runningThread = Thread.currentThread();
		running.set(true);
		started.set(true);
		try {
			final int copied = IOUtils.copy(in, out);
			LOG.debug("Copied {} bytes", copied);
		} finally {
			running.set(false);
			runningThread = null;
		}
	}

	public boolean isRunning() {
		return running.get();
	}

	public void waitForStart(final long timeoutMillis) throws CmdException {
		final long start = System.currentTimeMillis();
		while (!started.get()) {
			if (start + timeoutMillis < System.currentTimeMillis()) {
				throw new CmdException("Timout waiting for pipe to start after " + timeoutMillis + " ms");
			}
			Thread.yield();
		}
	}

	public void waitForStop(final long timeoutMillis) throws CmdException, InterruptedException {
		final long start = System.currentTimeMillis();
		while (isRunning()) {
			if (start + timeoutMillis < System.currentTimeMillis()) {
				throw new CmdException("Timout waiting for pipe to stop after " + timeoutMillis + " ms");
			}
			Thread.sleep(1);
		}
	}

	public void interrupt() {
		if (runningThread != null) {
			runningThread.interrupt();
		}
	}

	public void closeIn() throws IOException {
		if (in != null) {
			in.close();
		}
	}
}
