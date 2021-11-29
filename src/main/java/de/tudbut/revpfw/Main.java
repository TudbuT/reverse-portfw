package de.tudbut.revpfw;

import de.tudbut.revpfw.v2.Client;
import de.tudbut.revpfw.v2.Server;

import java.io.IOException;

/**
 * @author TudbuT
 * @since 13 Oct 2021
 */

public class Main {
    
    public static void main(String[] args) throws IOException, InterruptedException {
        if(args.length == 0) {
            System.err.println("Usage: ");
            System.err.println("  java -jar reverse-portfw.jar server <port> <key>");
            System.err.println("  java -jar reverse-portfw.jar client <ip> <port> <destinationPort> <key> [speed in KB/ms/c]");
            return;
        }
        if(args[0].equals("client") && args.length == 5 || args.length == 6) {
            Client.start(args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3]), args[4], (int)(args.length == 6 ? Float.parseFloat(args[5]) * 1024 : 0x2000));
        }
        if(args[0].equals("server") && args.length == 3) {
            Server.start(Integer.parseInt(args[1]), args[2]);
        }
    }
}
