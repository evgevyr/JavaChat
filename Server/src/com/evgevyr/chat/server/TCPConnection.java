package com.evgevyr.chat.server;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;

public class TCPConnection {
    private Socket socket;
    private Thread receiveThread;
    private TCPConnectionListener tcpConnectionListener;
    private BufferedReader input;
    private BufferedWriter output;
    boolean canSending = false;

    public TCPConnection(TCPConnectionListener tcpConnectionListener, String ipAddress, int port) throws IOException {
        this(new Socket(ipAddress, port), tcpConnectionListener);
    }

    public TCPConnection(Socket socket, TCPConnectionListener tcpConnectionListener) throws IOException {
        this.socket = socket;
        this.tcpConnectionListener = tcpConnectionListener;

        input = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));
        output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")));

        receiveThread = new Thread(() -> {
            try {
                tcpConnectionListener.onConnection(TCPConnection.this);

                while (!receiveThread.isInterrupted()) {
                    tcpConnectionListener.onReceive(TCPConnection.this, input.readLine());
                }
            } catch (Exception e) {
                tcpConnectionListener.onException(TCPConnection.this, e);
            } finally {
                tcpConnectionListener.onDisconnect(TCPConnection.this);
            }
        });
        receiveThread.start();
    }

    public synchronized void sendMessage(String string) {
        try {
            output.write(string + "\r\n");
            output.flush();
        } catch (IOException e) {
            tcpConnectionListener.onException(TCPConnection.this, e);
            disconnect();
        }
    }

    public synchronized void disconnect() {
        receiveThread.interrupt();
        
        try {
            socket.close();
        } catch (IOException e) {
            tcpConnectionListener.onException(TCPConnection.this, e);
        }
    }

    public Socket getSocket() {
        return socket;
    }
}
