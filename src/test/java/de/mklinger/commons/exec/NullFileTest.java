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
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.AssumptionViolatedException;
import org.junit.Test;

/**
 * @author Marc Klinger - mklinger[at]mklinger[dot]de - klingerm
 */
public class NullFileTest {
	@Test
	public void testNative() throws IOException {
		if (CmdUtil.isWindows()) {
			throw new AssumptionViolatedException("nul special file does not really exist in Windows");
		}
		try (final NullFile nf = new NullFile()) {
			Assert.assertNotNull(nf.getFile());
			Assert.assertTrue("Null file does not exist", nf.getFile().exists());
			Assert.assertTrue("Cannot write null file", nf.getFile().canWrite());
		}
	}

	@Test
	public void testSimulated() throws IOException {
		final File[] f = new File[1];
		try (final NullFile nf = new NullFile(null)) {
			Assert.assertNotNull(nf.getFile());
			Assert.assertTrue("Null file does not exist", nf.getFile().exists());
			Assert.assertTrue("Cannot write null file", nf.getFile().canWrite());
			f[0] = nf.getFile();
		}
		Assert.assertNotNull(f[0]);
		Assert.assertFalse("Null file does still exist", f[0].exists());
	}

	@Test
	public void testIvalid() throws IOException {
		final File[] f = new File[1];
		try (final NullFile nf = new NullFile(null)) {
			Assert.assertNotNull(nf.getFile());
			Assert.assertTrue("Null file does not exist", nf.getFile().exists());
			Assert.assertTrue("Cannot write null file", nf.getFile().canWrite());
			f[0] = nf.getFile();
		}
		Assert.assertNotNull(f[0]);
		Assert.assertFalse("Null file does still exist", f[0].exists());
	}

	@Test
	public void testWindows() throws IOException {
		if (!CmdUtil.isWindows()) {
			throw new AssumptionViolatedException("Can only be run on Windows");
		}
		final File nul = new File("nul");
		try (FileOutputStream fout = new FileOutputStream(nul)) {
			fout.write("test".getBytes());
		}
		Assert.assertFalse(nul.exists());
	}
}
