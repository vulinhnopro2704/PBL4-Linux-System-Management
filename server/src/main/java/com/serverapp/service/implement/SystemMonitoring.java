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
import com.serverapp.socket.SocketManager;
import com.serverapp.socket.TCPServer;
import com.serverapp.util.CurrentType;
import com.serverapp.util.NetworkInfoCollector;
import com.serverapp.model.ClientCard;
import com.serverapp.model.ClientDetail;
import com.serverapp.database.Redis;
import com.serverapp.service.ISystemMonitoring;
import javafx.application.Platform;

public class SystemMonitoring implements ISystemMonitoring {
    private MainSystemController mainSystemController;
    private boolean running;

    private final ExecutorService clientHandlerPool;
    private final ExecutorService networkScannerPool;
    private final ExecutorService mainSystemControllerPool;

    public SystemMonitoring() {
        clientHandlerPool = Executors.newFixedThreadPool(10);
        networkScannerPool = Executors.newSingleThreadExecutor();
        mainSystemControllerPool = Executors.newSingleThreadExecutor();
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
    }

    @Override
    public void start() {
        running = true;
        mainSystemControllerPool.submit(this::setUpConnection);
    }

    @Override
    public void setPort(int port) {

    }

    @Override
    public void setMainSystemController(MainSystemController mainSystemController) {
        this.mainSystemController = mainSystemController;
    }

    public void setUpConnection() {
        SocketManager.getInstance().getAllClientCredentials().forEach((ip, clientCredentials) -> {
            if (!Redis.getInstance().containsIp(ip)) {
                try {
                    PrintWriter writer = new PrintWriter(clientCredentials.getOutputStream(), true);
                    writer.println(CurrentType.getInstance().getType());
                    clientHandlerPool.submit(() -> handleClient(clientCredentials.getSocket()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        Platform.runLater(() -> {
            mainSystemController.updateUI();
        });
    }

    private void handleClient(Socket clientSocket){
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
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
                        clientSocket.getInetAddress().getHostAddress(),
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

                Redis.getInstance().getMapClientDetailView().forEach((key, value) -> {
                    System.out.println(key + " : " + value);
                });

                // Update the UI with the received information
                mainSystemController.updateUI();
                break;
            }
        } catch (JsonSyntaxException e) {
            logMessage("Error parsing JSON: " + e.getMessage());
        } catch (IOException e) {
            logMessage("Error handling client: " + e.getMessage());
        }
    }

    private void logMessage(String message) {
        if (mainSystemController != null) {
            mainSystemController.appendLog(message);
        } else {
            System.out.println(message);
        }
    }


    @Override
    public void stop() {
        logMessage("Stopping server (function Stop in SystemMonitoring)...");
        System.out.println("Stopping server (function Stop in SystemMonitoring)...");

        running = false;  // Set running to false to stop the server loop

        clientHandlerPool.shutdown();  // Initiates an orderly shutdown
        networkScannerPool.shutdown();

        try {
            if (!clientHandlerPool.awaitTermination(30, TimeUnit.MILLISECONDS)) {
                clientHandlerPool.shutdownNow();  // Forces shutdown if not completed within 30 seconds
                if (!clientHandlerPool.awaitTermination(30, TimeUnit.MILLISECONDS)) {
                    System.err.println("ClientHandlerPool did not terminate");
                }
            }

            if (!networkScannerPool.awaitTermination(30, TimeUnit.MILLISECONDS)) {
                networkScannerPool.shutdownNow();
                if (!networkScannerPool.awaitTermination(30, TimeUnit.MILLISECONDS)) {
                    System.err.println("NetworkScannerPool did not terminate");
                }
            }
        } catch (InterruptedException e) {
            clientHandlerPool.shutdownNow();
            networkScannerPool.shutdownNow();
            Thread.currentThread().interrupt();  // Preserve interrupt status
        }
    }
}