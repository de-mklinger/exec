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

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marc Klinger - mklinger[at]mklinger[dot]de
 */
public class PipeRunnable extends ErrorHandlingRunnable {
	private static final Logger LOG = LoggerFactory.getLogger(PipeRunnable.class);
	private final OutputStream out;
	private final InputStream in;
	private final AtomicBoolean running = new AtomicBoolean();

	public PipeRunnable(final InputStream in, final OutputStream out) {
		this.in = in;
		this.out = out;
	}

	@Override
	protected void doRun() throws IOException {
		running.set(true);
		try {
			final int copied = IOUtils.copy(in, out);
			LOG.debug("Copied {} bytes", copied);
		} finally {
			running.set(false);
		}
	}

	public boolean isRunning() {
		return running.get();
	}

	public void waitFor(final long timeoutMillis) throws CommandLineException, InterruptedException {
		final long start = System.currentTimeMillis();
		while (isRunning()) {
			if (start + timeoutMillis < System.currentTimeMillis()) {
				throw new CommandLineException("Timout waiting for pipe after " + timeoutMillis + " ms");
			}
			Thread.sleep(1);
		}
	}
}
