package com.evgevyr.chat.client;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Client implements TCPConnectionListener{
    private String name, ipAddr;
    private int port;

    private Client() {
        try (Scanner scanner = new Scanner(new File(Client.class.getResource("Properties").getFile()))) {
            name = scanner.nextLine();
            ipAddr = scanner.nextLine();
            port = scanner.nextInt();
        } catch (IOException e) {
            System.err.println("File cannot be found!");
        }

        try {
            Scanner scanner = new Scanner(System.in);
            TCPConnection tcpConnection = new TCPConnection(this, ipAddr, port);
            tcpConnection.sendMessage(name);

            while (true)
                tcpConnection.sendMessage(name + ": " + scanner.nextLine());
        } catch (IOException e) {
            printMessage("Connection failed:" + e);
        }
    }

    @Override
    public void onConnection(TCPConnection tcpConnection) {
        printMessage("Connection ready...");
    }

    @Override
    public void onReceive(TCPConnection tcpConnection, String string) {
        if (string == null) {
            printMessage("Wrong name! You have been disconnected from the chat! :(");
            tcpConnection.disconnect();
        }
        else
            printMessage(string);
    }

    @Override
    public void onDisconnect(TCPConnection tcpConnection) {
        printMessage("Connection Closed...");
    }

    @Override
    public void onException(TCPConnection tcpConnection, Exception e) {

    }

    private synchronized void printMessage(String string) {
        System.out.println(string);
    }

    public static void main(String[] args) {
        new Client();
    }
}
