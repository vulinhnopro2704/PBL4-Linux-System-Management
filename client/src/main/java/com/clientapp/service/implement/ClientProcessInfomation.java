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

    public static boolean isValidRequestType(String value) {
        for (RequestType type : RequestType.values()) {
            if (type.name().equals(value)) {
                return true;
            }
        }
        return false;
    }

    public void listenForExitRequest() {
        executors.submit(() -> {
            try{
                BufferedReader in = new BufferedReader(new InputStreamReader(ClientSocket.getInstance().getInputStream()));
                String message;
                while ((message = ClientSocket.getInstance().receiveDecryptedMessage()) != null) { // Đọc liên tục cho đến khi nhận yêu cầu thoát
                    System.out.println("msg: " + message);
                    if (isValidRequestType(message)){
                        if (!message.trim().isEmpty() && RequestType.valueOf(message) == RequestType.EXIT_PROCESS) {
                            isRunning = false; // Đặt cờ thoát khi nhận yêu cầu
                            break; // Thoát khỏi vòng lặp lắng nghe
                        }
                    }else if (!message.trim().isEmpty()) {
                        String pid = ClientSocket.getInstance().receiveDecryptedMessage();
                        killProcess(pid);
                    }
                }
            } catch (Exception e) {
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
                Thread.sleep(3000); // Thời gian nghỉ giữa các lần gửi
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Hàm kết thúc tiến trình dựa trên PID
    public static void killProcess(String pid) {
        String os = System.getProperty("os.name").toLowerCase();
        String killCommand;

        if (os.contains("win")) {
            killCommand = "taskkill /F /PID " + pid;  // Lệnh cho Windows
        } else {
            killCommand = "kill -9 " + pid;  // Lệnh cho Linux
        }

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", killCommand);
            if (!os.contains("win")) {
                processBuilder = new ProcessBuilder("bash", "-c", killCommand);
            }

            Process process = processBuilder.start();
            process.waitFor();
            System.out.println("Process " + pid + " has been killed on " + os);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("Failed to kill process " + pid);
        }
    }
}
