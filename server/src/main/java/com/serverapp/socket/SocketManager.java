package com.serverapp.socket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.serverapp.controller.view.AppController;
import com.serverapp.enums.RequestType;
import com.serverapp.helper.EnCodeDecoder;
import com.serverapp.model.ClientCredentials;

import javafx.application.Platform;
import lombok.Getter;


public class SocketManager {
    private static SocketManager _INSTANCE;
    private HashMap<String, ClientCredentials> socketMap;  // Key: Địa chỉ IP, Value: Socket
    private PublicKey rsaPublicKey;
    private PrivateKey rsaPrivateKey;
    @Getter
    private ServerSocket serverSocket;
    @Getter
    private final int port = 8080;
    private ExecutorService executorService = Executors.newCachedThreadPool();

    // Phương thức để lấy thể hiện duy nhất của Singleton
    public static synchronized SocketManager getInstance() {
        if (_INSTANCE == null) {
            _INSTANCE = new SocketManager();
        }
        return _INSTANCE;
    }

    // Private constructor để ngăn chặn việc khởi tạo từ bên ngoài
    private SocketManager() {
        socketMap = new HashMap<>();
        generateRSAKeys();
        if (serverSocket == null) {
            try {
                serverSocket = new ServerSocket(port);
                startListening();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Tạo cặp khóa RSA
    private void generateRSAKeys() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair pair = keyGen.generateKeyPair();
            this.rsaPublicKey = pair.getPublic();
            this.rsaPrivateKey = pair.getPrivate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startListening() {
        new Thread(() -> {
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    executorService.submit(() -> handleClientConnection(clientSocket));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void handleClientConnection(Socket clientSocket) {
        try {
            System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

            // Send COMMAND request to client
            writer.write(RequestType.CONNECTION + "\n");
            writer.flush();

            // Send RSA public key to client
            String RSAPublicKey = Base64.getEncoder().encodeToString(this.rsaPublicKey.getEncoded());
            writer.write(RSAPublicKey + "\n");
            writer.flush();
            System.out.println("Public Key Sent");

            // Receive encrypted AES key from the client
            String encryptedAesKey = reader.readLine();
            if (encryptedAesKey != null) {
                try {
                    SecretKey aesKey = decryptAESKey(encryptedAesKey);
                    String ip = clientSocket.getInetAddress().getHostAddress();
                    System.out.println("Client is ready: " + clientSocket.getInetAddress().getHostAddress());
                    addClientCredentials(ip, new ClientCredentials(clientSocket, aesKey));
                } catch (Exception e) {
                    System.out.println("Error decrypting AES key: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("Failed to receive AES key from client: " + clientSocket.getInetAddress().getHostAddress());
            }
        } catch (Exception e) {
            System.out.println("(Handle Client) Client disconnected: " + clientSocket.getInetAddress().getHostAddress() + " - " + e.getMessage());
            e.printStackTrace();
        }
        Platform.runLater(() -> {
            AppController.getInstance().update();
        });
    }

    // Thêm Socket vào HashMap với Key là địa chỉ IP
    public synchronized void addClientCredentials(String ip, ClientCredentials clientCredentials) {
        socketMap.put(ip, clientCredentials);
    }

    // Xóa Socket khỏi HashMap dựa vào địa chỉ IP
    public synchronized void removeClientCredentials(String ip) {
        socketMap.remove(ip);
    }

    // Lấy Socket dựa vào địa chỉ IP
    public synchronized ClientCredentials getClientCredentials(String ip) {
        return socketMap.get(ip);
    }

    // Lấy tất cả các Socket hiện tại (nếu cần)
    public synchronized HashMap<String, ClientCredentials> getAllClientCredentials() {
        return new HashMap<>(socketMap); // Trả về bản sao của HashMap để tránh sửa đổi ngoài ý muốn
    }

    // Kiểm tra xem một địa chỉ IP có đang kết nối không
    public synchronized boolean containsClientCredentials(String ip) {
        return socketMap.containsKey(ip);
    }

    // Mã hóa gói tin trước khi gửi (ví dụ: AES)
    public byte[] encryptMessage(String message, SecretKey aesKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        return cipher.doFinal(message.getBytes());
    }

    // Giải mã gói tin sau khi nhận
    public String decryptMessage(byte[] encryptedMessage, SecretKey aesKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, aesKey);
        return new String(cipher.doFinal(encryptedMessage));
    }

    public String encryptCommand(String command, SecretKey aesKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        byte[] encrypted = cipher.doFinal(command.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public String decryptResponse(String encryptedResponse, SecretKey aesKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, aesKey);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedResponse);
        return new String(cipher.doFinal(decodedBytes));
    }

    public SecretKey decryptAESKey(String encryptedKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, rsaPrivateKey);
        byte[] decodedKey = Base64.getDecoder().decode(encryptedKey);
        System.out.println("Decoded Key Length: " + decodedKey.length); // Log the length
        byte[] aesKey = cipher.doFinal(decodedKey);
        return new SecretKeySpec(aesKey, "AES");
    }

    public void stop() throws IOException {
        if (serverSocket != null) {
            serverSocket.close();
            _INSTANCE = null;
        }
    }
}
