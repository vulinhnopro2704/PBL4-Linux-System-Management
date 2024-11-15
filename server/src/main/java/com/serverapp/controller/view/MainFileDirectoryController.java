package com.serverapp.controller.view;

import com.serverapp.controller.IController;
import com.serverapp.enums.RequestType;
import com.serverapp.socket.SocketManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import java.util.HashMap;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;


import com.serverapp.util.CurrentType;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import com.serverapp.model.ClientCredentials;

import static com.serverapp.util.AlertHelper.showAlert;

import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;

import static com.serverapp.util.AlertHelper.showAlert;

public class MainFileDirectoryController implements IController {
    @FXML
    private ImageView btnUpfile;

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
            CurrentType.getInstance().setType(RequestType.FILE_TRANSFER);
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
                        writer.write(RequestType.FILE_TRANSFER + "\n");
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
    private void sendFile() throws Exception {
        if (!isRunning) {
            System.out.println("Client file transfer has stopped");
            return;
        }

        if (isWaitingForResponse) {
            log("Waiting for response from client. Please wait...");
            showAlert(Alert.AlertType.CONFIRMATION, "Waiting for response", "Please wait", "Waiting for response from client. Please wait...");
            return;
        }

        List<CheckBox> checkedClients = clientList.stream()
                .filter(CheckBox::isSelected)
                .collect(Collectors.toList());

        if (checkedClients.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No client selected", "Please select at least one client", "Please select at least one client to send the command to.");
            return;
        }

        String filePath = "D:\\SEM 5\\XLTHS\\DeCuong_XuLyTinHieuSo_Khoa CNTT.pdf";

        for (CheckBox clientCheckBox : checkedClients) {
            String clientAddress = clientCheckBox.getText();
            log("Sending file to " + clientAddress);

            ClientCredentials clientData = SocketManager.getInstance().getClientCredentials(clientAddress);
            if (clientData == null) {
                log("Client " + clientAddress + " not connected.");
                continue;
            }
            // Send file in a separate thread
            SocketManager.getInstance().sendEncryptedFile(filePath,clientAddress);
        }
    }

    private void log(String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String logEntry = "[" + timestamp + "] " + message;
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


