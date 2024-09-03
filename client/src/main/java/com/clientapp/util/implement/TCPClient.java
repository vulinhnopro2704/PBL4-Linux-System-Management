package com.clientapp.util.implement;

import com.clientapp.util.INetworkClient;
import com.clientapp.util.ISystemInfoCollector;

import java.io.PrintWriter;
import java.net.Socket;

public class TCPClient implements INetworkClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 2567;

    private final ISystemInfoCollector systemInfoCollector;

    public TCPClient(ISystemInfoCollector systemInfoCollector) {
        this.systemInfoCollector = systemInfoCollector;
    }
    //Send system information to test connect with server via TCP
    @Override
    public void sendSystemInfo() {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String jsonSystemInfo = systemInfoCollector.getSystemInfoAsJson();
            out.println(jsonSystemInfo);
            System.out.println("System info sent to server.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}