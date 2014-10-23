package de.mklinger.commons.exec;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Marc Klinger - mklinger[at]mklinger[dot]de - klingerm
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
