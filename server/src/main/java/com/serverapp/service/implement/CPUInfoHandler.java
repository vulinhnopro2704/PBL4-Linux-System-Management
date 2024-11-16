package com.serverapp.service.implement;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.serverapp.controller.view.AppController;
import com.serverapp.controller.view.ClientPerformanceController;
import com.serverapp.enums.RequestType;
import com.serverapp.model.CPUinfo;
import com.serverapp.model.ClientCredentials;
import com.serverapp.model.MemoryUsage;
import com.serverapp.service.ICPUinfoHandler;
import com.serverapp.socket.SocketManager;
import com.serverapp.util.CurrentType;

import java.io.IOException;
import java.io.PrintWriter;

public class CPUInfoHandler implements ICPUinfoHandler {
    private ClientCredentials clientCredentials;
    private ClientPerformanceController clientPerformanceController;
    public boolean isRunning = true;

    public CPUInfoHandler(ClientCredentials clientCredentials, ClientPerformanceController clientPerformanceController) {
        this.clientCredentials = clientCredentials;
        this.clientPerformanceController = clientPerformanceController;
    }
    @Override
    public void run() {
        while (isRunning) {
            try {
                if (CurrentType.getInstance().getType() != RequestType.CPU_INFO) {
                    break;
                }
                String out = SocketManager.getInstance().receiveDecryptedMessage(AppController.getInstance().getCurrentClientIp());
                Gson gson = new Gson();
                CPUinfo cpuInfo = gson.fromJson(out, CPUinfo.class);
                clientPerformanceController.setCPUinfo(cpuInfo);
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
        PrintWriter out = new PrintWriter(clientCredentials.getOutputStream(), true);
        out.println(RequestType.EXIT_CPU_INFO);
    }
}
