package de.mklinger.commons.exec;

/**
 * @author Marc Klinger - mklinger[at]mklinger[dot]de
 */
public class ExecutableWithMain {
	public static void main(final String[] args) {
		System.out.print("stdout");
		System.err.print("stderr");

		if (args.length > 0 && args[0].equals("error")) {
			System.exit(1);
		}
	}
}
