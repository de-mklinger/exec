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