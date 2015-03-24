package de.mklinger.commons.exec;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

import org.apache.commons.io.input.ClosedInputStream;
import org.apache.commons.io.output.ClosedOutputStream;

import de.mklinger.commons.junit.BeanTestBase;

/**
 * @author Marc Klinger - mklinger[at]mklinger[dot]de - klingerm
 */
public class CmdSettingsTest extends BeanTestBase<CmdSettings> {
	public CmdSettingsTest() {
		super(CmdSettings.class);
	}

	@Override
	protected Object createValue(final Type type) {
		if (type == OutputStream.class) {
			return ClosedOutputStream.CLOSED_OUTPUT_STREAM;
		}
		if (type == InputStream.class) {
			return ClosedInputStream.CLOSED_INPUT_STREAM;
		}
		if (type == File.class) {
			return new File(String.valueOf(getNextTestValue()));
		}
		if (type == Pingable.class) {
			return new Pingable() {
				@Override
				public void ping() {
				}
			};
		}
		if (type == ExecutorProvider.class) {
			return new DefaultExecutorProvider();
		}
		return super.createValue(type);
	}

	@Override
	protected Object getFieldDefaultValue(final String propertyName) {
		if ("destroyOnError".equals(propertyName)) {
			return true;
		}
		if ("destroyOnShutdown".equals(propertyName)) {
			return true;
		}
		return super.getFieldDefaultValue(propertyName);
	}
}
