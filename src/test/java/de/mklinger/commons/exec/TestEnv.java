package de.mklinger.commons.exec;

import org.junit.Test;

public class TestEnv {
	@Test
	public void test() throws CommandLineException {
		new CmdBuilder("env").arg("env").environment("test", "bla").stdout(System.out).toCmd().execute();
	}
}
