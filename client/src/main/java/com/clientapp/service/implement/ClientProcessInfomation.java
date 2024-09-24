package com.clientapp.service.implement;

import com.clientapp.enums.RequestType;
import com.clientapp.model.ClientProcessDetail;
import com.clientapp.socket.ClientSocket;
import com.clientapp.util.IClientProcessDetailCollector;
import com.clientapp.util.ISystemInfoCollector;
import com.clientapp.util.implement.ClientProcessDetailCollector;
import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClientProcessInfomation
{
    private final Gson gson = new Gson();
    private Boolean isRunning = false;
    private final IClientProcessDetailCollector clientProcessDetailCollector;
    private ExecutorService executors;

    public ClientProcessInfomation(){
        clientProcessDetailCollector = new ClientProcessDetailCollector();
        isRunning = true;
        executors = Executors.newSingleThreadExecutor();
        listenForExitRequest();
    }

    public void listenForExitRequest() {
        executors.submit(() -> {
            try{
                BufferedReader in = new BufferedReader(new InputStreamReader(ClientSocket.getInstance().getInputStream()));
                String message;
                while ((message = in.readLine()) != null) { // Đọc liên tục cho đến khi nhận yêu cầu thoát
                    System.out.println("msg: " + message);
                    if (!message.trim().isEmpty() && RequestType.valueOf(message) == RequestType.EXIT_PROCESS) {
                        isRunning = false; // Đặt cờ thoát khi nhận yêu cầu
                        break; // Thoát khỏi vòng lặp lắng nghe
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void sendProcessDetails() {
        try {
            while (isRunning) {
                String jsonDetails = gson.toJson(clientProcessDetailCollector.collectProcessDetails()); // Chuyển processDetails thành JSON
                if (isRunning) {
                    ClientSocket.getInstance().sendEncryptedMessage(jsonDetails);
                    System.out.println("Process Sent: " + jsonDetails);
                } else {
                    break;
                }
                Thread.sleep(1000); // Thời gian nghỉ giữa các lần gửi
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
