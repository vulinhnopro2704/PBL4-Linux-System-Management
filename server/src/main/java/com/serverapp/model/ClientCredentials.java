package com.serverapp.model;

import javax.crypto.SecretKey;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class ClientCredentials {
    public DataOutputStream outputStream;
    DataInputStream inputStream;
    public SecretKey aesKey;

    public ClientCredentials(DataOutputStream outputStream, DataInputStream inputStream, SecretKey aesKey) {
        this.outputStream = outputStream;
        this.inputStream = inputStream;
        this.aesKey = aesKey;
    }
}
