package com.serverapp.model;

import com.serverapp.controller.MainController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatModel {

    private static ServerSocket serverSocket;
    private static Socket clientSocket;
    private static PrintWriter out;
    private static BufferedReader in;
    private static MainController mainController;

    public static void startServer(MainController controller) {
        mainController = controller;
        try {
            serverSocket = new ServerSocket(8080);
            System.out.println("Server started, waiting for a client...");
            clientSocket = serverSocket.accept();
            System.out.println("Client connected");

            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // Start a new thread to handle incoming messages from the client
            new Thread(() -> handleClientMessages()).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendToClient(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    private static void handleClientMessages() {
        String clientMessage;
        try {
            while ((clientMessage = in.readLine()) != null) {
                System.out.println("Received from client: " + clientMessage);
                // Pass the message to the UI
                mainController.receiveMessage(clientMessage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
