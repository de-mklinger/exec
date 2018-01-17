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

import java.io.Writer;

/**
 * @author Marc Klinger - mklinger[at]mklinger[dot]de
 */
public class UnsynchronizedStringWriter extends Writer {
	private final StringBuilder buf;

	public UnsynchronizedStringWriter() {
		buf = new StringBuilder();
	}

	@Override
	public void write(final int c) {
		buf.append((char) c);
	}

	@Override
	public void write(final char cbuf[], final int off, final int len) {
		buf.append(cbuf, off, len);
	}

	@Override
	public void write(final String str) {
		buf.append(str);
	}

	@Override
	public void write(final String str, final int off, final int len)  {
		buf.append(str, off, off + len);
	}

	@Override
	public UnsynchronizedStringWriter append(final CharSequence csq) {
		buf.append(csq);
		return this;
	}

	@Override
	public UnsynchronizedStringWriter append(final CharSequence csq, final int start, final int end) {
		buf.append(csq, start, end);
		return this;
	}

	@Override
	public UnsynchronizedStringWriter append(final char c) {
		buf.append(c);
		return this;
	}

	/**
	 * Return the buffer's current value as a string.
	 */
	@Override
	public String toString() {
		return buf.toString();
	}

	/**
	 * Flushing a {@code UnsynchronizedStringWriter} has no effect.
	 */
	@Override
	public void flush() {
	}

	/**
	 * Closing a {@code UnsynchronizedStringWriter} has no effect.
	 */
	@Override
	public void close() {
	}
}
