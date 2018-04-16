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

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import org.apache.commons.io.input.ClosedInputStream;
import org.apache.commons.io.output.ClosedOutputStream;

import de.mklinger.commons.exec.internal.DefaultExecutorSupplier;
import de.mklinger.commons.exec.internal.Pingable;
import de.mklinger.commons.junitsupport.BeanTestBase;

/**
 * @author Marc Klinger - mklinger[at]mklinger[dot]de
 */
public class CmdSettingsTest extends BeanTestBase<CmdSettings> {
	public CmdSettingsTest() {
		super(CmdSettings.class);
	}

	@Override
	protected String[] getIgnorePropertyNames() {
		return new String[] { "frozen" };
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
		if (isParameterizedType(type, Supplier.class, Executor.class)) {
			return new DefaultExecutorSupplier();
		}
		return super.createValue(type);
	}

	private static boolean isParameterizedType(final Type actualType, final Type expectedRawType, final Type... expectedTypeArguments) {
		if (!(actualType instanceof ParameterizedType)) {
			return false;
		}
		final ParameterizedType actualParameterizedType = (ParameterizedType) actualType;
		final Type actualRawType = actualParameterizedType.getRawType();
		if (actualRawType != expectedRawType) {
			return false;
		}
		final Type[] actualTypeArguments = actualParameterizedType.getActualTypeArguments();
		return Arrays.equals(actualTypeArguments, expectedTypeArguments);
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
