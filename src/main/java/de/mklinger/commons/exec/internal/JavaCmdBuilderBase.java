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
package de.mklinger.commons.exec.internal;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.mklinger.commons.exec.CmdSettings;
import de.mklinger.commons.exec.JavaHome;

/**
 * @author Marc Klinger - mklinger[at]mklinger[dot]de
 */
public abstract class JavaCmdBuilderBase<B extends CmdBuilderBase<B>> extends CmdBuilderBase<B> {
	private String javaExecutable;
	private List<String> javaOpts;
	private StringBuilder modulePath;
	private StringBuilder upgradeModulePath;
	private StringBuilder modules;

	public B javaExecutable(final String javaExecutable) {
		this.javaExecutable = javaExecutable;
		return getBuilder();
	}

	public B javaExecutableFromRuntime() {
		final JavaHome javaHome = JavaHome.getByRuntime()
				.orElseThrow(() -> new IllegalStateException(
						"No valid current JavaRuntime could be determined!"));

		javaExecutable = javaHome.getJavaExecutable()
				.orElseThrow(() -> new IllegalStateException(
						"Current Java-runtime is valid, but no executable 'java' file could be found in javaHome: " +
								javaHome.getJavaHome().getAbsolutePath()))
				.getAbsolutePath();

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
	 * Specifies the maximum size, in bytes, of the memory allocation pool. This
	 * value must a multiple of 1024 greater than 2 MB. Append the letter k or K
	 * to indicate kilobytes, or m or M to indicate megabytes. The default value
	 * is chosen at runtime based on system configuration.
	 * <p>
	 * Examples:
	 * <ul>
	 * <li><code>"83886080"</code></li>
	 * <li><code>"81920k"</code></li>
	 * <li><code>"80m"</code></li>
	 * </ul>
	 *
	 * @param value The Xmx value as described above.
	 * @return This builder
	 */
	public B xmx(final String value) {
		final String key = "-Xmx";
		addJavaOption(key, value, false);
		return getBuilder();
	}

	/**
	 * Specifies the initial size, in bytes, of the memory allocation pool. This
	 * value must be a multiple of 1024 greater than 1 MB. Append the letter k
	 * or K to indicate kilobytes, or m or M to indicate megabytes. The default
	 * value is chosen at runtime based on system configuration.
	 * <p>
	 * Examples:
	 * </p>
	 * <ul>
	 * <li><code>"6291456"</code></li>
	 * <li><code>"6144k"</code></li>
	 * <li><code>"6m"</code></li>
	 * </ul>
	 *
	 * @param value The Xms value as described above.
	 * @return This builder
	 */
	public B xms(final String value) {
		final String key = "-Xms";
		addJavaOption(key, value, false);
		return getBuilder();
	}

	/**
	 * Sets the thread stack size (in bytes). Append the letter k or K to
	 * indicate KB, m or M to indicate MB, g or G to indicate GB. The default
	 * value depends on virtual memory.
	 * <p>
	 * Examples:
	 * </p>
	 * <ul>
	 * <li><code>"1m"</code></li>
	 * <li><code>"1024k"</code></li>
	 * <li><code>"1048576"</code></li>
	 * </ul>
	 *
	 * @param value The Xss value as described above.
	 * @return This builder
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

	public B bootClassPathPrepend(final String... bootClassPathAdditions) {
		if (bootClassPathAdditions == null || bootClassPathAdditions.length == 0) {
			return getBuilder();
		}
		final String bootClassPathOption = buildBootClassPath("/p", bootClassPathAdditions);
		addJavaOption(bootClassPathOption);
		return getBuilder();
	}

	public B bootClassPathReplace(final String... bootClassPathEntries) {
		if (bootClassPathEntries == null || bootClassPathEntries.length == 0) {
			throw new IllegalArgumentException("BootClasspathEntries cannot be null or empty!");
		}
		final String bootClassPathOption = buildBootClassPath("", bootClassPathEntries);
		addJavaOption(bootClassPathOption);
		return getBuilder();
	}

	public B bootClassPathAppend(final String... bootClassPathAdditions) {
		if (bootClassPathAdditions == null || bootClassPathAdditions.length == 0) {
			return getBuilder();
		}
		final String bootClassPathOption = buildBootClassPath("/a", bootClassPathAdditions);
		addJavaOption(bootClassPathOption);
		return getBuilder();
	}

	private String buildBootClassPath(final String bootClasspathPrefix, final String... bootClasspathAdditions) {
		final StringBuilder sb = new StringBuilder("-Xbootclasspath").append(bootClasspathPrefix).append(":");
		for (final String bootClasspathAddition : bootClasspathAdditions) {
			sb.append(bootClasspathAddition).append(File.pathSeparator);
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

	/**
	 * Add Java 9 module path entries.
	 *
	 * <pre>
	 * --module-path <module path>...
	 *     A : separated list of directories, each directory
	 *     is a directory of modules.
	 * </pre>
	 */
	public B modulePath(final String... modulePathEntries) {
		this.modulePath = appendPathEntries(modulePath, modulePathEntries);
		return getBuilder();
	}

	/**
	 * Add Java 9 upgrade module path entries.
	 *
	 * <pre>
	 * --upgrade-module-path <module path>...
	 *     A : separated list of directories, each directory
	 *     is a directory of modules that replace upgradeable
	 *     modules in the runtime image
	 * </pre>
	 */
	public B upgradeModulePath(final String... upgradeModulePathEntries) {
		this.upgradeModulePath = appendPathEntries(upgradeModulePath, upgradeModulePathEntries);
		return getBuilder();
	}

	private static StringBuilder appendPathEntries(StringBuilder path, final String... pathEntries) {
		if (pathEntries == null || pathEntries.length == 0) {
			return path;
		}
		if (path == null) {
			int size = pathEntries.length - 1;
			for (final String modulePathEntry : pathEntries) {
				size += modulePathEntry.length();
			}
			path = new StringBuilder(Math.max(16, size));
		}
		for (final String modulePathEntry : pathEntries) {
			if (path.length() > 0) {
				path.append(File.pathSeparatorChar);
			}
			path.append(modulePathEntry);
		}
		return path;
	}

	/**
	 * Add Java 9 modules.
	 *
	 * <pre>
	 * --add-modules <module name>[,<module name>...]
	 *               root modules to resolve in addition to the initial module.
	 *               <module name> can also be ALL-DEFAULT, ALL-SYSTEM,
	 *               ALL-MODULE-PATH.
	 * </pre>
	 */
	public B addModule(final String moduleName) {
		if (modules == null) {
			modules = new StringBuilder(Math.max(16, moduleName.length()));
		} else {
			modules.append(',');
		}
		modules.append(moduleName);
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
		if (javaOpts != null) {
			Collections.sort(javaOpts);
			additionalCommandParts.addAll(javaOpts);
		}
		if (modulePath != null) {
			additionalCommandParts.add("--module-path");
			additionalCommandParts.add(modulePath.toString());
		}
		if (upgradeModulePath != null) {
			additionalCommandParts.add("--upgrade-module-path");
			additionalCommandParts.add(upgradeModulePath.toString());
		}
		if (modules != null) {
			additionalCommandParts.add("--add-modules");
			additionalCommandParts.add(modules.toString());
		}
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
					defaultJavaExecutable = findJavaExecutable();
				}
			}
		}
		return defaultJavaExecutable;
	}

	// Visible for testing
	static String findJavaExecutable() {
		return JavaHome.getJavaHomeByEnvironmentVariable()
				.or(JavaHome::getByRuntime)
				.flatMap(JavaHome::getJavaExecutable)
				.map(File::getAbsolutePath)
				.orElse("java");
	}
}
