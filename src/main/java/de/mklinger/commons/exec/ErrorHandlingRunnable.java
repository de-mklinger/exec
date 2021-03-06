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

import java.util.concurrent.atomic.AtomicReference;

public abstract class ErrorHandlingRunnable implements Runnable, ErrorHandler {
	private final AtomicReference<Throwable> error = new AtomicReference<>();

	@Override
	public Throwable getError() {
		return error.get();
	}

	@Override
	public void run() {
		try {
			doRun();
		} catch (final Throwable e) {
			error.set(e);
		}
	}

	protected abstract void doRun() throws Exception;
}
