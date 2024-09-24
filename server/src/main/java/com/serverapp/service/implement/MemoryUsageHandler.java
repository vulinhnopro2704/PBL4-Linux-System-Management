package com.serverapp.service.implement;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.serverapp.controller.view.AppController;
import com.serverapp.controller.view.ClientPerformanceController;
import com.serverapp.enums.RequestType;
import com.serverapp.model.ClientCredentials;
import com.serverapp.model.MemoryUsage;
import com.serverapp.service.IMemoryUsageHandler;
import com.serverapp.socket.SocketManager;
import com.serverapp.util.CurrentType;

import java.io.IOException;
import java.io.PrintWriter;

public class MemoryUsageHandler implements IMemoryUsageHandler {
    private ClientCredentials clientCredentials;
    private ClientPerformanceController clientPerformanceController;
    public boolean isRunning = true;

    public MemoryUsageHandler(ClientCredentials clientCredentials, ClientPerformanceController clientPerformanceController) {
        this.clientCredentials = clientCredentials;
        this.clientPerformanceController = clientPerformanceController;
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                if (CurrentType.getInstance().getType() != RequestType.PERFORMANCE_INFO) {
                    break;
                }
                String out = SocketManager.getInstance().receiveDecryptedMessage(AppController.getInstance().getCurrentClientIp());

                Gson gson = new Gson();
                MemoryUsage memoryUsage = gson.fromJson(out, MemoryUsage.class);
                clientPerformanceController.setMemoryUsage(memoryUsage);
                clientPerformanceController.update();
            } catch (JsonSyntaxException e) {
                System.err.println("JSON syntax error: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("An error occurred: " + e.getMessage());
            }
        }
    }

    public void stop() throws IOException {
        isRunning = false;
        SocketManager.getInstance().clearReceiveStream(AppController.getInstance().getCurrentClientIp());
        PrintWriter out = new PrintWriter(clientCredentials.getOutputStream(), true);
        out.println(RequestType.EXIT_PERFORMANCE_INFO);
    }
}
