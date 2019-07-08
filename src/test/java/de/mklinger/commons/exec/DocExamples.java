package de.mklinger.commons.exec;

import java.io.File;
import java.nio.file.Paths;

/**
 * @author Marc Klinger - mklinger[at]mklinger[dot]de
 */
public class DocExamples {
	public static void example1() throws CmdException {
		final String output = CmdOutputUtil.executeForStdout(
				new CmdBuilder("ls")
				.arg("-l")
				.directory(new File("/etc")));
	}

	public static void example2() throws CmdException {
		new JavaJarCmdBuilder("tika-app.jar")
		.directory(Paths.get("/opt/tika"))
		.arg("--metadata")
		.arg("--json")
		.arg("myfile.docx")
		.systemProperty("java.io.tmpdir", "/mytmp")
		.xmx("2G")
		.toCmd()
		.execute();
	}
}
