package de.tudbut.revpfw.v2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author TudbuT
 * @since 23 Oct 2021
 */

public class DebugInputStream extends InputStream {
    
    public final InputStream parent;
    public final OutputStream duplicate;
    
    public DebugInputStream(InputStream parent, OutputStream duplicate) {
        this.parent = parent;
        this.duplicate = duplicate;
    }
    
    @Override
    public int read() throws IOException {
        int i = parent.read();
        duplicate.write(i);
        return i;
    }
    
    @Override
    public int available() throws IOException {
        return parent.available();
    }
    
    @Override
    public void close() throws IOException {
        parent.close();
        duplicate.flush();
    }
}
