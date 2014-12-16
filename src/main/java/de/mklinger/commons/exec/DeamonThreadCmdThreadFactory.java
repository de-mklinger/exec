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

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class DeamonThreadCmdThreadFactory implements ThreadFactory {
	private final ThreadGroup group;
	private final AtomicInteger threadNumber = new AtomicInteger(1);

	public DeamonThreadCmdThreadFactory() {
		final SecurityManager s = System.getSecurityManager();
		group = (s != null) ? s.getThreadGroup() :
			Thread.currentThread().getThreadGroup();
	}

	@Override
	public Thread newThread(final Runnable r) {
		final Thread t = new Thread(group, r, "cmd-" + threadNumber.getAndIncrement(), 0);
		if (!t.isDaemon()) {
			t.setDaemon(true);
		}
		if (t.getPriority() != Thread.NORM_PRIORITY) {
			t.setPriority(Thread.NORM_PRIORITY);
		}
		return t;
	}
}