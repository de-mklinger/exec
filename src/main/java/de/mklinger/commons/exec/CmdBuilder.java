package de.mklinger.commons.exec;



/**
 * @author Marc Klinger - mklinger[at]mklinger[dot]de - klingerm
 */
public class CmdBuilder extends CmdBuilderBase<CmdBuilder> {
	public CmdBuilder(final String command) {
		if (command == null) {
			throw new NullPointerException();
		}
		arg(command);
	}
}
