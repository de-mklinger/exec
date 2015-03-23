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

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author Marc Klinger - mklinger[at]mklinger[dot]de - klingerm
 */
public class DefaultExecutorProvider implements ExecutorProvider {
	private static volatile Executor executor = null;

	@Override
	public Executor getExecutor() {
		if (executor == null) {
			synchronized (this) {
				if (executor == null) {
					executor = Executors.newCachedThreadPool(new DeamonThreadCmdThreadFactory());
				}
			}
		}
		return executor;
	}
}
