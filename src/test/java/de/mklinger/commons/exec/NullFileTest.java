package de.mklinger.commons.exec;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Marc Klinger - mklinger[at]mklinger[dot]de - klingerm
 */
public class NullFileTest {
	@Test
	public void testNative() throws IOException {
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
		try (final NullFile nf = new NullFile(new File("DOESNOTEXIST", String.valueOf(Math.random())))) {
			Assert.assertNotNull(nf.getFile());
			Assert.assertTrue("Null file does not exist", nf.getFile().exists());
			Assert.assertTrue("Cannot write null file", nf.getFile().canWrite());
			f[0] = nf.getFile();
		}
		Assert.assertNotNull(f[0]);
		Assert.assertFalse("Null file does still exist", f[0].exists());
	}
}
