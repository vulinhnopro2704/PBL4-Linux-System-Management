package com.serverapp.util.implement;

import java.io.IOException;
import java.net.DatagramSocket;

import com.serverapp.controller.view.ScreenCaptureController;
import com.serverapp.util.IScreenCaptureServer;

public class ScreenCaptureServerUDP implements IScreenCaptureServer {
    private DatagramSocket datagramSocket;
    private ScreenCaptureController screenCaptureController;
    private int port;

    public ScreenCaptureServerUDP(int port, ScreenCaptureController screenCaptureController) throws IOException {
        this.port = port;
        this.screenCaptureController = screenCaptureController;
    }

    @Override
    public void start() {
        new Thread(() -> {
            ScreenCaptureHandlerUDP handler = new ScreenCaptureHandlerUDP(port, screenCaptureController);
            new Thread(handler).start();
        }).start();
    }

    @Override
    public void stop() throws IOException {
//        if (datagramSocket != null && !datagramSocket.isClosed()) {
//            datagramSocket.close();
//        }
    }
}
