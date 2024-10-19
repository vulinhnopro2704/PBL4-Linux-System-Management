package com.serverapp.socket;

import java.io.IOException;
import java.net.ServerSocket;

public class TCPServer {
    private int serverPort = 8080;

    private static TCPServer _instance;
    private ServerSocket serverSocket;
    private boolean isRunning = false;

    private TCPServer() throws IOException {
        serverSocket = new ServerSocket(serverPort);
        isRunning = true;
    }

    public static synchronized TCPServer getInstance() throws IOException {
        if (_instance == null) {
            _instance = new TCPServer();
        }
        return _instance;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public boolean getRunning() {
        return isRunning;
    }

    public int getServerPort() {
        return serverPort;
    }



    public void stop() throws IOException {
        isRunning = false;
        if (serverSocket != null) {
            serverSocket.close();
            isRunning = false;
            _instance = null;
        }
    }
}