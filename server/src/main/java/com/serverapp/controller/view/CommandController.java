package com.serverapp.controller.view;

import com.serverapp.enums.RequestType;
import com.serverapp.model.ClientCredentials;
import com.serverapp.model.Redis;
import com.serverapp.util.implement.TCPServer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import lombok.SneakyThrows;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommandController {

    @FXML
    private ListView<CheckBox> clientListView;
    @FXML
    private TextField commandField;
    @FXML
    private TextArea logArea;

    private TCPServer server;
    private ObservableList<CheckBox> clientList = FXCollections.observableArrayList();
    private ExecutorService executor = Executors.newCachedThreadPool();
    private PublicKey rsaPublicKey;
    private PrivateKey rsaPrivateKey;

    private DataOutputStream dataOutputStream;

    @FXML
    public void initialize() {
        // Khởi tạo cặp khóa RSA
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair pair = keyGen.generateKeyPair();
            rsaPublicKey = pair.getPublic();
            rsaPrivateKey = pair.getPrivate();
            server = TCPServer.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Khởi động server socket
        executor.submit(this::startServerSocket);
    }

    private void startServerSocket() {
        try (ServerSocket serverSocket = server.getServerSocket()) {
            log("Server is running on port 8080...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
                dataOutputStream.writeUTF(RequestType.COMMAND.toString());
                log("New client connected: " + clientSocket.getInetAddress().getHostAddress());

                // Thêm client vào danh sách
                Platform.runLater(() -> {
                    CheckBox clientCheckBox = new CheckBox(clientSocket.getInetAddress().getHostAddress());
                    clientList.add(clientCheckBox);
                    clientListView.setItems(clientList);
                });

                // Xử lý client trên luồng riêng
                executor.submit(() -> handleClient(clientSocket));
            }
        } catch (IOException e) {
            log("Error starting server: " + e.getMessage());
        }
    }

    private void handleClient(Socket clientSocket) {
        try {
            DataInputStream input = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());

            // Gửi khóa công khai RSA cho Client
            String RSAPublicKey = Base64.getEncoder().encodeToString(rsaPublicKey.getEncoded());
            System.out.println(RSAPublicKey);
            output.writeUTF(RSAPublicKey);
            log("Public Key Sent");

            // Nhận khóa AES đã mã hóa từ Client
            String encryptedAesKey = input.readUTF();
            SecretKey aesKey = decryptAESKey(encryptedAesKey);

            // Lưu trữ output stream và AES key để sử dụng sau này khi cần gửi lệnh
            Redis.getInstance().putClientCredential(clientSocket.getInetAddress().getHostAddress(), new ClientCredentials(output, input, aesKey));

            // Lắng nghe phản hồi từ client trong một luồng riêng
            executor.submit(() -> listenForResponse(clientSocket.getInetAddress().getHostAddress(), input, aesKey));

            log("Client is ready: " + clientSocket.getInetAddress().getHostAddress());
        } catch (Exception e) {
            log("Client disconnected: " + clientSocket.getInetAddress().getHostAddress());
        }
    }

    private void listenForResponse(String clientAddress, DataInputStream input, SecretKey aesKey) {
        try {
            while (true) {
                // Nhận phản hồi được mã hóa từ client
                String encryptedResponse = input.readUTF();
                // Giải mã phản hồi
                String response = decryptResponse(encryptedResponse, aesKey);
                log("Response from client (" + clientAddress + "): " + response);
            }
        } catch (IOException e) {
            log("Lost connection to client: " + clientAddress);
        } catch (Exception e) {
            log("Error receiving response from client: " + clientAddress + " - " + e.getMessage());
        }
    }


    @FXML
    private void sendCommand() {
        String command = commandField.getText();
        if (command.isEmpty()) return;

        for (CheckBox clientCheckBox : clientList) {
            if (clientCheckBox.isSelected()) {
                String clientAddress = clientCheckBox.getText();
                log("Sending command to " + clientAddress);

                // Lấy dữ liệu client từ Map
                ClientCredentials clientData = Redis.getInstance().getClientCredential(clientAddress);
                if (clientData != null) {
                    try {
                        // Mã hóa và gửi lệnh
                        String encryptedCommand = encryptCommand(command, clientData.aesKey);
                        clientData.outputStream.writeUTF(encryptedCommand);
                    } catch (Exception e) {
                        log("Failed to send command to " + clientAddress);
                    }
                }
            }
        }
    }

    private String encryptCommand(String command, SecretKey aesKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        byte[] encrypted = cipher.doFinal(command.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    private String decryptResponse(String encryptedResponse, SecretKey aesKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, aesKey);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedResponse);
        return new String(cipher.doFinal(decodedBytes));
    }

    private SecretKey decryptAESKey(String encryptedKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, rsaPrivateKey);
        byte[] decodedKey = Base64.getDecoder().decode(encryptedKey);
        byte[] aesKey = cipher.doFinal(decodedKey);
        return new SecretKeySpec(aesKey, "AES");
    }

    private void log(String message) {
        Platform.runLater(() -> logArea.appendText(message + "\n"));
    }
}
