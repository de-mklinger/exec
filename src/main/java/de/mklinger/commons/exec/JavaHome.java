package de.mklinger.commons.exec;

import java.io.File;

/**
 * @author Marc Klinger - mklinger[at]mklinger[dot]de
 */
public class JavaHome {
	private final File javaHome;

	public JavaHome(final File javaHome) {
		this.javaHome = javaHome;
	}

	public static JavaHome getByRuntime() {
		final String javaHomeProp = System.getProperty("java.home");
		if (javaHomeProp == null || javaHomeProp.isEmpty()) {
			return null;
		}
		final File f = new File(javaHomeProp);
		if (!isValidJavaHome(f)) {
			return null;
		}
		return new JavaHome(f);
	}

	public static JavaHome getJavaHomeByEnvironmentVariable() {
		final String javaHomeEnv = System.getenv("JAVA_HOME");
		if (javaHomeEnv == null || javaHomeEnv.isEmpty()) {
			return null;
		}
		final File f = new File(javaHomeEnv);
		if (!isValidJavaHome(f)) {
			return null;
		}
		return new JavaHome(f);
	}

	public static JavaHome getByPath() {
		final String s = CommandLineUtil.findExecutable("java", "java.exe");
		if (s == null || s.isEmpty()) {
			return null;
		}
		final File java = new File(s);
		final File bin = java.getParentFile();
		if (!bin.getName().equals("bin")) {
			return null;
		}
		final File javaHome = bin.getParentFile();
		if (!isValidJavaHome(javaHome)) {
			return null;
		}
		return new JavaHome(javaHome);
	}

	private static boolean isValidJavaHome(final File f) {
		// TODO more checks here
		return f.isDirectory() && f.canRead();
	}

	public File getJavaHome() {
		return javaHome;
	}

	public File getJavaExecutable() {
		File f = new File(javaHome, "bin");
		if (CommandLineUtil.isWindows()) {
			f = new File(f, "java.exe");
		} else {
			f = new File(f, "java");
		}
		if (f.exists() && f.canExecute()) {
			return f;
		}
		return null;
	}
}
