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
package de.mklinger.commons.exec.internal;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Marc Klinger - mklinger[at]mklinger[dot]de
 */
public class ErrorHandlingRunnableTest {
	@Test
	public void testNoError() {
		final ErrorHandlingRunnable r = new ErrorHandlingRunnable() {
			@Override
			protected void doRun() throws Exception {
				// do nothing
			}
		};
		r.run();
		Assert.assertFalse(r.getError().isPresent());
	}

	@Test
	public void testWithError() {
		final Exception e = new RuntimeException("test");
		final ErrorHandlingRunnable r = new ErrorHandlingRunnable() {
			@Override
			protected void doRun() throws Exception {
				throw e;
			}
		};
		r.run();
		Assert.assertSame(e, r.getError().get());
	}
}
