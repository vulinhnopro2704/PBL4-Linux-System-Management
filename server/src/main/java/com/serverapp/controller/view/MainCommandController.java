package com.serverapp.controller.view;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


import com.serverapp.controller.IController;
import com.serverapp.enums.RequestType;
import com.serverapp.model.ClientCredentials;
import com.serverapp.socket.SocketManager;

import static com.serverapp.util.AlertHelper.showAlert;

import com.serverapp.util.CurrentType;
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
    private Boolean isRunning;

    @FXML
    public void initialize() {
        try {
            CurrentType.getInstance().setType(RequestType.COMMAND);
            isRunning = true;
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
                    executor.submit(() -> listenForResponse(clientData.getSocket()));
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

    private void listenForResponse(Socket clientSocket) {
        try {
            while (isRunning) {
                // Receive encrypted response from client
                String response;
                if (isRunning)
                    response = SocketManager.getInstance().receiveDecryptedMessage(clientSocket.getInetAddress().getHostAddress());
                else {
                    break;
                }
                if (response == null || response.isEmpty()) {
                    continue;
                }
                log("Response from client (" + clientSocket.getInetAddress().getHostAddress() + "): " + response);
                isWaitingForResponse = false;
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
    private void sendCommand() throws Exception {
        if (!isRunning) {
            System.out.println("Client Command has stopped");
            return;
        }

        if (isWaitingForResponse) {
            log("Waiting for response from client. Please wait...");
            showAlert(Alert.AlertType.CONFIRMATION, "Waiting for response", "Please wait", "Waiting for response from client. Please wait...");
            return;
        }
        String command = txtAreaCommand.getText().trim();
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
                // Mã hóa và gửi lệnh
                log("Sending command to " + clientAddress);
                SocketManager.getInstance().sendEncryptedMessage(command, clientAddress);
                log("Sent command to " + clientAddress + " Waiting for response...");
                isWaitingForResponse = true;
        }
    }

    private void log(String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String logEntry = "[" + timestamp + "] " + message;
        Platform.runLater(() -> txtAreaTerminalLogs.appendText(logEntry + "\n"));
    }


    void close() {
        System.out.println("Close Client Command Screen");
        HashMap<String, ClientCredentials> clients = SocketManager.getInstance().getAllClientCredentials();
        clients.forEach((ip, clientData) -> {
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new OutputStreamWriter(clientData.getOutputStream()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                writer.write(RequestType.EXIT_COMMNAD_SCREEN + "\n");
                writer.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        isRunning = false;
        isWaitingForResponse = false;
        executor.shutdown();
        executor.shutdownNow();
        System.out.println("Closed client command screen");
    }
}
