package com.serverapp.service.implement;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.serverapp.controller.view.AppController;
import com.serverapp.controller.view.ClientProcessController;
import com.serverapp.controller.view.ClientScreenController;
import com.serverapp.database.Redis;
import com.serverapp.enums.RequestType;
import com.serverapp.model.ClientCredentials;
import com.serverapp.model.ClientProcess;
import com.serverapp.service.IProcessDetailHandler;
import com.serverapp.socket.SocketManager;
import com.serverapp.util.CurrentType;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ProcessDetailHandler implements IProcessDetailHandler {
    private ClientCredentials clientCredentials;
    private ClientProcessController clientProcessController;
    private boolean isRunning = true;

    public ProcessDetailHandler(ClientCredentials clientCredentials, ClientProcessController clientProcessController) {
        this.clientCredentials = clientCredentials;
        this.clientProcessController = clientProcessController;
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                if (CurrentType.getInstance().getType() != RequestType.PROCESS_LIST){
                    break;
                }
                //Nhan thong tin process tu client
                String out = SocketManager.getInstance().receiveDecryptedMessage(AppController.getInstance().getCurrentClientIp());
                // Parse chuỗi JSON thành danh sách các đối tượng ClientProcess
                Gson gson = new Gson();
                Type clientProcessListType = new TypeToken<List<ClientProcess>>(){}.getType();
                List<ClientProcess> clientProcessList = gson.fromJson(out, clientProcessListType);

                if (clientProcessList == null && !clientProcessList.isEmpty()) {
                    System.out.println("Null list");
                }else {
                    // In ra danh sách để kiểm tra
                    System.out.println("List size: " + clientProcessList.size());
                }

                Redis.getInstance().setClientProcessView(clientProcessList);
                clientProcessController.update();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void stop() throws IOException {
        isRunning = false;
        PrintWriter out = new PrintWriter(clientCredentials.getOutputStream(), true);
        out.println(RequestType.EXIT_PROCESS);
    }
}
