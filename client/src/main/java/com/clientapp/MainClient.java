package com.clientapp;

import com.clientapp.enums.RequestType;
import com.clientapp.util.implement.ClientCommand;
import com.clientapp.util.implement.ScreenCaptureClient;
import com.clientapp.util.implement.TCPClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MainClient {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private String serverIp = "localhost";
    private int serverPort = 8080;

    public void start() throws IOException {
        clientSocket = new Socket(serverIp, serverPort);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    public void listenForRequests() throws IOException {
        String request;
        while ((request = in.readLine()) != null) {
            RequestType requestType = RequestType.valueOf(request);
            System.out.println(requestType);
            switch (requestType) {
                case SCREEN_CAPTURE:
                    // Handle screen capture request
                    ScreenCaptureClient screenCaptureClient = new ScreenCaptureClient(serverIp, serverPort);
                    screenCaptureClient.start();
                    break;
                case SYSTEM_INFO:
                    // Handle system info request
                    TCPClient tcpClient = new TCPClient(serverIp, serverPort);
                    tcpClient.sendSystemInfo();
                    break;
                case PROCESS_LIST:
                    // Handle process list request

                    break;
                case COMMAND:
                    //Handle Command request
                    ClientCommand clientCommand = new ClientCommand(serverIp, serverPort);
                    clientCommand.start();
                    break;
            }
        }
    }

    public static void main(String[] args) throws IOException {
        MainClient client =  new MainClient();
        client.start();
        try {
            client.listenForRequests();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

