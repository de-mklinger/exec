package de.mklinger.commons.exec;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Marc Klinger - mklinger[at]mklinger[dot]de - klingerm
 */
public class JavaJarCmdBuilderTest {
	@Test
	public void test() {
		final CmdSettings cmdSettings = new JavaJarCmdBuilder("myjar.jar")
		.arg("arg1")
		.arg("arg2")
		.xmx("1g")
		.xms("1g")
		.maxPermSize("256m")
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
		expectedArgs.add("-jar");
		expectedArgs.add("myjar.jar");
		expectedArgs.add("arg1");
		expectedArgs.add("arg2");

		Assert.assertEquals(expectedArgs, actualArgs);
	}
}
