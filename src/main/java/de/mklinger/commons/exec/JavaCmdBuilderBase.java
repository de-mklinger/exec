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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Marc Klinger - mklinger[at]mklinger[dot]de - klingerm
 */
public abstract class JavaCmdBuilderBase<B extends CmdBuilderBase<B>> extends CmdBuilderBase<B> {
	private String javaExecutable;
	private List<String> javaOpts;
	private String bootClassPathOption;

	public B javaExecutable(final String javaExecutable) {
		this.javaExecutable = javaExecutable;
		return getBuilder();
	}

	public B systemProperty(final String name, final String value) {
		final String key = "-D" + name;
		addJavaOption(key, value, true);
		return getBuilder();
	}

	public B systemProperty(final String name) {
		final String key = "-D" + name;
		addJavaOption(key, "", false);
		return getBuilder();
	}

	/**
	 * Specifies the maximum size, in bytes, of the memory allocation
	 * pool. This value must a multiple of 1024 greater than 2 MB.
	 * Append the letter k or K to indicate kilobytes, or m or M to
	 * indicate megabytes. The default value is chosen at runtime
	 * based on system configuration.
	 * <p>
	 * Examples:
	 * <ul>
	 * <li><code>"83886080"</code></li>
	 * <li><code>"81920k"</code></li>
	 * <li><code>"80m"</code></li>
	 * </ul>
	 */
	public B xmx(final String value) {
		final String key = "-Xmx";
		addJavaOption(key, value, false);
		return getBuilder();
	}

	/**
	 * Specifies the initial size, in bytes, of the memory allocation
	 * pool. This value must be a multiple of 1024 greater than 1 MB.
	 * Append the letter k or K to indicate kilobytes, or m or M to
	 * indicate megabytes. The default value is chosen at runtime
	 * based on system configuration.
	 * <p>
	 * Examples:
	 * <ul>
	 * <li><code>"6291456"</code></li>
	 * <li><code>"6144k"</code></li>
	 * <li><code>"6m"</code></li>
	 * </ul>
	 */
	public B xms(final String value) {
		final String key = "-Xms";
		addJavaOption(key, value, false);
		return getBuilder();
	}

	/**
	 * Sets the thread stack size.
	 */
	public B xss(final String value) {
		final String key = "-Xss";
		addJavaOption(key, value, false);
		return getBuilder();
	}

	public B maxPermSize(final String value) {
		final String key = "-XX:MaxPermSize";
		addJavaOption(key, value, true);
		return getBuilder();
	}

	public B bootClassPathPrepended(List<File> bootClassPathAdditions) {
		if (bootClassPathAdditions == null || bootClassPathAdditions.isEmpty()) {
			return getBuilder();
		}
		String bootClassPathOption = buildBootClassPath(bootClassPathAdditions, "/p");
		addJavaOption(bootClassPathOption);
		return getBuilder();
	}

	public B bootClassPathReplace(List<File> bootClassPathEntries) {
		if (bootClassPathEntries == null || bootClassPathEntries.isEmpty()) {
			throw new IllegalArgumentException("BootClasspathEntries cannot be null or empty!");
		}
		String bootClassPathOption = buildBootClassPath(bootClassPathEntries, "");
		addJavaOption(bootClassPathOption);
		return getBuilder();
	}

	public B bootClassPathAppend(List<File> bootClassPathAdditions) {
		if (bootClassPathAdditions == null || bootClassPathAdditions.isEmpty()) {
			return getBuilder();
		}
		String bootClassPathOption = buildBootClassPath(bootClassPathAdditions, "/a");
		addJavaOption(bootClassPathOption);
		return getBuilder();
	}

	private String buildBootClassPath(List<File> bootClasspathAdditions, String bootClasspathSuffix) {
		StringBuilder sb = new StringBuilder("-Xbootclasspath").append(bootClasspathSuffix).append(":");
		for (File bootClasspathAddition : bootClasspathAdditions) {
			sb.append(bootClasspathAddition.getAbsoluteFile()).append(File.pathSeparator);
		}
		sb.delete(sb.length() - 1, sb.length());
		return sb.toString();
	}

	private void addJavaOption(final String key, final String value, final boolean useEquals) {
		String fullValue;
		if (useEquals) {
			fullValue = key + "=" + value;
		} else {
			fullValue = key + value;
		}
		addJavaOption(fullValue);
	}

	public B addJavaOption(final String option) {
		if (javaOpts == null) {
			javaOpts = new LinkedList<>();
		}
		javaOpts.add(option);
		return getBuilder();
	}

	@Override
	public CmdSettings toCmdSettings() {
		final CmdSettings cmdSettings = super.toCmdSettings();
		final List<String> additionalCommandParts = getJavaCommandParts();
		final List<String> command = cmdSettings.getCommand();
		if (command == null) {
			cmdSettings.setCommand(additionalCommandParts);
		} else {
			command.addAll(0, additionalCommandParts);
		}
		return cmdSettings;
	}

	protected List<String> getJavaCommandParts() {
		final List<String> additionalCommandParts = new LinkedList<>();
		additionalCommandParts.add(getActualJavaExecutable());
		Collections.sort(javaOpts);
		additionalCommandParts.addAll(javaOpts);
		return additionalCommandParts;
	}

	private String getActualJavaExecutable() {
		if (javaExecutable != null) {
			return javaExecutable;
		}
		return getDefaultJavaExecutable();
	}

	private static volatile String defaultJavaExecutable;

	private static String getDefaultJavaExecutable() {
		if (defaultJavaExecutable == null) {
			synchronized (JavaCmdBuilderBase.class) {
				if (defaultJavaExecutable == null) {
					JavaHome javaHome = JavaHome.getJavaHomeByEnvironmentVariable();
					if (javaHome == null) {
						javaHome = JavaHome.getByRuntime();
					}
					if (javaHome == null) {
						return "java";
					}
					final File java = javaHome.getJavaExecutable();
					if (java == null) {
						return "java";
					}
					return java.getAbsolutePath();
				}
			}
		}
		return defaultJavaExecutable;
	}
}
