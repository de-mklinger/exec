package de.mklinger.commons.exec;

import java.util.List;

/**
 * @author Marc Klinger - mklinger[at]mklinger[dot]de - klingerm
 */
public class JavaJarCmdBuilder extends JavaCmdBuilderBase<JavaJarCmdBuilder> {
	private final String jar;

	public JavaJarCmdBuilder(final String jar) {
		this.jar = jar;
	}

	@Override
	protected List<String> getJavaCommandParts() {
		final List<String> javaCommandParts = super.getJavaCommandParts();
		javaCommandParts.add("-jar");
		javaCommandParts.add(jar);
		return javaCommandParts;
	}
}
