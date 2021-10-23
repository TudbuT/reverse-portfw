package de.tudbut.revpfw.v2;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author TudbuT
 * @since 23 Oct 2021
 */

public class DebugOutputStream extends OutputStream {
    
    public final OutputStream parent;
    public final OutputStream duplicate;
    
    public DebugOutputStream(OutputStream parent, OutputStream duplicate) {
        this.parent = parent;
        this.duplicate = duplicate;
    }
    
    @Override
    public void write(int b) throws IOException {
        parent.write(b);
        duplicate.write(b);
    }
    
    @Override
    public void flush() throws IOException {
        parent.flush();
        duplicate.flush();
    }
    
    @Override
    public void close() throws IOException {
        parent.close();
        duplicate.flush();
    }
}
