package de.tudbut.revpfw.v2;

import tudbut.io.TypedInputStream;
import tudbut.io.TypedOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 * @author TudbuT
 * @since 23 Oct 2021
 */

public class Client {
    
    public static void start(String server, int port, int portLocal, String key) throws IOException, InterruptedException {
        Socket socket = new Socket(server, port);
        TypedOutputStream out = new TypedOutputStream(socket.getOutputStream());
        InputStream inp = socket.getInputStream();
        OutputStream oup = socket.getOutputStream();
        socket.setTcpNoDelay(false);
        oup.write(new byte[] { 'R', 'P', 'F', 73 });
        out.writeString(key);
        socket.setSendBufferSize(0xffff);
        socket.setReceiveBufferSize(0xffff);
        socket.setKeepAlive(true);
        
        ArrayList<Socket> sockets = new ArrayList<>();
        SCComm scComm = new SCComm(socket);
        CSComm csComm = new CSComm(socket);
        
        if(scComm.readPacketType() != SCComm.PacketType.KEEPALIVE) {
            return;
        }
        System.out.println("READY.");
        
        long lastKA = 0;
        while (true) {
            if(System.currentTimeMillis() - lastKA >= 30000) {
                lastKA = System.currentTimeMillis();
                csComm.writePacketType(CSComm.PacketType.KEEPALIVE);
            }
            if(inp.available() > 0) {
                readFromServer(sockets, scComm, csComm, portLocal);
            }
            readFromClients(sockets, csComm);
            Thread.sleep(1);
        }
    }
    
    private static void readFromServer(ArrayList<Socket> sockets, SCComm scComm, CSComm csComm, int portLocal) throws IOException {
        switch (scComm.readPacketType()) {
            case CONNECT:
                handleConnect(sockets, portLocal); break;
            case DISCONNECT:
                handleDisconnect(sockets, scComm); break;
            case DATA:
                handleData(sockets, scComm, csComm); break;
        }
    }
    
    private static void handleConnect(ArrayList<Socket> sockets, int portLocal) throws IOException {
        System.out.println("CONNECTION");
        Socket s = new Socket("localhost", portLocal);
        sockets.add(s);
    }
    
    private static void handleDisconnect(ArrayList<Socket> sockets, SCComm scComm) throws IOException {
        int cid = scComm.readDisconnect();
        try {
            sockets.get(cid).close();
            sockets.set(cid, null);
        }
        catch (Exception ignored) { }
    }
    
    private static void handleData(ArrayList<Socket> sockets, SCComm scComm, CSComm csComm) throws IOException {
        int cid = scComm.readDataPacketCID();
        byte[] data = scComm.readDataPacketDAT();
        Socket socket = sockets.get(cid);
        if (socket == null)
            return;
        try {
            BufferFixer.write(socket.getOutputStream(), data);
        } catch (Exception e) {
            sockets.get(cid).close();
            sockets.set(cid, null);
            csComm.writePacketType(CSComm.PacketType.DISCONNECT);
            csComm.writeDisconnect(cid);
        }
    }
    
    private static void readFromClients(ArrayList<Socket> sockets, CSComm csComm) throws IOException {
        for (int cid = 0, socketsSize = sockets.size() ; cid < socketsSize ; cid++) {
            Socket socket = sockets.get(cid);
            if (socket == null)
                continue;
            if (socket.isInputShutdown()) {
                socket.close();
                sockets.set(cid, null);
                csComm.writePacketType(CSComm.PacketType.DISCONNECT);
                csComm.writeDisconnect(cid);
                continue;
            }
            try {
                if(socket.getInputStream().available() > 0) {
                    readFromClient(cid, socket.getInputStream(), csComm);
                }
            } catch (Exception ignored) {
                socket.close();
                sockets.set(cid, null);
                csComm.writePacketType(CSComm.PacketType.DISCONNECT);
                csComm.writeDisconnect(cid);
            }
        }
    }
    
    private static void readFromClient(int cid, InputStream inp, CSComm csComm) throws IOException {
        byte[] bytes = new byte[inp.available()];
        BufferFixer.read(inp, bytes);
        csComm.writePacketType(CSComm.PacketType.DATA);
        csComm.writeDataPacket(cid, bytes);
    }
}
