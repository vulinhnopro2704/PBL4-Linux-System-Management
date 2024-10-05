package com.serverapp.util.implement;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import com.serverapp.controller.view.ClientScreenController;
import com.serverapp.enums.RequestType;
import com.serverapp.util.IScreenCaptureServer;

public class ScreenCaptureServer implements IScreenCaptureServer {
    private ServerSocket serverSocket;
    private ClientScreenController screenCaptureController;

    public ScreenCaptureServer(ClientScreenController screenCaptureController) throws IOException {
        this.serverSocket = TCPServer.getInstance().getServerSocket();
        this.screenCaptureController = screenCaptureController;
    }

    @Override
    public void start() {
        new Thread(() -> {
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    out.println(RequestType.SCREEN_CAPTURE);
                    new Thread(new ScreenCaptureHandler(clientSocket, screenCaptureController)).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void stop() throws IOException {
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
    }
}