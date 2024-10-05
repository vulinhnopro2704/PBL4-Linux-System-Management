package com.clientapp.util.implement;

import com.clientapp.util.IScreenCaptureClient;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ScreenCaptureClient implements IScreenCaptureClient {
    private String serverAddress;
    private int serverPort;
    private Socket socket;
    private DataOutputStream out;
    private static final int CHUNK_SIZE = 1024 * 64; // 64KB

    public ScreenCaptureClient(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        try {
            this.socket = new Socket(serverAddress, serverPort);
            this.out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        try {
            if (out != null) {
                out.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}