package com.serverapp.controller.view;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import com.serverapp.controller.IController;
import com.serverapp.database.Redis;
import com.serverapp.enums.RequestType;
import com.serverapp.helper.EnCodeDecoder;
import com.serverapp.model.ClientCredentials;
import com.serverapp.socket.TCPServer;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import static com.serverapp.util.AlertHelper.showAlert;

public class MainCommandController implements IController {

    @FXML
    private ListView<CheckBox> clientListView;

    @FXML
    private TextArea txtAreaCommand;

    @FXML
    private TableView<CheckBox> tableClient;

    @FXML
    private TextArea txtAreaTerminalLogs;

    private boolean isWaitingForResponse = false;


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

    @Override
    public void stop() {
        close();
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
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            
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
            if (encryptedAesKey != null) {
                SecretKey aesKey = EnCodeDecoder.getInstance().decryptAESKey(encryptedAesKey);

                // Store output stream and AES key for later use
                String ip = clientSocket.getInetAddress().getHostAddress();
                Redis.getInstance().putClientCredential(ip, new ClientCredentials(clientSocket.getInputStream(), clientSocket.getOutputStream(), aesKey));

                // Listen for responses from the client in a separate thread
                executor.submit(() -> listenForResponse(clientSocket, aesKey));

                log("Client is ready: " + clientSocket.getInetAddress().getHostAddress());
            } else {
                log("Failed to receive AES key from client: " + clientSocket.getInetAddress().getHostAddress());
            }
        } catch (Exception e) {
            log("(Handle Client) Client disconnected: " + clientSocket.getInetAddress().getHostAddress() + " - " + e.getMessage());
        }
    }

    private void listenForResponse(Socket clientSocket, SecretKey aesKey) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            while (!clientSocket.isClosed()) {
                // Receive encrypted response from client
                String encryptedResponse = reader.readLine();
                if (encryptedResponse != null) {
                    String response = EnCodeDecoder.getInstance().decryptResponse(encryptedResponse, aesKey);
                    log("Response from client (" + clientSocket.getInetAddress().getHostAddress() + "): " + response);
                    isWaitingForResponse = false;
                } else {
                    log("Client closed connection: " + clientSocket.getInetAddress().getHostAddress());
                    break;
                }
            }
        } catch (IOException e) {
            if (clientSocket.isClosed()) {
                log("(Listen for Response) Client disconnected: " + clientSocket.getInetAddress().getHostAddress());
            } else {
                log("Lost connection to client: " + clientSocket.getInetAddress().getHostAddress());
                e.printStackTrace();
            }
        } catch (Exception e) {
            log("Error receiving response from client: " + clientSocket.getInetAddress().getHostAddress() + " - " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void sendCommand() {
        if (isWaitingForResponse) {
            log("Waiting for response from client. Please wait...");
            showAlert(Alert.AlertType.CONFIRMATION, "Waiting for response", "Please wait", "Waiting for response from client. Please wait...");
            return;
        }
        String command = txtAreaCommand.getText();
        if (command.isEmpty()) return;
        List<CheckBox> checkedClients = clientList.stream()
                .filter(CheckBox::isSelected)
                .collect(Collectors.toList());

        if (checkedClients.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No client selected", "Please select at least one client", "Please select at least one client to send the command to.");
            return;
        }

        Redis.getInstance().getAllIpClientCredential().forEach(System.out::println);

        for (CheckBox clientCheckBox : clientList) {
            if (clientCheckBox.isSelected()) {
                String clientAddress = clientCheckBox.getText();

                // Lấy dữ liệu client từ Map
                ClientCredentials clientData = Redis.getInstance().getClientCredential(clientAddress);


                if (clientData != null) {
                    try {
                        // Mã hóa và gửi lệnh
                        log("Sending command to " + clientAddress);
                        String encryptedCommand = EnCodeDecoder.getInstance().encryptCommand(command, clientData.getAesKey());
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientData.getOutputStream()));
                        writer.write(encryptedCommand + "\n");
                        writer.flush();
                        log("Sent command to " + clientAddress + " Waiting for response...");
                        isWaitingForResponse = true;
                    } catch (Exception e) {
                        log("Failed to send command to " + clientAddress);
                        log(e.getMessage());
                        e.printStackTrace();
                    }
                }
                else {
                    log("Client not found: " + clientAddress);
                }
            }
        }
    }

    private void log(String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String logEntry = "[" + timestamp + "] " + message;
        Platform.runLater(() -> txtAreaTerminalLogs.appendText(logEntry + "\n"));
    }


    void close() {
        executor.shutdown();
        try {
            server.getServerSocket().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
