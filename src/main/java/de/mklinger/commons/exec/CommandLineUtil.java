package de.mklinger.commons.exec;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marc Klinger - mklinger[at]mklinger[dot]de
 */
public class CommandLineUtil {
	private static final Logger LOG = LoggerFactory.getLogger(CommandLineUtil.class);

	public static String findExecutable(final String... executables) {
		for (final String executable : executables) {
			final String executablePath = findExecutablePath(executable);
			if (executablePath != null) {
				return executablePath;
			}
		}
		return null;
	}

	private static String findExecutablePath(final String executable) {
		String executablePath = null;
		if (isWindows()) {
			executablePath = findExecutableWithCommand("where", executable);
			if (executablePath == null) {
				executablePath = findExecutableBySystemPath(executable + ".exe");
			}
		} else {
			executablePath = findExecutableWithCommand("which", executable);
			if (executablePath == null) {
				executablePath = findExecutableWithCommand("bash", "-l", "-c", "which " + executable);
			}
		}
		if (executablePath == null) {
			LOG.warn("Could not find {} executable", executable);
		} else {
			LOG.info("Found executable {} at location {}", executable, executablePath);
		}
		return executablePath;
	}

	private static String findExecutableBySystemPath(final String executableName) {
		final String path = System.getenv("PATH");
		if (path == null) {
			return null;
		}
		final StringTokenizer st = new StringTokenizer(path, File.pathSeparator);
		while (st.hasMoreTokens()) {
			final String pathEntry = st.nextToken();
			final File candidate = new File(pathEntry, executableName);
			if (candidate.exists() && candidate.canExecute()) {
				return candidate.getAbsolutePath();
			}
		}
		return null;
	}

	private static String findExecutableWithCommand(final Object... command) {
		try {
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			new CommandLine(command).stdout(out).execute();
			return extractExecutablePath(out.toString());
		} catch (final CommandLineException e) {
			// ignore
			LOG.debug("Error trying to find executable unix way", e);
		}
		return null;
	}

	private static String extractExecutablePath(final String s) {
		String executablePath = s;
		if (executablePath == null) {
			return null;
		}
		executablePath = executablePath.trim();
		if (executablePath.length() == 0) {
			return null;
		}
		final File f = new File(executablePath);
		if (f.isFile() && f.canExecute()) {
			return executablePath;
		}
		return null;
	}

	public static String findWindowsProgramFilesExecutable(final String executable, final String... subFolders) {
		String path = null;

		final String programFiles = System.getenv("ProgramFiles");
		LOG.info("Using program files dierctory: {}", programFiles);
		final File programFilesDir = new File(programFiles);
		if (programFilesDir.isDirectory()) {
			path = findWindowsProgramFilesExecutable(programFilesDir, executable, subFolders);
		}

		if (path == null) {
			final String programFilesX86 = System.getenv("ProgramFiles(x86)");
			if (programFilesX86 != null) {
				LOG.info("Using program files x86 dierctory: {}", programFilesX86);
				final File programFilesX86Dir = new File(programFilesX86);
				if (programFilesDir.isDirectory()) {
					path = findWindowsProgramFilesExecutable(programFilesX86Dir, executable, subFolders);
				}
			}
		}

		return path;
	}

	private static String findWindowsProgramFilesExecutable(final File programFilesDir, final String executable, final String... subFolders) {
		for (final String subFolder : subFolders) {
			final File executableFile = new File(programFilesDir, subFolder + File.separator + executable);
			if (executableFile.exists() && executableFile.canExecute()) {
				final String path = executableFile.getAbsolutePath();
				LOG.info("Found windows program files executable at: {}", executableFile.getAbsolutePath());
				return path;
			}
		}
		return null;
	}

	public static boolean isWindows() {
		final String osName = System.getProperty("os.name");
		return osName != null && osName.toLowerCase().startsWith("win");
	}

	public static boolean isMacOsX() {
		final String osName = System.getProperty("os.name");
		return osName != null && osName.equalsIgnoreCase("Mac OS X");
	}
}
