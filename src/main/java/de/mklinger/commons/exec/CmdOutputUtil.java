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
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;

import de.mklinger.commons.exec.internal.CmdBuilderBase;
import de.mklinger.commons.exec.io.SynchronizedOutputStream;
import de.mklinger.commons.exec.io.TeeOutputStream;
import de.mklinger.commons.exec.io.UnsynchronizedStringWriter;
import de.mklinger.commons.exec.io.WriterOutputStream;

/**
 * Utility class to execute a {@link CmdBuilder} or {@link CmdSettings} for output
 * on stdout and/or stderr.
 *
 * @author Marc Klinger - mklinger[at]mklinger[dot]de
 */
public class CmdOutputUtil {
	/**
	 * Execute a Cmd for the given builder and return its stdout output as a String
	 * using the default charset of this Java virtual machine.
	 * <p>
	 * The given builder will not be manipulated and can be re-used by the caller.
	 * </p>
	 * <p>
	 * In case of an error, the captured output is also included in the exception
	 * thrown.
	 * </p>
	 */
	public static String executeForStdout(final CmdBuilderBase<?> cmdBuilder) throws CmdException {
		return executeForStdout(cmdBuilder.toCmdSettings());
	}

	/**
	 * Execute a Cmd for the given settings and return its stdout output as a String
	 * using the default charset of this Java virtual machine.
	 * <p>
	 * The given settings will not be manipulated and can be re-used by the caller.
	 * </p>
	 * <p>
	 * In case of an error, the captured output is also included in the exception
	 * thrown.
	 * </p>
	 */
	public static String executeForStdout(final CmdSettings cmdSettings) throws CmdException {
		final CmdSettings cs = new CmdSettings(cmdSettings);
		final UnsynchronizedStringWriter sw = new UnsynchronizedStringWriter();
		final OutputStream wout = new SynchronizedOutputStream(new WriterOutputStream(sw, Charset.defaultCharset()));

		if (cs.getStdout() == null) {
			cs.setStdout(wout);
		} else {
			cs.setStdout(new TeeOutputStream(cs.getStdout(), wout));
		}

		cs.freeze();
		try {
			new Cmd(cs).execute();
		} catch (final CmdException e) {
			throw new CmdOutputException(e, getOutput(sw, wout), true, false);
		}

		return getOutput(sw, wout);
	}

	/**
	 * Execute a Cmd for the given builder and return its stderr output as a String
	 * using the default charset of this Java virtual machine.
	 * <p>
	 * The given builder will not be manipulated and can be re-used by the caller.
	 * </p>
	 * <p>
	 * In case of an error, the captured output is also included in the exception
	 * thrown.
	 * </p>
	 */
	public static String executeForStderr(final CmdBuilderBase<?> cmdBuilder) throws CmdException {
		return executeForStderr(cmdBuilder.toCmdSettings());
	}

	/**
	 * Execute a Cmd for the given settings and return its stderr output as a String
	 * using the default charset of this Java virtual machine.
	 * <p>
	 * The given settings will not be manipulated and can be re-used by the caller.
	 * </p>
	 * <p>
	 * In case of an error, the captured output is also included in the exception
	 * thrown.
	 * </p>
	 */
	public static String executeForStderr(final CmdSettings cmdSettings) throws CmdException {
		if (cmdSettings.isRedirectErrorStream()) {
			throw new IllegalArgumentException("Error stream is redirected");
		}

		final CmdSettings cs = new CmdSettings(cmdSettings);
		final UnsynchronizedStringWriter sw = new UnsynchronizedStringWriter();
		final OutputStream wout = new SynchronizedOutputStream(new WriterOutputStream(sw, Charset.defaultCharset()));

		if (cs.getStderr() == null) {
			cs.setStderr(wout);
		} else {
			cs.setStderr(new TeeOutputStream(cs.getStderr(), wout));
		}

		cs.freeze();
		try {
			new Cmd(cs).execute();
		} catch (final CmdException e) {
			throw new CmdOutputException(e, getOutput(sw, wout), false, true);
		}

		return getOutput(sw, wout);
	}

	/**
	 * Execute a Cmd for the given builder and return its stdout and stderr output
	 * as a String using the default charset of this Java virtual machine.
	 * <p>
	 * The given settings will not be manipulated and can be re-used by the caller.
	 * </p>
	 * <p>
	 * In case of an error, the captured output is also included in the exception
	 * thrown.
	 * </p>
	 */
	public static String executeForOutput(final CmdBuilderBase<?> cmdBuilder) throws CmdException {
		return executeForOutput(cmdBuilder.toCmdSettings());
	}

	/**
	 * Execute a Cmd for the given settings and return its stdout and stderr output
	 * as a String using the default charset of this Java virtual machine.
	 * <p>
	 * The given settings will not be manipulated and can be re-used by the caller.
	 * </p>
	 * <p>
	 * In case of an error, the captured output is also included in the exception
	 * thrown.
	 * </p>
	 */
	public static String executeForOutput(final CmdSettings cmdSettings) throws CmdException {
		if (cmdSettings.isRedirectErrorStream()) {
			return executeForStdout(cmdSettings);
		}

		final CmdSettings cs = new CmdSettings(cmdSettings);
		final UnsynchronizedStringWriter sw = new UnsynchronizedStringWriter();
		final OutputStream wout = new SynchronizedOutputStream(new WriterOutputStream(sw, Charset.defaultCharset()));

		if (cs.getStderr() == null) {
			cs.setStderr(wout);
		} else {
			cs.setStderr(new TeeOutputStream(cs.getStderr(), wout));
		}
		if (cs.getStdout() == null) {
			cs.setStdout(wout);
		} else {
			cs.setStdout(new TeeOutputStream(cs.getStdout(), wout));
		}

		cs.freeze();
		try {
			new Cmd(cs).execute();
		} catch (final CmdException e) {
			throw new CmdOutputException(e, getOutput(sw, wout), true, true);
		}

		return getOutput(sw, wout);
	}

	private static String getOutput(final UnsynchronizedStringWriter sw, final OutputStream wout) {
		try {
			wout.close();
		} catch (final IOException e) {
			// should not happen with StringWriter and WriterOutputStream
			throw new UncheckedIOException(e);
		}
		return sw.toString();
	}
}
