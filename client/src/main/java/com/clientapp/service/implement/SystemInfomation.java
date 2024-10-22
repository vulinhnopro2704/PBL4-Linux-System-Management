package com.clientapp.service.implement;

import com.clientapp.socket.ClientSocket;
import com.clientapp.service.ISystemInformation;
import com.clientapp.util.ISystemInfoCollector;
import com.clientapp.util.implement.SystemInfoCollector;
import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

public class SystemInfomation implements ISystemInformation {
    private final Gson gson = new Gson();
    private final ISystemInfoCollector systemInfoCollector;

    public SystemInfomation() {
        systemInfoCollector = new SystemInfoCollector();
    }

    // Send system information to test connect with server via TCP
    @Override
    public void sendSystemInfo() {
        try {
            String jsonClientDetail = gson.toJson(systemInfoCollector.getClientDetail());

            // Check Information
            System.out.println(jsonClientDetail);
            ClientSocket.getInstance().sendEncryptedMessage(jsonClientDetail);
            System.out.println("System info sent to server.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Close the socket connection
    public void closeConnection() {
    }
}