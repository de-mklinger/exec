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

/**
 * @author Marc Klinger - mklinger[at]mklinger[dot]de
 */
public class ExitCodeException extends CmdException {

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

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(getClass().getName());
		final String message = getLocalizedMessage();
		if (message != null) {
			sb.append(": ");
			sb.append(message);
		}
		sb.append(" - Expected exit code ");
		sb.append(expectedExitCode);
		sb.append(" but was ");
		sb.append(exitCode);
		return sb.toString();
	}
}
