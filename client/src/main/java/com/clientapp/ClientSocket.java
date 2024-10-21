package com.clientapp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

import javax.crypto.SecretKey;

import static com.clientapp.util.implement.EncodeDecoder.decryptCommand;
import static com.clientapp.util.implement.EncodeDecoder.encryptResponse;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientSocket {
    private Socket socket;
    private String serverIp = "localhost";
    private int serverPort = 8080;
    private SecretKey aesKey;
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

    // Encrypt the message and send it to the server
    public void sendByBufferWriter(String message) {
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
            String encryptedMessage = encryptResponse(message, aesKey);
            writer.write(encryptedMessage + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error encrypting message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Receive the message from the server and decrypt it
    public String receiveByBufferReader() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String encryptedCommand = reader.readLine();
            if (encryptedCommand == null || encryptedCommand.isEmpty()) {
                System.err.println("Received null or empty command");
                return "";
            }
            return decryptCommand(encryptedCommand, aesKey);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error decrypting command: " + e.getMessage());
            e.printStackTrace();
        }
        return "";
    }
}
