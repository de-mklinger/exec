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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.AssumptionViolatedException;
import org.junit.Test;

/**
 * @author Marc Klinger - mklinger[at]mklinger[dot]de - klingerm
 */
public class CmdBuilderTest {
	@Test
	public void example() throws CommandLineException {
		if (CommandLineUtil.isWindows()) {
			throw new AssumptionViolatedException("ls command not available");
		}

		final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
		new CmdBuilder("ls")
		.arg("-l")
		.directory(new File("/etc"))
		.stdout(stdout)
		.toCmd()
		.execute();
		final String output = stdout.toString();
		System.out.println(output);
	}

	@Test
	public void testCommandString() {
		final CmdBuilder cb = new CmdBuilder("command");
		final CmdSettings cmdSettings = cb.toCmdSettings();
		final List<String> command = cmdSettings.getCommand();
		Assert.assertNotNull(command);
		Assert.assertEquals(1, command.size());
		Assert.assertEquals("command", command.get(0));
	}

	@Test
	public void testCommandFile() {
		final File f = new File(".");
		final CmdBuilder cb = new CmdBuilder(f);
		final CmdSettings cmdSettings = cb.toCmdSettings();
		final List<String> command = cmdSettings.getCommand();
		Assert.assertNotNull(command);
		Assert.assertEquals(1, command.size());
		Assert.assertEquals(f.getAbsolutePath(), command.get(0));
	}

	@Test(expected = NullPointerException.class)
	public void testCommandStringNull() {
		new CmdBuilder((String)null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCommandStringEmpty() {
		new CmdBuilder("");
	}

	@Test(expected = NullPointerException.class)
	public void testCommandFileNull() {
		new CmdBuilder((File)null);
	}

	@Test
	public void testCommandStringWithArgs() {
		final CmdBuilder cb = new CmdBuilder("command");
		cb.arg("arg1");
		cb.args("arg2", "arg3");
		cb.arg("arg4");
		final CmdSettings cmdSettings = cb.toCmdSettings();
		final List<String> command = cmdSettings.getCommand();
		Assert.assertNotNull(command);
		Assert.assertEquals(5, command.size());
		Assert.assertEquals("command", command.get(0));
		Assert.assertEquals("arg1", command.get(1));
		Assert.assertEquals("arg2", command.get(2));
		Assert.assertEquals("arg3", command.get(3));
		Assert.assertEquals("arg4", command.get(4));
	}

	@Test
	public void testCommandFileWithArgs() {
		final File f = new File(".");
		final CmdBuilder cb = new CmdBuilder(f);
		cb.arg("arg1");
		cb.args("arg2", "arg3");
		cb.arg("arg4");
		final CmdSettings cmdSettings = cb.toCmdSettings();
		final List<String> command = cmdSettings.getCommand();
		Assert.assertNotNull(command);
		Assert.assertEquals(5, command.size());
		Assert.assertEquals(f.getAbsolutePath(), command.get(0));
		Assert.assertEquals("arg1", command.get(1));
		Assert.assertEquals("arg2", command.get(2));
		Assert.assertEquals("arg3", command.get(3));
		Assert.assertEquals("arg4", command.get(4));
	}

	@Test
	public void testNullArgs1() {
		final CmdBuilder cb = new CmdBuilder("command");
		cb.args((Object[])null);
		final CmdSettings cmdSettings = cb.toCmdSettings();
		final List<String> command = cmdSettings.getCommand();
		Assert.assertNotNull(command);
		Assert.assertEquals(1, command.size());
		Assert.assertEquals("command", command.get(0));
	}

	@Test
	public void testNullArgs2() {
		final CmdBuilder cb = new CmdBuilder("command");
		cb.args(new Object[] { null });
		final CmdSettings cmdSettings = cb.toCmdSettings();
		final List<String> command = cmdSettings.getCommand();
		Assert.assertNotNull(command);
		Assert.assertEquals(1, command.size());
		Assert.assertEquals("command", command.get(0));
	}

	@Test
	public void testEmptyArgs() {
		final CmdBuilder cb = new CmdBuilder("command");
		cb.args(new Object[0]);
		final CmdSettings cmdSettings = cb.toCmdSettings();
		final List<String> command = cmdSettings.getCommand();
		Assert.assertNotNull(command);
		Assert.assertEquals(1, command.size());
		Assert.assertEquals("command", command.get(0));
	}

	@Test
	public void testNullArgs3() {
		final CmdBuilder cb = new CmdBuilder("command");
		cb.args((Object)null);
		final CmdSettings cmdSettings = cb.toCmdSettings();
		final List<String> command = cmdSettings.getCommand();
		Assert.assertNotNull(command);
		Assert.assertEquals(1, command.size());
		Assert.assertEquals("command", command.get(0));
	}

	@Test
	public void testNullArg() {
		final CmdBuilder cb = new CmdBuilder("command");
		cb.arg(null);
		final CmdSettings cmdSettings = cb.toCmdSettings();
		final List<String> command = cmdSettings.getCommand();
		Assert.assertNotNull(command);
		Assert.assertEquals(1, command.size());
		Assert.assertEquals("command", command.get(0));
	}

	@Test
	public void testArgArray() {
		final CmdBuilder cb = new CmdBuilder("command");
		cb.arg(new String[] { "arg1", "arg2" });
		final CmdSettings cmdSettings = cb.toCmdSettings();
		final List<String> command = cmdSettings.getCommand();
		Assert.assertNotNull(command);
		Assert.assertEquals(3, command.size());
		Assert.assertEquals("command", command.get(0));
		Assert.assertEquals("arg1", command.get(1));
		Assert.assertEquals("arg2", command.get(2));
	}

	@Test
	public void testArgsArray() {
		final CmdBuilder cb = new CmdBuilder("command");
		cb.args((Object)new String[] { "arg1", "arg2" });
		final CmdSettings cmdSettings = cb.toCmdSettings();
		final List<String> command = cmdSettings.getCommand();
		Assert.assertNotNull(command);
		Assert.assertEquals(3, command.size());
		Assert.assertEquals("command", command.get(0));
		Assert.assertEquals("arg1", command.get(1));
		Assert.assertEquals("arg2", command.get(2));
	}

	@Test
	public void testArgCollection() {
		final CmdBuilder cb = new CmdBuilder("command");
		cb.arg(Arrays.asList("arg1", "arg2"));
		final CmdSettings cmdSettings = cb.toCmdSettings();
		final List<String> command = cmdSettings.getCommand();
		Assert.assertNotNull(command);
		Assert.assertEquals(3, command.size());
		Assert.assertEquals("command", command.get(0));
		Assert.assertEquals("arg1", command.get(1));
		Assert.assertEquals("arg2", command.get(2));
	}

	@Test
	public void testArgsCollection() {
		final CmdBuilder cb = new CmdBuilder("command");
		cb.args(Arrays.asList("arg1", "arg2"));
		final CmdSettings cmdSettings = cb.toCmdSettings();
		final List<String> command = cmdSettings.getCommand();
		Assert.assertNotNull(command);
		Assert.assertEquals(3, command.size());
		Assert.assertEquals("command", command.get(0));
		Assert.assertEquals("arg1", command.get(1));
		Assert.assertEquals("arg2", command.get(2));
	}

	@Test
	public void testArgToStringCollection() {
		final CmdBuilder cb = new CmdBuilder("command");
		cb.arg(Arrays.asList(new File("."), new Integer(2)));
		final CmdSettings cmdSettings = cb.toCmdSettings();
		final List<String> command = cmdSettings.getCommand();
		Assert.assertNotNull(command);
		Assert.assertEquals(3, command.size());
		Assert.assertEquals("command", command.get(0));
		Assert.assertEquals(new File(".").getAbsolutePath(), command.get(1));
		Assert.assertEquals("2", command.get(2));
	}

	@Test
	public void testArgsToStringCollection() {
		final CmdBuilder cb = new CmdBuilder("command");
		cb.args(Arrays.asList(new File("."), new Integer(2)));
		final CmdSettings cmdSettings = cb.toCmdSettings();
		final List<String> command = cmdSettings.getCommand();
		Assert.assertNotNull(command);
		Assert.assertEquals(3, command.size());
		Assert.assertEquals("command", command.get(0));
		Assert.assertEquals(new File(".").getAbsolutePath(), command.get(1));
		Assert.assertEquals("2", command.get(2));
	}

	@Test
	public void testArgArrayNested() {
		final CmdBuilder cb = new CmdBuilder("command");
		cb.arg(new Object[] { "arg1", new String[] { "arg2", "arg3" }});
		final CmdSettings cmdSettings = cb.toCmdSettings();
		final List<String> command = cmdSettings.getCommand();
		Assert.assertNotNull(command);
		Assert.assertEquals(4, command.size());
		Assert.assertEquals("command", command.get(0));
		Assert.assertEquals("arg1", command.get(1));
		Assert.assertEquals("arg2", command.get(2));
		Assert.assertEquals("arg3", command.get(3));
	}

	@Test
	public void testArgsArrayNested() {
		final CmdBuilder cb = new CmdBuilder("command");
		cb.args((Object)new Object[] { "arg1", new String[] { "arg2", "arg3" }});
		final CmdSettings cmdSettings = cb.toCmdSettings();
		final List<String> command = cmdSettings.getCommand();
		Assert.assertNotNull(command);
		Assert.assertEquals(4, command.size());
		Assert.assertEquals("command", command.get(0));
		Assert.assertEquals("arg1", command.get(1));
		Assert.assertEquals("arg2", command.get(2));
		Assert.assertEquals("arg3", command.get(3));
	}

	@Test
	public void testArgCollectionNested() {
		final CmdBuilder cb = new CmdBuilder("command");
		cb.arg(Arrays.asList("arg1", Arrays.asList("arg2", "arg3")));
		final CmdSettings cmdSettings = cb.toCmdSettings();
		final List<String> command = cmdSettings.getCommand();
		Assert.assertNotNull(command);
		Assert.assertEquals(4, command.size());
		Assert.assertEquals("command", command.get(0));
		Assert.assertEquals("arg1", command.get(1));
		Assert.assertEquals("arg2", command.get(2));
		Assert.assertEquals("arg3", command.get(3));
	}

	@Test
	public void testArgsCollectionNested() {
		final CmdBuilder cb = new CmdBuilder("command");
		cb.args(Arrays.asList("arg1", Arrays.asList("arg2", "arg3")));
		final CmdSettings cmdSettings = cb.toCmdSettings();
		final List<String> command = cmdSettings.getCommand();
		Assert.assertNotNull(command);
		Assert.assertEquals(4, command.size());
		Assert.assertEquals("command", command.get(0));
		Assert.assertEquals("arg1", command.get(1));
		Assert.assertEquals("arg2", command.get(2));
		Assert.assertEquals("arg3", command.get(3));
	}


	@Test
	public void testStdout() {
		final OutputStream stdout = new ByteArrayOutputStream();
		final CmdBuilder cb = new CmdBuilder("command");
		Assert.assertNull(cb.toCmdSettings().getStdout());
		cb.stdout(stdout);
		Assert.assertSame(stdout, cb.toCmdSettings().getStdout());
	}

	@Test
	public void testStderr() {
		final OutputStream stderr = new ByteArrayOutputStream();
		final CmdBuilder cb = new CmdBuilder("command");
		Assert.assertNull(cb.toCmdSettings().getStderr());
		cb.stderr(stderr);
		Assert.assertSame(stderr, cb.toCmdSettings().getStderr());
	}

	@Test
	public void testStdin() {
		final byte[] stdinBytes = new byte[] { 1, 2, 3 };
		final CmdBuilder cb = new CmdBuilder("command");
		Assert.assertNull(cb.toCmdSettings().getStdinBytes());
		cb.stdin(stdinBytes);
		Assert.assertArrayEquals(stdinBytes, cb.toCmdSettings().getStdinBytes());
	}

	@Test
	public void testPing() {
		final Pingable pingable = EasyMock.createStrictMock(Pingable.class);
		EasyMock.replay(pingable);
		final CmdBuilder cb = new CmdBuilder("command");
		Assert.assertNull(cb.toCmdSettings().getPingable());
		cb.ping(pingable);
		Assert.assertSame(pingable, cb.toCmdSettings().getPingable());
		EasyMock.verify(pingable);
	}

	@Test
	public void testDirectory() {
		final File dir = new File(".");
		final CmdBuilder cb = new CmdBuilder("command");
		Assert.assertNull(cb.toCmdSettings().getDirectory());
		cb.directory(dir);
		Assert.assertSame(dir, cb.toCmdSettings().getDirectory());
	}

	@Test
	public void testEnvironment() {
		final CmdBuilder cb = new CmdBuilder("command");
		final Map<String, String> environmentEmpty = cb.toCmdSettings().getEnvironment();
		Assert.assertNull(environmentEmpty);
		cb.environment("name1", "value1");
		cb.environment("name2", "value2");
		final Map<String, String> environment = cb.toCmdSettings().getEnvironment();
		Assert.assertNotNull(environment);
		Assert.assertEquals(2 + System.getenv().size(), environment.size());
		Assert.assertEquals("value1", environment.get("name1"));
		Assert.assertEquals("value2", environment.get("name2"));
	}

	@Test
	public void testTimeout() {
		final CmdBuilder cb = new CmdBuilder("command");
		Assert.assertEquals(0, cb.toCmdSettings().getTimeout());
		cb.timeout(100);
		Assert.assertEquals(100, cb.toCmdSettings().getTimeout());
	}

	@Test
	public void testTimeout2() {
		final CmdBuilder cb = new CmdBuilder("command");
		Assert.assertEquals(0, cb.toCmdSettings().getTimeout());
		cb.timeout(1, TimeUnit.HOURS);
		Assert.assertEquals(1000 * 60 * 60, cb.toCmdSettings().getTimeout());
	}

	@Test
	public void testDestroyOnError() {
		final CmdBuilder cb = new CmdBuilder("command");
		Assert.assertTrue(cb.toCmdSettings().isDestroyOnError());
		cb.destroyOnError(false);
		Assert.assertFalse(cb.toCmdSettings().isDestroyOnError());
		cb.destroyOnError(true);
		Assert.assertTrue(cb.toCmdSettings().isDestroyOnError());
	}

	@Test
	public void testDestroyOnShutdown() {
		final CmdBuilder cb = new CmdBuilder("command");
		Assert.assertTrue(cb.toCmdSettings().isDestroyOnShutdown());
		cb.destroyOnShutdown(false);
		Assert.assertFalse(cb.toCmdSettings().isDestroyOnShutdown());
		cb.destroyOnShutdown(true);
		Assert.assertTrue(cb.toCmdSettings().isDestroyOnShutdown());
	}
}
