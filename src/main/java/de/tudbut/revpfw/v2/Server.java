package de.tudbut.revpfw.v2;

import tudbut.io.TypedInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author TudbuT
 * @since 23 Oct 2021
 */

public class Server {
    
    public static void start(int port, String key) throws IOException {
        ServerSocket socket = new ServerSocket(port);
        socket.setSoTimeout(1);
        
        Socket handler = null;
        SCComm scComm = null;
        CSComm csComm = null;
        TypedInputStream handlerIn = null;
        ArrayList<Socket> clients = new ArrayList<>();
        
        int speed = 0x2000;
        
        long lastKA = 0;
        while (true) {
            try {
                if(System.currentTimeMillis() - lastKA >= 30000) {
                    lastKA = System.currentTimeMillis();
                    if(scComm != null)
                        scComm.writePacketType(SCComm.PacketType.KEEPALIVE);
                }
                try {
                    Socket newClient = socket.accept();
                    newClient.setKeepAlive(true);
                    if(handler == null) {
                        byte[] bytes = new byte[4];
                        newClient.setSoTimeout(500);
                        try {
                            newClient.getInputStream().read(bytes);
                        } catch (Exception e) {
                            continue;
                        }
                        newClient.setSoTimeout(0);
                        if (Arrays.equals(bytes, new byte[] { 'R', 'P', 'F', 73 })) {
                            handlerIn = new TypedInputStream(newClient.getInputStream());
                            if (!handlerIn.readString().equals(key)) {
                                newClient.close();
                                handlerIn = null;
                            }
                            else {
                                speed = handlerIn.readInt();
                                scComm = new SCComm(newClient);
                                csComm = new CSComm(newClient);
                                newClient.setSendBufferSize(speed * 4);
                                newClient.setReceiveBufferSize(speed * 4);
                                handler = newClient;
                                scComm.writePacketType(SCComm.PacketType.KEEPALIVE);
                                System.out.println("READY");
                            }
                        }
                    }
                    else {
                        clients.add(newClient);
                        scComm.writePacketType(SCComm.PacketType.CONNECT);
                        System.out.println("CONNECTION: " + newClient);
                    }
                } catch (SocketTimeoutException ignored) {}
                if(handler == null) {
                    Thread.sleep(50);
                    continue;
                }
                if (handlerIn.getStream().available() > 0)
                    readFromHandler(scComm, csComm, clients);
                readFromClients(clients, scComm, speed);
                Thread.sleep(1);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException();
            }
        }
    }
    
    private static void readFromHandler(SCComm scComm, CSComm csComm, ArrayList<Socket> clients) throws IOException {
        switch (csComm.readPacketType()) {
            case DISCONNECT:
                handleDisconnect(csComm, clients); break;
            case DATA:
                handleData(csComm, scComm, clients); break;
        }
    }
    
    private static void handleDisconnect(CSComm csComm, ArrayList<Socket> clients) throws IOException {
        int cid = csComm.readDisconnect();
        clients.get(cid).close();
        clients.set(cid, null);
    }
    
    private static void handleData(CSComm csComm, SCComm scComm, ArrayList<Socket> clients) throws IOException {
        int cid = csComm.readDataPacketCID();
        byte[] bytes = csComm.readDataPacketDAT();
        Socket socket = clients.get(cid);
        if (socket == null)
            return;
        try {
            BufferFixer.write(socket.getOutputStream(), bytes);
        } catch (Exception e) {
            clients.get(cid).close();
            clients.set(cid, null);
            scComm.writePacketType(SCComm.PacketType.DISCONNECT);
            scComm.writeDisconnect(cid);
        }
    }
    
    private static void readFromClients(ArrayList<Socket> clients, SCComm scComm, int speed) throws IOException {
        for (int cid = 0, clientsSize = clients.size() ; cid < clientsSize ; cid++) {
            Socket socket = clients.get(cid);
            if (socket == null)
                continue;
            if (socket.isInputShutdown()) {
                socket.close();
                clients.set(cid, null);
                scComm.writePacketType(SCComm.PacketType.DISCONNECT);
                scComm.writeDisconnect(cid);
                continue;
            }
            try {
                if(socket.getInputStream().available() > 0) {
                    readFromClient(cid, socket.getInputStream(), scComm, speed);
                }
            } catch (Exception ignored) {
                socket.close();
                clients.set(cid, null);
                scComm.writePacketType(SCComm.PacketType.DISCONNECT);
                scComm.writeDisconnect(cid);
            }
        }
    }
    
    private static void readFromClient(int cid, InputStream inp, SCComm scComm, int speed) throws IOException {
        byte[] bytes = new byte[Math.min(inp.available(), speed)];
        BufferFixer.read(inp, bytes);
        scComm.writePacketType(SCComm.PacketType.DATA);
        scComm.writeDataPacket(cid, bytes);
    }
}
