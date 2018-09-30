package com.evgevyr.chat.server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Scanner;

public class Server implements TCPConnectionListener {
    private ArrayList<TCPConnection> tcpConnections = new ArrayList<>();

    private Server() {
        System.out.println("Server running...");

        try(ServerSocket serverSocket = new ServerSocket(8080)) {
            while (true) {
                try {
                    new TCPConnection(serverSocket.accept(), this);
                } catch (IOException e) {

                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendToAllClients(String string) {
        for (int i = 0; i < tcpConnections.size(); i++) {
            tcpConnections.get(i).sendMessage(string);
        }
    }

    private void userCheck(String user, TCPConnection tcpConnection) {
        try (Scanner scanner = new Scanner(new File(Server.class.getResource("Properties").getFile()))) {
            while (scanner.hasNextLine()) {
                if (user.equals(scanner.nextLine())) {
                    tcpConnection.canSending = true;
                }
            }

            if (!tcpConnection.canSending) {
                tcpConnection.sendMessage("You can't send messages!");
                tcpConnection.disconnect(); // We don't need to spend time on unregistered user.
                tcpConnections.remove(tcpConnection);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void onConnection(TCPConnection tcpConnection) {
        tcpConnections.add(tcpConnection);
        System.out.println("Incoming connection: " + tcpConnection.getSocket().getInetAddress() + ":" + tcpConnection.getSocket().getPort());
    }

    @Override
    public synchronized void onReceive(TCPConnection tcpConnection, String string) {
        if (tcpConnection.canSending && !string.equals("null"))
            sendToAllClients(string);
        else
            userCheck(string, tcpConnection);
    }

    @Override
    public synchronized void onDisconnect(TCPConnection tcpConnection) {
        tcpConnections.remove(tcpConnection);
        System.out.println("Disconnected: " + tcpConnection.getSocket().getInetAddress() + ":" + tcpConnection.getSocket().getPort());
    }

    @Override
    public synchronized void onException(TCPConnection tcpConnection, Exception e) {
        System.out.println("Error: " + e);
    }

    public static void main(String[] args) {
        new Server();
    }
}
