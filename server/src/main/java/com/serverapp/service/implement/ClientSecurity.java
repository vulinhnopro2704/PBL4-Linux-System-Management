package com.serverapp.service.implement;

import com.serverapp.controller.view.MainSecurityController;
import com.serverapp.database.Redis;
import com.serverapp.enums.RequestType;
import com.serverapp.model.ClientCard;
import com.serverapp.model.ClientCredentials;
import com.serverapp.model.ClientFirewallRow;
import com.serverapp.service.IClientSecurity;
import com.serverapp.socket.SocketManager;
import com.serverapp.util.CurrentType;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
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
import java.util.stream.Collectors;

import static com.serverapp.util.AlertHelper.showAlert;

public class ClientSecurity implements IClientSecurity {
    private final MainSecurityController controller;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private boolean isRunning;
    private boolean isWaitingForResponse = false;

    public ClientSecurity(MainSecurityController controller) {
        this.controller = controller;
    }

    public void initialize() {
        try {
            CurrentType.getInstance().setType(RequestType.SECURITY);
            isRunning = true;
            Platform.runLater(() -> {
                List<ClientCard> clientCards = Redis.getInstance().getAllClientCard();
                ObservableList<ClientFirewallRow> data = FXCollections.observableArrayList(
                        clientCards.stream().map(clientCard -> new ClientFirewallRow(
                                false,
                                clientCard.getHostName(),
                                clientCard.getIpAddress(),
                                clientCard.getMacAddress()
                        )).collect(Collectors.toList())
                );

                data.add(new ClientFirewallRow(false, "DummyHost1", "192.168.0.101", "00:0a:95:9d:68:16"));
                data.add(new ClientFirewallRow(false, "DummyHost2", "192.168.0.102", "00:0a:95:9d:68:17"));

                controller.setupTableColumns(data);

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
                        writer.write(RequestType.SECURITY + "\n");
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

    @Override
    public void sendCommand(String command, ClientFirewallRow clientFirewallRow, TextArea txtAreaTerminalLogs) throws Exception {
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

        String clientAddress = clientFirewallRow.getIpAddress();
        controller.log("Sending command to " + clientAddress);
        SocketManager.getInstance().sendEncryptedMessage(command, clientAddress);
        controller.log("Sent command to " + clientAddress + " Waiting for response...");
        isWaitingForResponse = true;
    }

    public void listenForResponse(Socket clientSocket) {
        try {
            while (isRunning) {
                String response;
                if (isRunning)
                    response = SocketManager.getInstance().receiveDecryptedMessage(clientSocket.getInetAddress().getHostAddress());
                else {
                    break;
                }
                if (response == null || response.isEmpty()) {
                    continue;
                }
                if (!response.trim().isEmpty()) {
                    String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                    String logEntry = "[" + timestamp + "] " + response;
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

    public void close() {
        System.out.println("Close Client Security Screen");
        HashMap<String, ClientCredentials> clients = SocketManager.getInstance().getAllClientCredentials();
        clients.forEach((ip, clientData) -> {
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new OutputStreamWriter(clientData.getOutputStream()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                writer.write(RequestType.EXIT_SECURITY_SCREEN + "\n");
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
