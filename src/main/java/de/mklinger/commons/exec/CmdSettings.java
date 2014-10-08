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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Marc Klinger - mklinger[at]mklinger[dot]de - klingerm
 */
public class CmdSettings {
	private List<String> command;
	private File directory;
	private int expectedExitValue = 0;
	private OutputStream stdout;
	private OutputStream stderr;
	private Pingable pingable = null;
	private byte[] stdinBytes;
	private Map<String, String> environment;
	private long timeout;
	private boolean destroyOnError = true;
	private boolean destroyOnShutdown = true;
	private boolean frozen;

	/**
	 * Default constructor.
	 */
	public CmdSettings() {
	}

	/**
	 * Create a copy of the given CmdSettings. The copy
	 * is not frozen.
	 */
	public CmdSettings(final CmdSettings cmdSettings) {
		this.command = cmdSettings.command;
		this.directory = cmdSettings.directory;
		this.expectedExitValue = cmdSettings.expectedExitValue;
		this.stdout = cmdSettings.stdout;
		this.stderr = cmdSettings.stderr;
		this.pingable = cmdSettings.pingable;
		if (cmdSettings.stdinBytes != null) {
			this.stdinBytes = new byte[cmdSettings.stdinBytes.length];
			System.arraycopy(cmdSettings.stdinBytes, 0, this.stdinBytes, 0, cmdSettings.stdinBytes.length);
		}
		if (cmdSettings.environment != null) {
			this.environment = new HashMap<>(cmdSettings.environment);
		}
		this.timeout = cmdSettings.timeout;
		this.destroyOnError = cmdSettings.destroyOnError;
		this.destroyOnShutdown = cmdSettings.destroyOnShutdown;
	}

	public List<String> getCommand() {
		return command;
	}

	public void setCommand(final List<String> command) {
		checkFrozen();
		this.command = command;
	}

	public File getDirectory() {
		return directory;
	}

	public void setDirectory(final File directory) {
		checkFrozen();
		this.directory = directory;
	}

	public int getExpectedExitValue() {
		return expectedExitValue;
	}

	public void setExpectedExitValue(final int expectedExitValue) {
		checkFrozen();
		this.expectedExitValue = expectedExitValue;
	}

	public OutputStream getStdout() {
		return stdout;
	}

	public void setStdout(final OutputStream stdout) {
		checkFrozen();
		this.stdout = stdout;
	}

	public OutputStream getStderr() {
		return stderr;
	}

	public void setStderr(final OutputStream stderr) {
		checkFrozen();
		this.stderr = stderr;
	}

	public Pingable getPingable() {
		return pingable;
	}

	public void setPingable(final Pingable pingable) {
		checkFrozen();
		this.pingable = pingable;
	}

	// The byte array may be manipulated even if frozen. We live with that.
	public byte[] getStdinBytes() {
		return stdinBytes;
	}

	public void setStdinBytes(final byte[] stdinBytes) {
		checkFrozen();
		this.stdinBytes = stdinBytes;
	}

	public Map<String, String> getEnvironment() {
		return environment;
	}

	public void setEnvironment(final Map<String, String> environment) {
		checkFrozen();
		this.environment = environment;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(final long timeout) {
		checkFrozen();
		this.timeout = timeout;
	}

	public boolean isDestroyOnError() {
		return destroyOnError;
	}

	public void setDestroyOnError(final boolean destroyOnError) {
		checkFrozen();
		this.destroyOnError = destroyOnError;
	}

	public boolean isDestroyOnShutdown() {
		return destroyOnShutdown;
	}

	public void setDestroyOnShutdown(final boolean destroyOnShutdown) {
		checkFrozen();
		this.destroyOnShutdown = destroyOnShutdown;
	}

	public void freeze() {
		this.frozen = true;
	}

	private void checkFrozen() {
		if (frozen) {
			throw new IllegalStateException("Instance is frozen");
		}
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append("[command=");
		if (command != null) {
			boolean first = true;
			for (final String part : command) {
				if (!first) {
					sb.append(' ');
				} else {
					first = false;
				}
				sb.append(part);
			}
		}
		return sb.toString();
	}
}