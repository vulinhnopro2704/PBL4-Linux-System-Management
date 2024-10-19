package com.clientapp.service.implement;

import com.clientapp.ClientSocket;
import com.clientapp.service.ISystemInformation;
import com.clientapp.util.ISystemInfoCollector;
import com.clientapp.util.implement.SystemInfoCollector;
import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

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
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(ClientSocket.getInstance().getOutputStream()));
            out.write(jsonClientDetail + "\n");
            out.flush();
            System.out.println("System info sent to server.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Close the socket connection
    public void closeConnection() {
    }
}