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
package de.mklinger.commons.exec.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Marc Klinger - mklinger[at]mklinger[dot]de
 */
public class SynchronizedOutputStream extends ProxyOutputStream {
	public SynchronizedOutputStream(final OutputStream delegate) {
		super(delegate);
	}

	@Override
	public synchronized void write(final int b) throws IOException {
		super.write(b);
	}

	@Override
	public synchronized void write(final byte[] b, final int off, final int len) throws IOException {
		super.write(b, off, len);
	}

	@Override
	public synchronized void write(final byte[] b) throws IOException {
		super.write(b);
	}

	@Override
	public synchronized void close() throws IOException {
		super.close();
	}

	@Override
	public synchronized void flush() throws IOException {
		super.flush();
	}
}