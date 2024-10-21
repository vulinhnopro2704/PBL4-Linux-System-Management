package com.clientapp;

import com.clientapp.enums.RequestType;
import com.clientapp.service.ISetupConnection;
import com.clientapp.service.implement.ClientCommand;
import com.clientapp.service.implement.ScreenCaptureClient;
import com.clientapp.service.implement.SetupConnection;
import com.clientapp.service.implement.SystemInfomation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Client {
    public static void main(String[] args) throws IOException {
        try {
            String request;
            BufferedReader in = new BufferedReader(new InputStreamReader(ClientSocket.getInstance().getInputStream()));
            while (true) {
                request = in.readLine();
                if (request == null || request.isEmpty()) continue;
                RequestType requestType = RequestType.valueOf(request);
                System.out.println(requestType);
                switch (requestType) {
                    case CONNECTION:
                        // Handle first connection request
                        ISetupConnection setupConnection = new SetupConnection();
                        setupConnection.start();
                        break;

                    case SCREEN_CAPTURE:
                        // Handle screen capture request
                        ScreenCaptureClient screenCaptureClient = new ScreenCaptureClient();
                        screenCaptureClient.start();
                        break;
                    case SYSTEM_INFO:
                        // Handle system info request
                        SystemInfomation systemInfomation = new SystemInfomation();
                        systemInfomation.sendSystemInfo();
                        break;
                    case PROCESS_LIST:
                        // Handle process list request

                        break;
                    case COMMAND:
                        //Handle Command request
                        ClientCommand clientCommand = new ClientCommand();
                        clientCommand.start();
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

