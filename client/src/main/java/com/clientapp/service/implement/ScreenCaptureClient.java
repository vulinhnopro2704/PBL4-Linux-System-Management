package com.clientapp.service.implement;

import com.clientapp.enums.RequestType;
import com.clientapp.socket.ClientSocket;
import com.clientapp.service.IScreenCaptureClient;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScreenCaptureClient implements IScreenCaptureClient {
    private static final int CHUNK_SIZE = 1024 * 64; // 64KB
    private Boolean isRunning = true;
    BufferedReader in;

    public ScreenCaptureClient() {
        in = new BufferedReader(new InputStreamReader(ClientSocket.getInstance().getInputStream()));
    }

    public void start() throws IOException {
        new Thread(() -> {
            try {
                String request = in.readLine();
                if (request != null && !request.trim().isEmpty()) {
                    if (RequestType.valueOf(request) == RequestType.EXIT_SCREEN_CAPTURE) {
                        isRunning = false;
                        System.out.println("Exit Screen Capture Completed");
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
        while (isRunning) {
            captureAndSendScreen();
        }
    }

    public void captureAndSendScreen() {
        try {
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            Robot robot = new Robot();
            BufferedImage screenCapture = robot.createScreenCapture(screenRect);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(screenCapture, "jpg", baos);
            byte[] imageBytes = baos.toByteArray();

            int totalChunks = (int) Math.ceil((double) imageBytes.length / CHUNK_SIZE);
            DataOutputStream out = new DataOutputStream(ClientSocket.getInstance().getClientSocket().getOutputStream());
            out.writeInt(totalChunks);

            for (int i = 0; i < totalChunks; i++) {
                if (!isRunning) break;
                int start = i * CHUNK_SIZE;
                int length = Math.min(imageBytes.length - start, CHUNK_SIZE);
                out.writeInt(length);
                out.write(imageBytes, start, length);
                out.flush();
            }

            baos.close();
        } catch (AWTException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void closeConnection() {

    }
}