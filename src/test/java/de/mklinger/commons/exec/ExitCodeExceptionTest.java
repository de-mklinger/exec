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

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Marc Klinger - mklinger[at]mklinger[dot]de
 */
public class ExitCodeExceptionTest {
	@Test
	public void testConstructor1() {
		final ExitCodeException e = new ExitCodeException(111, 999);
		Assert.assertNull(e.getMessage());
		Assert.assertEquals(111, e.getExpectedExitCode());
		Assert.assertEquals(999, e.getExitCode());
		Assert.assertTrue("Missing toString() contents", e.toString().contains("111"));
		Assert.assertTrue("Missing toString() contents", e.toString().contains("999"));
	}

	@Test
	public void testConstructor2() {
		final ExitCodeException e = new ExitCodeException("msg", 111, 999);
		Assert.assertEquals("msg", e.getMessage());
		Assert.assertEquals(111, e.getExpectedExitCode());
		Assert.assertEquals(999, e.getExitCode());
		Assert.assertTrue("Missing toString() contents", e.toString().contains("msg"));
		Assert.assertTrue("Missing toString() contents", e.toString().contains("111"));
		Assert.assertTrue("Missing toString() contents", e.toString().contains("999"));
	}
}
