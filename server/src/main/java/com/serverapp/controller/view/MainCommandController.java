package com.serverapp.controller.view;

import com.serverapp.enums.RequestType;
import com.serverapp.helper.EnCodeDecoder;
import com.serverapp.model.ClientCredentials;
import com.serverapp.database.Redis;
import com.serverapp.socket.TCPServer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainCommandController {

    @FXML
    private ListView<CheckBox> clientListView;

    @FXML
    private TextArea txtAreaCommand;

    @FXML
    private TableView<CheckBox> tableClient;

    @FXML
    private TextArea txtAreaTerminalLogs;


    private TCPServer server;
    private ObservableList<CheckBox> clientList = FXCollections.observableArrayList();
    private ExecutorService executor = Executors.newCachedThreadPool();

    @FXML
    public void initialize() {
        try {
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
                log("New client connected: " + clientSocket.getInetAddress().getHostAddress());
                // Add the client to the list in the GUI
                Platform.runLater(() -> {
                    CheckBox clientCheckBox = new CheckBox(clientSocket.getInetAddress().getHostAddress());
                    clientList.add(clientCheckBox);
                    clientListView.setItems(clientList);
                });

                // Handle client in a separate thread
                executor.submit(() -> handleClient(clientSocket));
            }
        } catch (IOException e) {
            log("Error starting server: " + e.getMessage());
        }
    }

    private void handleClient(Socket clientSocket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))) {
            // Send COMMAND request to client
            writer.write(RequestType.COMMAND + "\n");
            writer.flush();
            // Send RSA public key to client
            String RSAPublicKey = Base64.getEncoder().encodeToString(EnCodeDecoder.getInstance().getRsaPublicKey().getEncoded());
            writer.write(RSAPublicKey + "\n");
            writer.flush();
            log("Public Key Sent");

            // Receive encrypted AES key from the client
            String encryptedAesKey = reader.readLine();
            SecretKey aesKey = EnCodeDecoder.getInstance().decryptAESKey(encryptedAesKey);

            // Store output stream and AES key for later use
            String ip = clientSocket.getInetAddress().getHostAddress();
            Redis.getInstance().putClientCredential(ip, new ClientCredentials(writer, reader, aesKey));

            // Listen for responses from the client in a separate thread
            executor.submit(() -> listenForResponse(clientSocket.getInetAddress().getHostAddress(), reader, aesKey));

            log("Client is ready: " + clientSocket.getInetAddress().getHostAddress());
        } catch (Exception e) {
            log("Client disconnected: " + clientSocket.getInetAddress().getHostAddress() + " - " + e.getMessage());
        }
    }

    private void listenForResponse(String clientAddress, BufferedReader reader, SecretKey aesKey) {
        try {
            while (true) {
                // Receive encrypted response from client
                String encryptedResponse = reader.readLine();
                if (encryptedResponse != null) {
                    String response = EnCodeDecoder.getInstance().decryptResponse(encryptedResponse, aesKey);
                    log("Response from client (" + clientAddress + "): " + response);
                }
            }
        } catch (IOException e) {
            log("Lost connection to client: " + clientAddress);
        } catch (Exception e) {
            log("Error receiving response from client: " + clientAddress + " - " + e.getMessage());
        }
    }

    @FXML
    private void sendCommand() {
        String command = txtAreaCommand.getText();
        if (command.isEmpty()) return;

        Redis.getInstance().getAllIpClientCredential().forEach(System.out::println);

        for (CheckBox clientCheckBox : clientList) {
            if (clientCheckBox.isSelected()) {
                String clientAddress = clientCheckBox.getText();
                log("Sending command to " + clientAddress);

                // Lấy dữ liệu client từ Map
                ClientCredentials clientData = Redis.getInstance().getClientCredential(clientAddress);

                if (clientData != null) {
                    try {
                        // Mã hóa và gửi lệnh
                        String encryptedCommand = EnCodeDecoder.getInstance().encryptCommand(command, clientData.aesKey);
                        clientData.bufferedWriter.write(encryptedCommand);
                    } catch (Exception e) {
                        log("Failed to send command to " + clientAddress);
                    }
                }
            }
        }
    }

    private void log(String message) {
        Platform.runLater(() -> txtAreaTerminalLogs.appendText(message + "\n"));
    }
}
