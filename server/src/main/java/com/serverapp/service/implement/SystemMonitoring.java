package com.serverapp.service.implement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.serverapp.controller.view.MainSystemController;
import com.serverapp.controller.view.MainSystemController;
import com.serverapp.enums.RequestType;
import com.serverapp.socket.TCPServer;
import com.serverapp.util.CurrentType;
import com.serverapp.util.NetworkInfoCollector;
import com.serverapp.model.ClientCard;
import com.serverapp.model.ClientDetail;
import com.serverapp.database.Redis;
import com.serverapp.service.ISystemMonitoring;

public class SystemMonitoring implements ISystemMonitoring {
    private MainSystemController mainSystemController;
    private boolean running;

    private ExecutorService clientHandlerPool;
    private ExecutorService networkScannerPool;
    private ExecutorService mainSystemControllerPool;

    public SystemMonitoring() {
        clientHandlerPool = Executors.newFixedThreadPool(10);
        networkScannerPool = Executors.newSingleThreadExecutor();
        mainSystemControllerPool = Executors.newSingleThreadExecutor();
    }

    @Override
    public void start() {
        running = true;
        mainSystemControllerPool.submit(() -> runServer());
    }

    @Override
    public void setPort(int port) {

    }

    @Override
    public void setMainSystemController(MainSystemController mainSystemController) {
        this.mainSystemController = mainSystemController;
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
                mainSystemController.updateUI();

                long endTime = System.currentTimeMillis();
                // Calculate the elapsed time
                long elapsedTime = endTime - startTime;
                // Print the elapsed time
                logMessage("Execution time in milliseconds: " + elapsedTime);
            });

            while (running && CurrentType.getInstance().getType() == RequestType.SYSTEM_INFO) {
                try {
                    Socket clientSocket = server.getServerSocket().accept();
                    clientSocket.setSoTimeout(30000);  // Timeout after 30 seconds of inactivity
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                    if (CurrentType.getInstance().getType() != RequestType.SYSTEM_INFO)
                        break;
                    else {
                        out.println(CurrentType.getInstance().getType());
                    }

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

                if (inputLine == null || inputLine.isEmpty()) {
                    continue;
                }

                logMessage("Received JSON from client: " + inputLine);
                System.out.println(inputLine);
                // Parse the received JSON using Gson
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(inputLine, JsonObject.class);

                Redis.getInstance().putClientDetail(
                        String.join(
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
                mainSystemController.updateUI();
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
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String logEntry = "[" + timestamp + "] " + message;
        if (mainSystemController != null) {
            mainSystemController.appendLog(logEntry);
        } else {
            System.out.println(logEntry);
        }
    }


    @Override
    public void stop() {
        try {
            logMessage("Stopping server (function Stop in SystemMonitoring)...");
            System.out.println("Stopping server (function Stop in SystemMonitoring)...");
            clientHandlerPool.shutdown();  // Initiates an orderly shutdown
            networkScannerPool.shutdown();
            if (!clientHandlerPool.awaitTermination(60, TimeUnit.SECONDS)) {
                clientHandlerPool.shutdownNow();  // Forces shutdown if not completed within 60 seconds
            }
            if (!networkScannerPool.awaitTermination(60, TimeUnit.SECONDS)) {
                networkScannerPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            clientHandlerPool.shutdownNow();
            networkScannerPool.shutdownNow();
        }
        running = false;
    }
}