package com.serverapp.util.implement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.serverapp.controller.MainController;
import com.serverapp.util.ITCPServer;

public class TCPServer implements ITCPServer {
    private int port;
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

                // Initialize variables with default values
                String hostName = "N/A";
                String ipAddress = "N/A";
                String macAddress = "N/A";
                String osInfo = "N/A";

                            // Extract information from the JSON object
                hostName = jsonObject.get("hostName").toString();
                ipAddress = String.valueOf(clientSocket.getInetAddress());
                macAddress = jsonObject.get("macAddress").toString();
                osInfo = jsonObject.get("osVersion").toString();

                // Update the UI with the received information
                if (mainController != null) {
                    mainController.addClientCard(hostName, ipAddress, macAddress, osInfo);
                }
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
            // mainController.appendLog(message);
        } else {
            System.out.println(message);
        }
    }
}