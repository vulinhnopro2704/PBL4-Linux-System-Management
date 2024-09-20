package com.clientapp.util.implement;

import com.clientapp.util.INetworkClient;
import com.clientapp.util.ISystemInfoCollector;
import com.google.gson.Gson;

import java.io.PrintWriter;
import java.net.Socket;

public class TCPClient implements INetworkClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 9999;
    private final Gson gson = new Gson();
    private final ISystemInfoCollector systemInfoCollector;
    private Socket socket;
    private PrintWriter out;

    public TCPClient(ISystemInfoCollector systemInfoCollector) {
        this.systemInfoCollector = systemInfoCollector;
        try {
            this.socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            this.out = new PrintWriter(socket.getOutputStream(), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Send system information to test connect with server via TCP
    @Override
    public void sendSystemInfo() {
        try {
            String jsonClientDetail = gson.toJson(systemInfoCollector.getClientDetail());

            // Check Information
            System.out.println(jsonClientDetail);

            out.println(jsonClientDetail);
            System.out.println("System info sent to server.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Close the socket connection
    public void closeConnection() {
        try {
            if (out != null) {
                out.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}