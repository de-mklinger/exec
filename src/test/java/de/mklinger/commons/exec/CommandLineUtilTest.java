package de.mklinger.commons.exec;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Marc Klinger - mklinger[at]mklinger[dot]de
 */
public class CommandLineUtilTest {
	@Test
	public void escapeShellArgTest() {
		Assert.assertEquals("''", CommandLineUtil.escapeShellArg(null));
		Assert.assertEquals("''", CommandLineUtil.escapeShellArg(""));
		Assert.assertEquals("'xyz'", CommandLineUtil.escapeShellArg("xyz"));
		Assert.assertEquals("'spaces \\'and\\' quote'", CommandLineUtil.escapeShellArg("spaces 'and' quote"));
	}
}
