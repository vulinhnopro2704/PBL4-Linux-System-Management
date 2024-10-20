package com.serverapp.model;

import lombok.Getter;
import lombok.Setter;

import javax.crypto.SecretKey;
import java.io.*;
import java.net.Socket;

@Getter
@Setter
public class ClientCredentials {
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private final SecretKey aesKey;

    public ClientCredentials(InputStream inputStream, OutputStream outputStream, SecretKey aesKey) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.aesKey = aesKey;
    }
}
