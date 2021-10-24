package de.tudbut.revpfw.v2;

import tudbut.io.TypedInputStream;
import tudbut.io.TypedOutputStream;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @author TudbuT
 * @since 23 Oct 2021
 */

public class CSComm {
    
    private final Socket socket;
    private final InputStream inp;
    private final OutputStream out;
    private final TypedInputStream tinp;
    private final TypedOutputStream tout;
    
    public CSComm(Socket socket) throws IOException {
        this.socket = socket;
        this.inp = socket.getInputStream();
        this.out = socket.getOutputStream();
        this.tinp = new TypedInputStream(inp);
        this.tout = new TypedOutputStream(out);
    }
    
    public void writeDataPacket(int cid, byte[] data) throws IOException {
        tout.writeInt(cid);
        tout.writeInt(data.length);
        BufferFixer.write(out, data);
    }
    public int readDataPacketCID() throws IOException {
        return tinp.readInt();
    }
    public byte[] readDataPacketDAT() throws IOException {
        byte[] bytes = new byte[tinp.readInt()];
        BufferFixer.read(inp, bytes);
        return bytes;
    }
    
    public void writeDisconnect(int cid) throws IOException {
        tout.writeInt(cid);
        out.flush();
    }
    public int readDisconnect() throws IOException {
        return tinp.readInt();
    }
    
    public void writePacketType(PacketType type) throws IOException {
        tout.writeInt(type.ordinal());
        out.flush();
    }
    public PacketType readPacketType() throws IOException {
        return PacketType.values()[tinp.readInt()];
    }
    
    public enum PacketType {
        DATA,
        DISCONNECT,
        KEEPALIVE,
    }
}
