package de.mklinger.commons.exec;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URISyntaxException;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.AssumptionViolatedException;
import org.junit.Test;

/**
 * @author Marc Klinger - mklinger[at]mklinger[dot]de
 */
public class CmdOutputUtilTest {
	@Test
	public void testStdout() throws CmdException, URISyntaxException {
		final File testClassesDir = new File(ExecutableWithMain.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		final JavaClassCmdBuilder cmdBuilder = new JavaClassCmdBuilder(ExecutableWithMain.class.getName())
				.classpath(testClassesDir.getAbsolutePath());

		final String output = CmdOutputUtil.executeForStdout(cmdBuilder);

		Assert.assertEquals("stdout", output);
	}

	@Test
	public void testStdoutTee() throws CmdException, URISyntaxException {
		final File testClassesDir = new File(ExecutableWithMain.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		final ByteArrayOutputStream stderr = new ByteArrayOutputStream();
		final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
		final JavaClassCmdBuilder cmdBuilder = new JavaClassCmdBuilder(ExecutableWithMain.class.getName())
				.classpath(testClassesDir.getAbsolutePath())
				.stderr(stderr)
				.stdout(stdout);

		final String output = CmdOutputUtil.executeForStdout(cmdBuilder);

		Assert.assertEquals("stdout", stdout.toString());
		Assert.assertEquals("stderr", stderr.toString());

		Assert.assertEquals("stdout", output);
	}

	@Test
	public void testStderr() throws CmdException, URISyntaxException {
		final File testClassesDir = new File(ExecutableWithMain.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		final JavaClassCmdBuilder cmdBuilder = new JavaClassCmdBuilder(ExecutableWithMain.class.getName())
				.classpath(testClassesDir.getAbsolutePath());

		final String output = CmdOutputUtil.executeForStderr(cmdBuilder);

		Assert.assertEquals("stderr", output);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testStderrWithRedirectEnabled() throws CmdException, URISyntaxException {
		final File testClassesDir = new File(ExecutableWithMain.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		final JavaClassCmdBuilder cmdBuilder = new JavaClassCmdBuilder(ExecutableWithMain.class.getName())
				.classpath(testClassesDir.getAbsolutePath())
				.redirectErrorStream(true);

		CmdOutputUtil.executeForStderr(cmdBuilder);
	}

	@Test
	public void testStderrTee() throws CmdException, URISyntaxException {
		final File testClassesDir = new File(ExecutableWithMain.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		final ByteArrayOutputStream stderr = new ByteArrayOutputStream();
		final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
		final JavaClassCmdBuilder cmdBuilder = new JavaClassCmdBuilder(ExecutableWithMain.class.getName())
				.classpath(testClassesDir.getAbsolutePath())
				.stderr(stderr)
				.stdout(stdout);

		final String output = CmdOutputUtil.executeForStderr(cmdBuilder);

		Assert.assertEquals("stdout", stdout.toString());
		Assert.assertEquals("stderr", stderr.toString());

		Assert.assertEquals("stderr", output);
	}

	@Test
	public void testStderrAndStdout() throws CmdException, URISyntaxException {
		final File testClassesDir = new File(ExecutableWithMain.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		final JavaClassCmdBuilder cmdBuilder = new JavaClassCmdBuilder(ExecutableWithMain.class.getName())
				.classpath(testClassesDir.getAbsolutePath());

		final String output = CmdOutputUtil.executeForOutput(cmdBuilder);

		Assert.assertThat(output, Matchers.containsString("stdout"));
		Assert.assertThat(output, Matchers.containsString("stderr"));
	}

	@Test
	public void testStderrAndStdoutTee() throws CmdException, URISyntaxException {
		final File testClassesDir = new File(ExecutableWithMain.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		final ByteArrayOutputStream stderr = new ByteArrayOutputStream();
		final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
		final JavaClassCmdBuilder cmdBuilder = new JavaClassCmdBuilder(ExecutableWithMain.class.getName())
				.classpath(testClassesDir.getAbsolutePath())
				.stderr(stderr)
				.stdout(stdout);

		final String output = CmdOutputUtil.executeForOutput(cmdBuilder);

		Assert.assertEquals("stdout", stdout.toString());
		Assert.assertEquals("stderr", stderr.toString());

		Assert.assertThat(output, Matchers.containsString("stdout"));
		Assert.assertThat(output, Matchers.containsString("stderr"));
	}

	@Test
	public void testStderrAndStdoutError() throws URISyntaxException {
		final File testClassesDir = new File(ExecutableWithMain.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		final JavaClassCmdBuilder cmdBuilder = new JavaClassCmdBuilder(ExecutableWithMain.class.getName())
				.classpath(testClassesDir.getAbsolutePath())
				.arg("error");

		try {
			CmdOutputUtil.executeForOutput(cmdBuilder);
			Assert.fail("Expected exception not thrown");
		} catch (final CmdException e) {
			Assert.assertThat(e.getMessage(), Matchers.containsString("stdout"));
			Assert.assertThat(e.getMessage(), Matchers.containsString("stderr"));
		}
	}

	@Test
	public void exampleNonWindows() throws CmdException {
		if (CmdUtil.isWindows()) {
			throw new AssumptionViolatedException("ls command not available");
		}

		final String output = CmdOutputUtil.executeForStdout(
				new CmdBuilder("ls")
				.arg("-l")
				.directory(new File("/etc")));
		System.out.println(output);
	}

	@Test
	public void exampleWindows() throws CmdException {
		if (!CmdUtil.isWindows()) {
			throw new AssumptionViolatedException("dir command not available");
		}

		final String output = CmdOutputUtil.executeForStdout(
				new CmdBuilder("cmd")
				.arg("/C")
				.arg("dir")
				.directory(new File("c:\\")));
		System.out.println(output);
	}

}
