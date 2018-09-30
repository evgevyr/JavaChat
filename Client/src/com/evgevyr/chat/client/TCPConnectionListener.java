package com.evgevyr.chat.client;

public interface TCPConnectionListener {
    void onConnection(TCPConnection tcpConnection);
    void onReceive(TCPConnection tcpConnection, String string);
    void onDisconnect(TCPConnection tcpConnection);
    void onException(TCPConnection tcpConnection, Exception e);
}
