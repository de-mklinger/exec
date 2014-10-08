/* Copyright 2013, 2014 mklinger GmbH
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
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * @author Marc Klinger - mklinger[at]mklinger[dot]de - klingerm
 */
public abstract class CmdBuilderBase<B extends CmdBuilderBase<B>> {
	private final CmdSettings cmdSettings = new CmdSettings();

	public B stdout(final OutputStream stdout) {
		cmdSettings.setStdout(stdout);
		return getBuilder();
	}

	public B stderr(final OutputStream stderr) {
		cmdSettings.setStderr(stderr);
		return getBuilder();
	}

	public B stdin(final byte[] stdinBytes) {
		cmdSettings.setStdinBytes(stdinBytes);
		return getBuilder();
	}

	public B ping(final Pingable pingable) {
		cmdSettings.setPingable(pingable);
		return getBuilder();
	}

	public B directory(final File directory) {
		cmdSettings.setDirectory(directory);
		return getBuilder();
	}

	public B environment(final String name, final String value) {
		if (cmdSettings.getEnvironment() == null) {
			cmdSettings.setEnvironment(new HashMap<String, String>());
		}
		cmdSettings.getEnvironment().put(name, value);
		return getBuilder();
	}

	public B timeout(final long timeout) {
		cmdSettings.setTimeout(timeout);
		return getBuilder();
	}

	public B destroyOnError(final boolean destroyOnError) {
		cmdSettings.setDestroyOnError(destroyOnError);
		return getBuilder();
	}

	public B destroyOnShutdown(final boolean destroyOnShutdown) {
		cmdSettings.setDestroyOnShutdown(destroyOnShutdown);
		return getBuilder();
	}

	public B args(final Object... arguments) {
		if (cmdSettings.getCommand() == null) {
			cmdSettings.setCommand(new ArrayList<String>());
		}
		addArguments(cmdSettings.getCommand(), arguments);
		return getBuilder();
	}

	public B arg(final Object argument) {
		if (cmdSettings.getCommand() == null) {
			cmdSettings.setCommand(new ArrayList<String>());
		}
		addArguments(cmdSettings.getCommand(), argument);
		return getBuilder();
	}

	private void addArguments(final List<String> command, final Object... arguments) {
		for (final Object o : arguments) {
			if (o == null) {
				continue;
			}
			if (o.getClass().isArray()) {
				addArguments(command, (Object[])o);
			} else if (Collection.class.isAssignableFrom(o.getClass())) {
				final Collection<?> c = (Collection<?>)o;
				final Object[] array = new Object[c.size()];
				int idx = 0;
				for (final Object object : c) {
					array[idx] = object;
					idx++;
				}
				addArguments(command,  array);
			} else {
				command.add(String.valueOf(o));
			}
		}
	}

	protected CmdSettings toCmdSettings() {
		return new CmdSettings(cmdSettings);
	}

	public Cmd toCmd() {
		final CmdSettings clone = toCmdSettings();
		clone.freeze();
		return new Cmd(clone);
	}

	@SuppressWarnings("unchecked")
	protected B getBuilder() {
		return (B)this;
	}
}
