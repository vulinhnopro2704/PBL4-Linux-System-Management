package com.serverapp.service.implement;

import com.google.gson.Gson;
import com.serverapp.controller.view.MainCommandController;
import com.serverapp.database.Redis;
import com.serverapp.enums.RequestType;
import com.serverapp.model.ClientCommnandRow;
import com.serverapp.model.ClientCredentials;
import com.serverapp.model.CommandModel;
import com.serverapp.service.IClientCommand;
import com.serverapp.socket.SocketManager;
import com.serverapp.util.CurrentType;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.serverapp.util.AlertHelper.showAlert;

public class ClientCommand implements IClientCommand {
    private final MainCommandController controller;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private boolean isRunning;
    private boolean isWaitingForResponse = false;

    public ClientCommand(MainCommandController controller) {
        this.controller = controller;
    }

    public void initialize() {
        try {
            CurrentType.getInstance().setType(RequestType.COMMAND);
            isRunning = true;
            Platform.runLater(() -> {
                controller.setupTableColumns();

                HashMap<String, ClientCredentials> clients = SocketManager.getInstance().getAllClientCredentials();
                clients.forEach((ip, clientData) -> {
                    controller.addClientToList(ip);
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
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendCommand(String command, TableView<ClientCommnandRow> tableClient, TextArea txtAreaTerminalLogs) throws Exception {
        if (!isRunning) {
            System.out.println("Client Command has stopped");
            return;
        }

        if (isWaitingForResponse) {
            controller.log("Waiting for response from client. Please wait...");
            showAlert(Alert.AlertType.CONFIRMATION, "Waiting for response", "Please wait", "Waiting for response from client. Please wait...");
            return;
        }

        if (command.isEmpty()) return;

        List<ClientCommnandRow> checkedClients = tableClient.getItems().stream()
                .filter(ClientCommnandRow::isCheckbox)
                .toList();

        if (checkedClients.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No client selected", "Please select at least one client", "Please select at least one client to send the command to.");
            return;
        }

        Gson gson = new Gson();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        for (ClientCommnandRow clientRow : checkedClients) {
            String clientAddress = clientRow.getIpAddress();
            controller.log("Sending command to " + clientAddress);

            // Create the CommandModel object
            CommandModel commandModel = new CommandModel();
            commandModel.time = timestamp;
            commandModel.message = command;

            // Serialize CommandModel to JSON
            String jsonCommand = gson.toJson(commandModel);

            // Send the JSON command
            SocketManager.getInstance().sendEncryptedMessage(jsonCommand, clientAddress);

            controller.log("Sent command to " + clientAddress + " Waiting for response...");
            isWaitingForResponse = false;
        }
    }

    public void listenForResponse(Socket clientSocket) {
        try {
            Gson gson = new Gson();

            while (isRunning) {
                String responseJson;
                if (isRunning)
                    responseJson = SocketManager.getInstance().receiveDecryptedMessage(clientSocket.getInetAddress().getHostAddress()).trim();
                else {
                    break;
                }

                if (responseJson == null || responseJson.isEmpty()) {
                    continue;
                }
                CommandModel response = null;
                try {
                    // Deserialize JSON response
                    response = gson.fromJson(responseJson, CommandModel.class);
                }
                catch (Exception e) {
                    System.err.println("Failed to parse command JSON: " + e.getMessage());
                    e.printStackTrace();
                    continue;
                }

                if (response != null && response.message != null && !response.message.trim().isEmpty()) {
                    String timestamp = response.time != null ? response.time : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                    String logEntry = "[" + timestamp + "] " + response.message;
                    Redis.getInstance().appendConsoleLogs(clientSocket.getInetAddress().getHostAddress(), logEntry);
                    controller.updateConsole();
                    isWaitingForResponse = false;
                }
            }
        } catch (IOException e) {
            if (clientSocket.isClosed()) {
                controller.log("(Listen for Response) Client disconnected: " + clientSocket.getInetAddress().getHostAddress());
            } else {
                controller.log("Lost connection to client: " + clientSocket.getInetAddress().getHostAddress());
                e.printStackTrace();
            }
        } catch (Exception e) {
            controller.log("Error receiving response from client: " + clientSocket.getInetAddress().getHostAddress() + " - " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendExitCommand(String ipAddress) {
        try {
            // Tạo một CommandModel với RequestType.EXIT_COMMNAD_SCREEN
            CommandModel command = new CommandModel();
            command.type = RequestType.EXIT_COMMNAD_SCREEN;
            command.time = String.valueOf(System.currentTimeMillis());
            command.message = "Server exit command screen";

            // Parse thành JSON
            String jsonCommand = new Gson().toJson(command);

            // Gửi JSON qua socket
            SocketManager.getInstance().sendEncryptedMessage(jsonCommand, ipAddress);
            System.out.println("Sent exit command to client.");
        } catch (Exception e) {
            System.err.println("Error while sending exit command: " + e.getMessage());
        }
    }

    public void close() {
        isRunning = false;
        isWaitingForResponse = false;
        System.out.println("Close Client Command Screen");
        HashMap<String, ClientCredentials> clients = SocketManager.getInstance().getAllClientCredentials();
        clients.forEach((ip, _) -> {
            try {
                sendExitCommand(ip);
            } catch (Exception e) {
                System.err.println("Error while sending exit command: " + e.getMessage());
            }
        });

        executor.shutdown();
        executor.shutdownNow();
        System.out.println("Closed client command screen");
    }
}
