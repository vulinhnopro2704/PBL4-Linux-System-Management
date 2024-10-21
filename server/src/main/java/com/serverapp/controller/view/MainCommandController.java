package com.serverapp.controller.view;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import com.serverapp.controller.IController;
import com.serverapp.enums.RequestType;
import com.serverapp.model.ClientCredentials;
import com.serverapp.socket.SocketManager;
import com.serverapp.socket.TCPServer;
import static com.serverapp.util.AlertHelper.showAlert;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;

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

    private ObservableList<CheckBox> clientList = FXCollections.observableArrayList();
    private ExecutorService executor = Executors.newCachedThreadPool();

    @FXML
    public void initialize() {
        try {
            Platform.runLater(() -> {
                HashMap<String, ClientCredentials> clients = SocketManager.getInstance().getAllClientCredentials();
                clients.forEach((ip, clientData) -> {
                    CheckBox clientCheckBox = new CheckBox(ip);
                    clientList.add(clientCheckBox);
                    // Listen for responses from the client in a separate thread
                    BufferedWriter writer = null;
                    try {
                        writer = new BufferedWriter(new OutputStreamWriter(clientData.getOutputStream()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        writer.write(RequestType.COMMAND + "\n");
                        writer.flush();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    executor.submit(() -> listenForResponse(clientData.getSocket(), clientData.getAesKey()));
                });
                clientListView.setItems(clientList);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update() {

    }

    @Override
    public void stop() {
        close();
    }

    private void listenForResponse(Socket clientSocket, SecretKey aesKey) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            while (!clientSocket.isClosed()) {
                // Receive encrypted response from client
                String encryptedResponse = reader.readLine();
                if (encryptedResponse != null) {
                    String response = SocketManager.getInstance().decryptResponse(encryptedResponse, aesKey);
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

        for (CheckBox clientCheckBox : checkedClients) {
                String clientAddress = clientCheckBox.getText();

                ClientCredentials clientCredentials = SocketManager.getInstance().getClientCredentials(clientAddress);

                if (clientCredentials != null) {
                    try {
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientCredentials.getOutputStream()));
                        writer.write(RequestType.COMMAND + "\n");
                        // Mã hóa và gửi lệnh
                        log("Sending command to " + clientAddress);
                        String encryptedCommand = SocketManager.getInstance().encryptCommand(command, clientCredentials.getAesKey());
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

    private void log(String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String logEntry = "[" + timestamp + "] " + message;
        Platform.runLater(() -> txtAreaTerminalLogs.appendText(logEntry + "\n"));
    }


    void close() {
        executor.shutdown();
    }
}
