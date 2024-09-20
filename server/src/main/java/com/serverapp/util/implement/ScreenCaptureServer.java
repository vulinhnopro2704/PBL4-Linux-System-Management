package com.serverapp.util.implement;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.serverapp.controller.view.ScreenCaptureController;
import com.serverapp.util.IScreenCaptureServer;

public class ScreenCaptureServer implements IScreenCaptureServer {
    private ServerSocket serverSocket;
    private ScreenCaptureController screenCaptureController;

    public ScreenCaptureServer(int port, ScreenCaptureController screenCaptureController) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.screenCaptureController = screenCaptureController;
    }

    @Override
    public void start() {
        new Thread(() -> {
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
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