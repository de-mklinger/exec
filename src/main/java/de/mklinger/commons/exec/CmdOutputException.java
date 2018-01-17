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
public class CmdOutputException extends CmdException {
	private static final long serialVersionUID = 1L;

	private final String output;
	private final boolean stdoutIncluded;
	private final boolean stderrIncluded;

	public CmdOutputException(final CmdException cause, final String output, final boolean stdoutIncluded, final boolean stderrIncluded) {
		super(cause);
		this.output = output;
		this.stdoutIncluded = stdoutIncluded;
		this.stderrIncluded = stderrIncluded;
	}

	public String getOutput() {
		return output;
	}

	public boolean isStdoutIncluded() {
		return stdoutIncluded;
	}

	public boolean isStderrIncluded() {
		return stderrIncluded;
	}

	@Override
	public String getMessage() {
		final String message = super.getMessage();
		final StringBuilder sb = new StringBuilder(message.length() + output.length() + 25);
		sb.append(message);
		sb.append("\nOutput (");
		if (stdoutIncluded) {
			sb.append("stdout");
		}
		if (stderrIncluded) {
			if (stdoutIncluded) {
				sb.append("+");
			}
			sb.append("stderr");
		}
		sb.append("):\n");
		sb.append(output);
		return sb.toString();
	}
}
