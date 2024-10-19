package com.clientapp;

import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.net.Socket;

@Getter
@Setter
public class ClientSocket {
    private Socket socket;
    private String serverIp = "localhost";
    private int serverPort = 8080;
    private String aesKey;
    private InputStream inputStream;
    private OutputStream outputStream;

    private static ClientSocket _INSTANCE;

    private ClientSocket() {
        try {
            socket = new Socket(serverIp, serverPort);
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ClientSocket getInstance() {
        if (_INSTANCE == null) {
            _INSTANCE = new ClientSocket();
        }
        return _INSTANCE;
    }

    public Socket getClientSocket() {
        return socket;
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
