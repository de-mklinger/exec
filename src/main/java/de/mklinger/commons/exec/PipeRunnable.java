package de.mklinger.commons.exec;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marc Klinger - mklinger[at]mklinger[dot]de
 */
public class PipeRunnable implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(PipeRunnable.class);
    private final OutputStream out;
    private final InputStream in;
    private Throwable error;
    private final AtomicBoolean running = new AtomicBoolean();

    public PipeRunnable(final InputStream in, final OutputStream out) {
        this.in = in;
        this.out = out;
    }

    @Override
    public void run() {
        running.set(true);
        try {
            final int copied = IOUtils.copy(in, out);
            LOG.debug("Copied {} bytes", copied);
        } catch (final Throwable e) {
            // TODO interrupt main thread here if we are in waitFor()? Or read and discard contents
            LOG.error("Error in PipeRunnable", e);
            this.error = e;
        } finally {
            running.set(false);
        }
    }

    public Throwable getError() {
        return error;
    }

    public boolean isRunning() {
        return running.get();
    }

    public void waitFor(final long timeoutMillis) throws CommandLineException, InterruptedException {
        final long start = System.currentTimeMillis();
        while (isRunning()) {
            if (start + timeoutMillis < System.currentTimeMillis()) {
                throw new CommandLineException("Timout waiting for pipe");
            }
            Thread.sleep(1);
        }
    }
}
