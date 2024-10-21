package com.serverapp.model;

import lombok.Getter;
import lombok.Setter;

import javax.crypto.SecretKey;
import java.io.*;
import java.net.Socket;

@Getter
@Setter
public class ClientCredentials {
    private final Socket socket;
    private final SecretKey aesKey;

    public ClientCredentials(Socket socket, SecretKey aesKey) {
        this.socket = socket;
        this.aesKey = aesKey;
    }

    public InputStream getInputStream() throws IOException {
        return this.socket.getInputStream();
    }

    public OutputStream getOutputStream() throws IOException {
        return this.socket.getOutputStream();
    }

    public String getHostAddress() {
        return this.socket.getInetAddress().getHostAddress();
    }
}
