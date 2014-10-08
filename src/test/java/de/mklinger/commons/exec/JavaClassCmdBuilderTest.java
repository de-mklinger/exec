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

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Marc Klinger - mklinger[at]mklinger[dot]de - klingerm
 */
public class JavaClassCmdBuilderTest {
	@Test
	public void test() {
		final CmdSettings cmdSettings = new JavaClassCmdBuilder("de.mklinger.test.MyClass")
		.arg("arg1")
		.classpath("dir/*")
		.arg("arg2")
		.xmx("1g")
		.xms("1g")
		.maxPermSize("256m")
		.classpath("anotherdir")
		.systemProperty("myprop", "mypropvalue")
		.systemProperty("mypropwithoutvalue")
		.toCmdSettings();

		final List<String> actualArgs = new ArrayList<>(cmdSettings.getCommand());
		actualArgs.remove(0);

		final List<String> expectedArgs = new ArrayList<>();
		expectedArgs.add("-Dmyprop=mypropvalue");
		expectedArgs.add("-Dmypropwithoutvalue");
		expectedArgs.add("-XX:MaxPermSize=256m");
		expectedArgs.add("-Xms1g");
		expectedArgs.add("-Xmx1g");
		expectedArgs.add("-cp");
		expectedArgs.add("dir/*:anotherdir");
		expectedArgs.add("de.mklinger.test.MyClass");
		expectedArgs.add("arg1");
		expectedArgs.add("arg2");

		Assert.assertEquals(expectedArgs, actualArgs);
	}
}
