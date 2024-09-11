package com.serverapp.util.implement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.serverapp.controller.MainController;
import com.serverapp.model.ClientCard;
import com.serverapp.model.ClientDetail;
import com.serverapp.model.Redis;
import com.serverapp.util.INetworkInfoCollector;
import com.serverapp.util.ITCPServer;

public class TCPServer implements ITCPServer {
    private int port = 2567;
    private MainController mainController;
    private ServerSocket serverSocket;
    private boolean running;

    @Override
    public void start() {
        running = true;
        new Thread(this::runServer).start();
    }

    @Override
    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            logMessage("Error closing server socket: " + e.getMessage());
        }
    }

    @Override
    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    private void runServer() {
        try {
            serverSocket = new ServerSocket(port);
            logMessage("Server started on port " + port + ". Waiting for connections...");

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    logMessage("New client connected: " + clientSocket.getInetAddress().getHostAddress());

                    // Create a new thread to handle the client
                    new Thread(() -> handleClient(clientSocket)).start();

                } catch (IOException e) {
                    if (running) {
                        logMessage("Error accepting client connection: " + e.getMessage());
                    }
                    break;
                }
            }
            INetworkInfoCollector networkInfoCollector = new NetworkInfoCollector();
            List<ClientCard> list = networkInfoCollector.getAllClientCardsInLAN();
            Redis.getInstance().putAllClientCard(list);
            mainController.updateUI();
        } catch (IOException e) {
            logMessage("Error starting server: " + e.getMessage());
        }
    }

    private void handleClient(Socket clientSocket) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                logMessage("Received JSON from client: " + inputLine);
                System.out.println(inputLine);

                // Parse the received JSON using Gson
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(inputLine, JsonObject.class);

                Redis.getInstance().putClientDetail(
                        String.join(":",
                            clientSocket.getInetAddress().toString(),
                            Integer.toString(clientSocket.getPort())
                        ),
                        new ClientDetail().builder()
                        .hostName(jsonObject.get("hostName").getAsString())
                        .ipAddress(clientSocket.getInetAddress().toString())
                        .macAddress(jsonObject.get("macAddress").getAsString())
                        .ram(Long.valueOf(jsonObject.get("ram").toString()))
                        .cpuModel(jsonObject.get("cpuModel").toString())
                        .osVersion(jsonObject.get("osVersion").getAsString())
                        .isConnect(true)
                        .usedDisk(jsonObject.get("usedDisk").getAsLong())
                        .totalDisk(jsonObject.get("totalDisk").getAsLong())
                        .build()
                );

                // Update the UI with the received information
                mainController.updateUI();
            }
        } catch (JsonSyntaxException e) {
            logMessage("Error parsing JSON: " + e.getMessage());
        } catch (IOException e) {
            logMessage("Error handling client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                logMessage("Error closing client socket: " + e.getMessage());
            }
        }
    }

    private void logMessage(String message) {
        if (mainController != null) {
             mainController.appendLog(message);
        } else {
            System.out.println(message);
        }
    }
}