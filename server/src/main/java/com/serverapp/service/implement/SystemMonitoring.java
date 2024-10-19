package com.serverapp.service.implement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.serverapp.controller.view.MainController;
import com.serverapp.enums.RequestType;
import com.serverapp.socket.TCPServer;
import com.serverapp.util.CurrentType;
import com.serverapp.util.NetworkInfoCollector;
import com.serverapp.model.ClientCard;
import com.serverapp.model.ClientDetail;
import com.serverapp.database.Redis;
import com.serverapp.service.ISystemMonitoring;

public class SystemMonitoring implements ISystemMonitoring {
    private MainController mainController;
    private boolean running;

    private ExecutorService clientHandlerPool;
    private ExecutorService networkScannerPool;

    public SystemMonitoring() {
        clientHandlerPool = Executors.newFixedThreadPool(10); // Create a thread pool for client handling
        networkScannerPool = Executors.newSingleThreadExecutor(); // Single thread for network scanning
        start();
    }

    @Override
    public void start() {
        running = true;
        new Thread(this::runServer).start();
    }

    @Override
    public void stop() {

    }

    @Override
    public void setPort(int port) {

    }

    @Override
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    private void runServer() {
        try {
            TCPServer server = TCPServer.getInstance();
            logMessage("Server started on port " + "8080" + ". Waiting for connections...");

            // Start network scanning in a separate thread
            networkScannerPool.submit(() -> {
                long startTime = System.currentTimeMillis();

                NetworkInfoCollector networkInfoCollector = new NetworkInfoCollector();
                List<ClientCard> list = networkInfoCollector.getAllClientCardsInLAN();
                Redis.getInstance().putAllClientCard(list);
                mainController.updateUI();

                long endTime = System.currentTimeMillis();
                // Calculate the elapsed time
                long elapsedTime = endTime - startTime;
                // Print the elapsed time
                logMessage("Execution time in milliseconds: " + elapsedTime);
            });

            while (running) {
                try {
                    Socket clientSocket = server.getServerSocket().accept();
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    out.println(RequestType.SYSTEM_INFO);

                    logMessage("New client connected: " + clientSocket.getInetAddress().getHostAddress());

                    // Create a new thread to handle the client
                    clientHandlerPool.submit(() -> handleClient(clientSocket));

                } catch (IOException e) {
                    if (running) {
                        logMessage("Error accepting client connection: " + e.getMessage());
                    }
                    break;
                }
            }
        } catch (IOException e) {
            logMessage("Error starting server: " + e.getMessage());
        }
    }

    private void handleClient(Socket clientSocket) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        ) {
            String inputLine;
            while (CurrentType.getInstance().getType() == RequestType.SYSTEM_INFO) {
                inputLine = in.readLine();
                try {
                    RequestType requestType = RequestType.valueOf(inputLine);
                    if (requestType != RequestType.SYSTEM_INFO) {
                        close();
                        break;
                    }
                } catch (IllegalArgumentException e) {
                    // It's not a RequestType, so it must be the JSON data
                }

                logMessage("Received JSON from client: " + inputLine);
                System.out.println(inputLine);
                // Parse the received JSON using Gson
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(inputLine, JsonObject.class);

                Redis.getInstance().putClientDetail(
                        String.join(":",
                            clientSocket.getInetAddress().toString()
                        ),
                        new ClientDetail().builder()
                        .hostName(jsonObject.get("hostName").getAsString())
                        .ipAddress(clientSocket.getInetAddress().toString().substring(1))
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

    void close() {
        running = false;
        clientHandlerPool.shutdown();
        networkScannerPool.shutdown();
    }


}