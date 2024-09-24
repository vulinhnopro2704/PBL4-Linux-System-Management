package com.serverapp.service.implement;

import com.serverapp.controller.view.AppController;
import com.serverapp.controller.view.ClientPerformanceController;
import com.serverapp.model.ClientCredentials;
import com.serverapp.service.IMemoryUsageServer;
import com.serverapp.socket.SocketManager;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MemoryUsageServer implements IMemoryUsageServer {
    private ClientPerformanceController clientPerformanceController;
    private ExecutorService clientHandlerPool;
    private MemoryUsageHandler memoryUsageHandler;

    public MemoryUsageServer(ClientPerformanceController clientPerformanceController) {
        this.clientPerformanceController = clientPerformanceController;
        this.clientHandlerPool = Executors.newSingleThreadExecutor();
    }
    @Override
    public void start() throws IOException {
        System.out.println("Start Memory Usage Server" + "\n");
        ClientCredentials clientCredentials = SocketManager.getInstance().getClientCredentials(AppController.getInstance().getCurrentClientIp());
        if (clientCredentials != null) {
            Socket clientSocket = clientCredentials.getSocket();
            if (!clientSocket.isClosed()) {
                clientHandlerPool.submit(() -> {
                    memoryUsageHandler = new MemoryUsageHandler(clientCredentials, clientPerformanceController);
                    memoryUsageHandler.run();
                });
            }else{
                System.out.println("Socket is closed for IP: " + AppController.getInstance().getCurrentClientIp());
            }
        } else {
            System.out.println("Client credentials not found for IP: " + AppController.getInstance().getCurrentClientIp());
        }
    }

    @Override
    public void stop() throws IOException {
        System.out.println("Stopping Memory Usage Server...");
        if (memoryUsageHandler != null) {
            memoryUsageHandler.stop();
            memoryUsageHandler = null;
        }
        clientHandlerPool.shutdown();
        clientHandlerPool.shutdownNow();
        System.out.println("Memory Usage Server stopped.");
    }
}
