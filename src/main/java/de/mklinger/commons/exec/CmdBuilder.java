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

import java.io.File;

/**
 * The main entry point for using the Exec library. Fluent API for creating
 * {@link Cmd} instances.
 *
 * @author Marc Klinger - mklinger[at]mklinger[dot]de - klingerm
 */
public class CmdBuilder extends CmdBuilderBase<CmdBuilder> {
	/**
	 * Prepare command execution.
	 *
	 * @param command
	 *            The command to execute on the underlying OS. The given String
	 *            must be available as command on the PATH or must be absolute
	 *            or must be relative to the current working directory of the
	 *            VM.
	 */
	public CmdBuilder(final String command) {
		if (command == null) {
			throw new NullPointerException();
		}
		if (command.isEmpty()) {
			throw new IllegalArgumentException();
		}
		arg(command);
	}

	/**
	 * Prepare command execution.
	 *
	 * @param command
	 *            The command to execute on the underlying OS. The given File
	 *            must be executable on the underlying OS. It must be absolute
	 *            or must be relative to the current working directory of the
	 *            VM.
	 */
	public CmdBuilder(final File command) {
		if (command == null) {
			throw new NullPointerException();
		}
		arg(command.getAbsolutePath());
	}
}
