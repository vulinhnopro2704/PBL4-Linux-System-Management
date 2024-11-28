package com.serverapp.service.implement;

import com.serverapp.socket.SocketManager;
import com.serverapp.util.AlertHelper;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class WatchDirectoryClamAVServer {
    private int port;

    public WatchDirectoryClamAVServer(int port) {
        this.port = port;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");

                new Thread(() -> handleClient(socket)).start();
            }
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
        }
    }

    private void handleClient(Socket socket) {
        try (InputStream input = socket.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {

            String message;
            while ((message = reader.readLine()) != null) {
                System.out.println("Received: " + message);
                String messageFound = message;
                String ip = socket.getInetAddress().getHostAddress();
                Platform.runLater(() -> AlertHelper.showAlert(Alert.AlertType.ERROR, "Found Malware", "Found malware in client " + ip, messageFound));
                SocketManager.getInstance().removeClientCredentials(ip);
                Platform.runLater(() ->
                        AlertHelper.showAlert(Alert.AlertType.INFORMATION,
                                "Disconnect " + ip,
                                "Client Disconnected",
                                "Disconnect Client " + ip + "because of malware found"));
            }

        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        }
    }
}
