package com.serverapp.service.implement;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.serverapp.controller.view.AppController;
import com.serverapp.controller.view.ClientScreenController;
import com.serverapp.enums.RequestType;
import com.serverapp.model.ClientCredentials;
import com.serverapp.service.IScreenCaptureServer;
import com.serverapp.socket.SocketManager;

public class ScreenCaptureServer implements IScreenCaptureServer {
    private ClientScreenController screenCaptureController;
    private ExecutorService clientHandlerPool;
    private ScreenCaptureHandler screenCaptureHandler;


    public ScreenCaptureServer(ClientScreenController screenCaptureController) throws IOException {
        this.screenCaptureController = screenCaptureController;
        this.clientHandlerPool = Executors.newSingleThreadExecutor();
    }

    @Override
    public void start() throws IOException {
        System.out.println("Start Client Screen");
        ClientCredentials clientCredentials = SocketManager.getInstance().getClientCredentials(AppController.getInstance().getCurrentClientIp());
        if (clientCredentials != null) {
            Socket clientSocket = clientCredentials.getSocket();
            if (!clientSocket.isClosed()) { // Check if the socket is open
                PrintWriter out = new PrintWriter(clientCredentials.getOutputStream(), true);
                out.println(RequestType.SCREEN_CAPTURE);
                clientHandlerPool.submit(() -> {
                    screenCaptureHandler = new ScreenCaptureHandler(clientCredentials, screenCaptureController);
                    screenCaptureHandler.run();
                });
            } else {
                System.out.println("Socket is closed for IP: " + AppController.getInstance().getCurrentClientIp());
            }
        } else {
            System.out.println("Client credentials not found for IP: " + AppController.getInstance().getCurrentClientIp());
        }
    }

    @Override
    public void stop() throws IOException {
        System.out.println("Stopping screen capture server...");
        if (screenCaptureHandler != null) {
            screenCaptureHandler.stop();
        }
        clientHandlerPool.shutdown();
        clientHandlerPool.shutdownNow();
        System.out.println("Screen capture server stopped.");
    }
}
