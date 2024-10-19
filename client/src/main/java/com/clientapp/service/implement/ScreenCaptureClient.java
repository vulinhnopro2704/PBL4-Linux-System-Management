package com.clientapp.service.implement;

import com.clientapp.ClientSocket;
import com.clientapp.service.IScreenCaptureClient;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ScreenCaptureClient implements IScreenCaptureClient {
    private static final int CHUNK_SIZE = 1024 * 64; // 64KB

    public ScreenCaptureClient() {
    }

    public void start() throws IOException {
        while (true) {
            try {
                captureAndSendScreen();
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
                closeConnection();
            }
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