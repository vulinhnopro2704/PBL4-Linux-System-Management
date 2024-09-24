package com.serverapp.service.implement;

import com.serverapp.controller.view.AppController;
import com.serverapp.controller.view.ClientPerformanceController;
import com.serverapp.model.ClientCredentials;
import com.serverapp.service.ICPUinfoServer;
import com.serverapp.socket.SocketManager;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CPUinfoServer implements ICPUinfoServer {
    private ClientPerformanceController clientPerformanceController;
    private ExecutorService clientHandlerPool;
    private CPUInfoHandler CPUInfoHandler;

    public CPUinfoServer(ClientPerformanceController clientPerformanceController) {
        this.clientPerformanceController = clientPerformanceController;
        this.clientHandlerPool = Executors.newSingleThreadExecutor();
    }
    @Override
    public void start() throws IOException {
        System.out.println("Start CPU Info Server" + "\n");
        ClientCredentials clientCredentials = SocketManager.getInstance().getClientCredentials(AppController.getInstance().getCurrentClientIp());
        if (clientCredentials != null) {
            Socket clientSocket = clientCredentials.getSocket();
            if (!clientSocket.isClosed()) {
                clientHandlerPool.submit(() -> {
                    CPUInfoHandler = new CPUInfoHandler(clientCredentials, clientPerformanceController);
                    CPUInfoHandler.run();
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
        System.out.println("Stopping CPU Usage Server...");
        if (CPUInfoHandler != null) {
            CPUInfoHandler.stop();
            CPUInfoHandler = null;
        }
        clientHandlerPool.shutdown();
        clientHandlerPool.shutdownNow();
        System.out.println("CPU Usage Server stopped.");
    }
}
