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
import java.util.List;

/**
 * @author Marc Klinger - mklinger[at]mklinger[dot]de - klingerm
 */
public class JavaClassCmdBuilder extends JavaCmdBuilderBase<JavaClassCmdBuilder> {
	private final String className;
	private StringBuilder classpath;

	public JavaClassCmdBuilder(final String className) {
		this.className = className;
	}

	public JavaClassCmdBuilder classpath(final String... classpathEntries) {
		if (classpathEntries == null || classpathEntries.length == 0) {
			return getBuilder();
		}
		if (classpath == null) {
			classpath = new StringBuilder();
		}
		for (final String classpathEntry : classpathEntries) {
			if (classpath.length() > 0) {
				classpath.append(File.pathSeparatorChar);
			}
			classpath.append(classpathEntry);
		}
		return getBuilder();
	}

	@Override
	protected List<String> getJavaCommandParts() {
		final List<String> javaCommandParts = super.getJavaCommandParts();
		if (classpath != null && classpath.length() > 0) {
			javaCommandParts.add("-cp");
			javaCommandParts.add(classpath.toString());
		}
		javaCommandParts.add(className);
		return javaCommandParts;
	}
}
