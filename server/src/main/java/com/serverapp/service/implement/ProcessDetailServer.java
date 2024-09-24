package com.serverapp.service.implement;

import com.serverapp.controller.view.AppController;
import com.serverapp.controller.view.ClientProcessController;
import com.serverapp.model.ClientCredentials;
import com.serverapp.service.IProcessDetailServer;
import com.serverapp.socket.SocketManager;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProcessDetailServer implements IProcessDetailServer {
    private ClientProcessController processController;
    private ExecutorService clientHandlerPool;
    private ProcessDetailHandler processDetailHandler;

    public ProcessDetailServer(ClientProcessController processController) {
        this.processController = processController;
        this.clientHandlerPool = Executors.newSingleThreadExecutor();
    }

    @Override
    public void start() throws IOException {
        System.out.println("Start Process Detail Server" + "\n");
        ClientCredentials clientCredentials = SocketManager.getInstance().getClientCredentials(AppController.getInstance().getCurrentClientIp());
        if (clientCredentials != null) {
            Socket clientSocket = clientCredentials.getSocket();
            if (!clientSocket.isClosed()) {
                clientHandlerPool.submit(() -> {
                    processDetailHandler = new ProcessDetailHandler(clientCredentials, processController);
                    processDetailHandler.run();
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
        System.out.println("Stopping process detail server...");
        if (processDetailHandler != null) {
            processDetailHandler.stop();
        }
        clientHandlerPool.shutdown();
        clientHandlerPool.shutdownNow();
        System.out.println("Process detail server stopped.");
    }
}
