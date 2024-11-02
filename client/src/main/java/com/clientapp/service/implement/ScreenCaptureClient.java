package com.clientapp.service.implement;

import com.clientapp.enums.RequestType;
import com.clientapp.socket.ClientSocket;
import com.clientapp.service.IScreenCaptureClient;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class ScreenCaptureClient implements IScreenCaptureClient {
    private static final int CHUNK_SIZE = 1024 * 64;
    private volatile Boolean isRunning;
    BufferedReader in;
    DataOutputStream out;
    private Thread listenerThread;

    public ScreenCaptureClient() throws IOException {
        in = new BufferedReader(new InputStreamReader(ClientSocket.getInstance().getInputStream()));
        out = new DataOutputStream(ClientSocket.getInstance().getClientSocket().getOutputStream());
    }

    public void start() throws IOException {
        isRunning = true;
        System.out.println("Screen Capture Started");
        // Tạo và khởi động luồng lắng nghe
        listenerThread = new Thread(() -> {
            try {
                while (isRunning) {
                    String request = in.readLine();
                    if (request != null && !request.trim().isEmpty()) {
                        if (RequestType.valueOf(request) == RequestType.EXIT_SCREEN_CAPTURE) {
                            isRunning = false;
                            System.out.println("Exit Screen Capture Command Received");
                        }
                    }
                }
            } catch (IOException e) {
                if (!isRunning) {
                    System.out.println("Listener thread interrupted and stopped.");
                } else {
                    throw new RuntimeException(e);
                }
            }
        });
        listenerThread.start();
        while (isRunning) {
            captureAndSendScreen();
        }
        System.out.println("Screen Captured exit while loop");
    }

    public void captureAndSendScreen() throws IOException {
        try {
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            Robot robot = new Robot();
            BufferedImage screenCapture = robot.createScreenCapture(screenRect);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(screenCapture, "jpg", baos);
            byte[] imageBytes = baos.toByteArray();

            int totalChunks = (int) Math.ceil((double) imageBytes.length / CHUNK_SIZE);

            if (!isRunning) return;
            out.writeInt(totalChunks);

            for (int i = 0; i < totalChunks; i++) {
                if (!isRunning) break;
                int start = i * CHUNK_SIZE;
                int length = Math.min(imageBytes.length - start, CHUNK_SIZE);
                out.writeInt(length);
                out.write(imageBytes, start, length);
                System.out.println("Length: " + length);
                out.flush();
            }
            System.out.println("Screen Captured and Sent");

            baos.close();
        } catch (AWTException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void closeConnection() {
        isRunning = false;
        try {
            if (listenerThread != null) {
                System.out.println("Interrupting listener thread...");
                listenerThread.interrupt();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}