package com.serverapp.model;

import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class ClientCredentials {
    public BufferedWriter bufferedWriter;
    public BufferedReader bufferedReader;
    public SecretKey aesKey;

    public ClientCredentials(BufferedWriter bufferedWriter, BufferedReader bufferedReader, SecretKey aesKey) {
        this.bufferedWriter = bufferedWriter;
        this.bufferedReader = bufferedReader;
        this.aesKey = aesKey;
    }
}
