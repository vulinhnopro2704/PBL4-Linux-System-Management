package com.clientapp.service.implement;

import com.clientapp.enums.RequestType;
import com.clientapp.socket.ClientSocket;
import com.clientapp.util.IClientProcessDetailCollector;
import com.clientapp.util.IMemoryUsageCollector;
import com.clientapp.util.implement.MemoryUsageCollector;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientMemoryUsage {
    private final Gson gson = new Gson();
    private Boolean isRunning = false;
    private final IMemoryUsageCollector memoryUsageCollector;
    private ExecutorService executors;

    public ClientMemoryUsage() {
        memoryUsageCollector = new MemoryUsageCollector();
        isRunning = true;
        executors = Executors.newSingleThreadExecutor();
        listenForExitRequest();
    }

    public void listenForExitRequest() {
        executors.submit(() -> {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(ClientSocket.getInstance().getInputStream()));
                String message;
                while ((message = in.readLine()) != null) { // Đọc liên tục cho đến khi nhận yêu cầu thoát
                    System.out.println("msg: " + message);
                    if (!message.trim().isEmpty() && RequestType.valueOf(message) == RequestType.EXIT_PERFORMANCE_INFO) {
                        isRunning = false; // Đặt cờ thoát khi nhận yêu cầu
                        break; // Thoát khỏi vòng lặp lắng nghe
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void sendMemoryUsage() {
        try {
            while (isRunning) {
                String json = gson.toJson(memoryUsageCollector.getMemoryUsage());
                if (isRunning) {
                    ClientSocket.getInstance().sendEncryptedMessage(json);
                } else
                    break;
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
