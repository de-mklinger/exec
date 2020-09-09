package de.mklinger.commons.exec.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ProxyOutputStream extends FilterOutputStream {
    public ProxyOutputStream(OutputStream out) {
        super(out);
    }

    // Implement here delegating. Not implemented delegating in FilterOutputStream
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
    }
}
