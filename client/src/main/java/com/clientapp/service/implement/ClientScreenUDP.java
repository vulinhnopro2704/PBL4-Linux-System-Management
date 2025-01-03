package com.clientapp.service.implement;

import com.clientapp.enums.RequestType;
import com.clientapp.model.ScreenPacket;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ClientScreenUDP {
    private static final int CHUNK_SIZE = 6048; // Kích thước gói
    private volatile boolean isRunning;
    private DatagramSocket socket;
    private Thread listenerThread;
    private Thread screenThread;
    private String hostname;
    private int port = 9876;

    public ClientScreenUDP(String hostname, int port) throws IOException {
        socket = new DatagramSocket(10000);
        isRunning = true;
        this.hostname = hostname;
        this.port = port;
    }

    public void start() throws IOException {
        // Start listener thread for STOP signal
        listenerThread = new Thread(this::listen);
        listenerThread.start();
        screenThread = new Thread(this::startCapture);
        screenThread.start();
    }

    private void startCapture() {
            while (isRunning) {
                BufferedImage screen = captureScreen();
                sendScreen(screen, hostname, port);
            }
    }

    // Chụp màn hình
    private BufferedImage captureScreen() {
        try {
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            Robot robot = new Robot();
            BufferedImage screenCapture = robot.createScreenCapture(screenRect);

            // Resize và nén ảnh
            BufferedImage resizedImage = resizeImage(screenCapture, 800, 450);
            return compressImage(resizedImage);

        } catch (AWTException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Gửi màn hình
    private void sendScreen(BufferedImage image, String hostname, int port) {
        try {
            if (image == null) return;

            // Chuyển ảnh thành mảng byte
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", baos);
            byte[] imageBytes = baos.toByteArray();

            int totalChunks = (int) Math.ceil((double) imageBytes.length / CHUNK_SIZE);
            InetAddress address = InetAddress.getByName(hostname);

            for (int i = 0; i < totalChunks; i++) {
                if (!isRunning) break;

                int start = i * CHUNK_SIZE;
                int length = Math.min(CHUNK_SIZE, imageBytes.length - start);

                byte[] chunkData = new byte[length];
                System.arraycopy(imageBytes, start, chunkData, 0, length);

                ScreenPacket screenPacket = new ScreenPacket(totalChunks, i + 1, length, chunkData);
                String json = screenPacket.toJson();
                byte[] sendData = json.getBytes();

                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, port);
//                System.out.println("Screen --> " + sendPacket.getLength());
                socket.send(sendPacket);

//                System.out.println("Sent chunk " + (i + 1) + " of " + totalChunks);
            }
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Resize ảnh
    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        Image scaledImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(scaledImage, 0, 0, null);
        g2d.dispose();
        return resizedImage;
    }

    // Nén ảnh
    private BufferedImage compressImage(BufferedImage image) {
        RescaleOp rescaleOp = new RescaleOp(0.8f, 15, null); // Giảm độ sáng, tăng tương phản
        rescaleOp.filter(image, image);
        return image;
    }

    public void listen() {
        try {
            byte[] receiveBuffer = new byte[1024];  // Đảm bảo đủ lớn để nhận chuỗi
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

            while (isRunning) {
                socket.receive(receivePacket);  // Nhận gói tin UDP
                String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
//                System.out.println("Received message: " + message);

                // Chuyển chuỗi nhận được thành Enum RequestType
                try {
                    RequestType requestType = RequestType.valueOf(message.trim().toUpperCase());

                    // Xử lý từng loại yêu cầu
                    switch (requestType) {
                        case EXIT_SCREEN_CAPTURE:
                            close();
                            break;
                        case START_SCREEN_CAPTURE:
//                            System.out.println("Received START_SCREEN_CAPTURE request. Starting screen capture.");
                            // Thực hiện hành động bắt đầu capture màn hình (nếu cần)
                            break;
                        case PAUSE_SCREEN_CAPTURE:
//                            System.out.println("Received PAUSE_SCREEN_CAPTURE request. Pausing screen capture.");
                            // Thực hiện hành động tạm dừng capture màn hình (nếu cần)
                            break;
                        default:
//                            System.out.println("Unknown request type received: " + requestType);
                    }
                } catch (IllegalArgumentException e) {
                    System.err.println("Invalid request type received: " + message);
                }
            }
        } catch (IOException e) {
            if (!isRunning) {
                System.out.println("Listener thread stopped due to STOP signal.");
            } else {
                e.printStackTrace();
            }
        } finally {
            close();
        }
    }

    public void close() {
        isRunning = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        System.out.println("Socket closed.");
    }
}
