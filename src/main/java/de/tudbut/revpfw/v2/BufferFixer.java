package de.tudbut.revpfw.v2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author TudbuT
 * @since 23 Oct 2021
 */

public class BufferFixer {
    
    public static void write(OutputStream stream, byte[] bytes) throws IOException {
        stream.write(bytes);
        stream.flush();
    }
    
    public static void read(InputStream stream, byte[] bytes) throws IOException {
        for (int i = 0, n ; i < bytes.length ; i+=n) {
            n = stream.read(bytes, i, bytes.length - i);
        }
    }
}
