package com.serverapp;

import java.net.ServerSocket;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        //Port 2567 is just for Test, implementer can change in the future
        try (ServerSocket serverSocket = new ServerSocket(2567)) {
            System.out.println("Server started on port 2567. Waiting for connections...");

            while (true) {
                try {
                    java.net.Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());

                    // Create a new thread to handle the client
                    new Thread(() -> {
                        try (
                            java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(clientSocket.getInputStream()));
                            java.io.PrintWriter out = new java.io.PrintWriter(clientSocket.getOutputStream(), true)
                        ) {
                            String inputLine;
                            while ((inputLine = in.readLine()) != null) {
                                // Assuming the input is in JSON format
                                System.out.println("Received JSON from client: " + inputLine);

                            }
                        } catch (java.io.IOException e) {
                            System.err.println("Error handling client: " + e.getMessage());
                        } finally {
                            try {
                                clientSocket.close();
                            } catch (java.io.IOException e) {
                                System.err.println("Error closing client socket: " + e.getMessage());
                            }
                        }
                    }).start();

                } catch (java.io.IOException e) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                    break;
                }
            }
        }

    }

    public static void main(String[] args) {
        launch(args);
    }
}
