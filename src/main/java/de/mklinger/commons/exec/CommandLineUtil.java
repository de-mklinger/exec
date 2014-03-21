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
	private static final String OS_NAME = System.getProperty("os.name");
	private static final String OS_ARCH = System.getProperty("os.arch");

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
			// this should already be covered by which, but sometimes seems not to work :(
			executablePath = findExecutableBySystemPath(executable);
		}
		if (executablePath == null) {
			LOG.info("Could not find {} executable on PATH", executable);
		} else {
			LOG.info("Found executable {} on PATH at location {}", executable, executablePath);
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
		LOG.info("Executable not found on PATH: {}", path);
		return null;
	}

	private static String findExecutableWithCommand(final Object... command) {
		try {
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			new CommandLine(command).stdout(out).execute();
			return extractExecutablePath(out.toString());
		} catch (final CommandLineException e) {
			// ignore
			LOG.debug("Error trying to find executable with command", e);
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
		if (programFiles != null) {
			final File programFilesDir = new File(programFiles);
			if (programFilesDir.isDirectory()) {
				LOG.info("Trying program files dierctory: {}", programFilesDir);
				path = findWindowsProgramFilesExecutable(programFilesDir, executable, subFolders);
			}
		}

		if (path == null) {
			final String programFilesX86 = System.getenv("ProgramFiles(x86)");
			if (programFilesX86 != null) {
				final File programFilesX86Dir = new File(programFilesX86);
				if (programFilesX86Dir.isDirectory()) {
					LOG.info("Trying program files x86 dierctory: {}", programFilesX86);
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
				LOG.info("Found windows program files executable at: {}", executableFile);
				return path;
			}
		}
		return null;
	}

	public static String findMacOsXApplicationExecutable(final String executable, final String... appNames) {
		final File applicationsDir = new File("/Applications");
		if (!applicationsDir.exists() || !applicationsDir.canRead()) {
			return null;
		}
		for (final String appName : appNames) {
			final File executableCandidate = new File(applicationsDir, appName + "/Contents/MacOS/" + executable);
			if (executableCandidate.exists() && executableCandidate.canExecute()) {
				LOG.info("Found MacOSX applications executable at: {}", executableCandidate);
				return executableCandidate.getAbsolutePath();
			}
		}
		return null;
	}

	public static boolean isWindows() {
		final String osName = OS_NAME;
		return osName != null && osName.toLowerCase().startsWith("win");
	}

	public static boolean isMacOsX() {
		final String osName = OS_NAME;
		return "Mac OS X".equalsIgnoreCase(osName);
	}

	public static boolean isLinux() {
		final String osName = OS_NAME;
		return "Linux".equalsIgnoreCase(osName);
	}

	public static boolean is64bit() {
		final String arch = OS_ARCH;
		return arch != null && arch.contains("64");
	}

	public static String escapeShellArg(final String s) {
		if (s == null || s.isEmpty()) {
			return "''";
		}
		final String escaped = s.replace("\'", "\\\'");
		return "\'" + escaped + "\'";
	}
}
