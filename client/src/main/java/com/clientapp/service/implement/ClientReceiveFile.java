package com.clientapp.service.implement;

import com.clientapp.socket.ClientSocket;
import com.clientapp.service.IReceiveFile;

import java.io.IOException;

public class ClientReceiveFile implements IReceiveFile {
    private volatile boolean isRunning = true;

    public ClientReceiveFile() {
    }

    @Override
    public void start() {
        try {
            while (isRunning) {
                String command = ClientSocket.getInstance().receiveMessage();
                try {
                    if (!command.trim().isEmpty()) {
                        if ("EXIT_FILE_TRANSFER".equalsIgnoreCase(command)) {
                            System.out.println("Server đã đóng kết nối.");
                            break;
                        } else if ("FILE_TRANSFER".equals(command)) {
                            ClientSocket.getInstance().receiveDecryptedFile();
                    }
                    } else {
                        System.out.println("Nhận được lệnh rỗng. Tắt trạng thái chạy.");
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    @Override
    public void stop() {
        isRunning = false;
        System.out.println("Exit Client Receive File");
    }
}

