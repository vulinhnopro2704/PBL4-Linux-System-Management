package com.clientapp.socket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import com.clientapp.enums.RequestType;
import com.clientapp.service.implement.WatchDirectoryClamAVClient;
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
            watchDirectory();
        }
        return _INSTANCE;
    }

    public Socket getClientSocket() {
        return socket;
    }

    public static void watchDirectory() {
        new Thread(() -> {
            WatchDirectoryClamAVClient watchDirectoryClamAVClient = new WatchDirectoryClamAVClient();
            watchDirectoryClamAVClient.run();
        }).start();
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Boolean isAvailableToRead() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        return reader.ready();
    }

    // Method to send encrypted message
    public void sendEncryptedMessage(String message) throws Exception {
        for (RequestType requestType : RequestType.values()) {
            if (requestType.name().equals(message)) {
                System.out.println("Don't send request type by EncryptedMessage!!!");
                return;
            }
        }
        String encryptedMessage = encryptWithAES(message, aesKey);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
        writer.write(encryptedMessage + "\n");
        writer.flush();
    }

    // Method to receive and decrypt message
    public String receiveDecryptedMessage() throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        // Check if the decrypted message is a RequestType value
        String encryptedMessage = reader.readLine();
        for (RequestType requestType : RequestType.values()) {
            if (requestType.name().equals(encryptedMessage)) {
                return requestType.name();
            }
        }
        return decryptWithAES(encryptedMessage, aesKey);
    }

    // Encrypt message with AES
    private String encryptWithAES(String plainText, SecretKey aesKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] iv = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        cipher.init(Cipher.ENCRYPT_MODE, aesKey, ivSpec);
        byte[] encryptedText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        byte[] ivAndEncryptedText = ByteBuffer.allocate(iv.length + encryptedText.length)
                .put(iv)
                .put(encryptedText)
                .array();

        return Base64.getEncoder().encodeToString(ivAndEncryptedText);
    }

    // Decrypt message with AES
    private String decryptWithAES(String encryptedText, SecretKey aesKey) throws Exception {
        System.out.println("Encrypted text: " + encryptedText);
        byte[] ivAndEncryptedText = Base64.getDecoder().decode(encryptedText);

        ByteBuffer byteBuffer = ByteBuffer.wrap(ivAndEncryptedText);
        byte[] iv = new byte[16];
        byteBuffer.get(iv);
        byte[] encryptedBytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(encryptedBytes);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, aesKey, ivSpec);

        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    public void sendExitCommand() {
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
            writer.write(RequestType.EXIT_COMMNAD_SCREEN.name() + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Ham gui khong ma hoa
    // Send message without encryption for testing purposes
    public void sendPlainMessage(String message) {
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
            writer.write(message + "\n");
            writer.flush();
            System.out.println("Sent plain message: " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
