package com.clientapp.service.implement;

import com.clientapp.service.IReceiveFile;

import java.io.*;
import java.net.*;

public class ClientReceiveFile implements IReceiveFile {
    private Socket socket;
    private DataInputStream dis;
    private boolean isRunning = true;
    private final String downloadDir = System.getProperty("user.home") + "/Downloads";
    public ClientReceiveFile(String host, int port) throws IOException {
        socket = new Socket(host, port);
        dis = new DataInputStream(socket.getInputStream());
    }

    @Override
    public void start() {
        new Thread(() -> {
            System.out.println("Client is ready and waiting for files...");
            try {
                while (isRunning) {
                    String command = dis.readUTF();
                    System.out.println("Command File received: " + command);
                    if ("EXIT_FILE_SCREEN".equalsIgnoreCase(command)) {
                        System.out.println("Connection closed by server.");
                        stop();
                        break;
                    } else if ("FILE_TRANSFER".equalsIgnoreCase(command)) {
                        receiveFile();
                    }
                    else {
                        System.out.println("Connection closed by server.");
                        stop();
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    dis.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void stop() {
        try {
            isRunning = false;
            dis.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveFile() throws IOException {

        // Đảm bảo thư mục lưu trữ tồn tại
        File directory = new File(downloadDir);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Failed to create directory: " + downloadDir);
        }

        // Nhận tên file
        String fileName = dis.readUTF();
        long fileSize = dis.readLong();

        // Tạo file trong thư mục chỉ định
        File file = new File(directory, fileName);
        if (file.exists()) {
            System.out.println("File already exists, renaming to avoid overwrite.");
            file = new File(directory, System.currentTimeMillis() + "_" + fileName);
        }

        try (FileOutputStream fos = new FileOutputStream(file)) {
            byte[] buffer = new byte[4096];
            long remaining = fileSize;
            int bytesRead;
            while (remaining > 0 && (bytesRead = dis.read(buffer, 0, (int) Math.min(buffer.length, remaining))) > 0) {
                fos.write(buffer, 0, bytesRead);
                remaining -= bytesRead;
            }
        }

        System.out.println("File received and saved to: " + file.getAbsolutePath());
    }
}

