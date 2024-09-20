package com.clientapp.util.implement;

import com.clientapp.util.IScreenCaptureClient;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ScreenCaptureClientUDP implements IScreenCaptureClient {
    private String serverAddress;
    private int serverPort;

    public ScreenCaptureClientUDP(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    public void captureAndSendScreen() {
        try {
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            Robot robot = new Robot();
            BufferedImage screenCapture = robot.createScreenCapture(screenRect);

            // Nén ảnh
            byte[] imageBytes = compressImage(screenCapture, "jpg", 0.5f); // Nén với chất lượng 50%

            // Chia dữ liệu thành các gói UDP nhỏ và gửi đi
            InetAddress serverInetAddress = InetAddress.getByName(serverAddress);
            try (DatagramSocket socket = new DatagramSocket()) {
                int packetSize = 1024; // Kích thước mỗi gói (1KB)
                int totalPackets = (int) Math.ceil((double) imageBytes.length / packetSize);

                for (int i = 0; i < totalPackets; i++) {
                    int start = i * packetSize;
                    int end = Math.min(imageBytes.length, start + packetSize);
                    byte[] packetData = new byte[end - start];

                    System.arraycopy(imageBytes, start, packetData, 0, end - start);
                    DatagramPacket packet = new DatagramPacket(packetData, packetData.length, serverInetAddress, serverPort);
                    socket.send(packet);
                }

                System.out.println("Sent image in " + totalPackets + " packets");
            }
        } catch (AWTException | IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] compressImage(BufferedImage image, String formatName, float quality) throws IOException {
        if (quality < 0f || quality > 1f) {
            throw new IllegalArgumentException("Chất lượng nén phải nằm trong khoảng từ 0.0 đến 1.0");
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {

            ImageWriter writer = ImageIO.getImageWritersByFormatName(formatName).next();
            writer.setOutput(ios);

            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(quality); // Chất lượng nén
            }

            writer.write(null, new IIOImage(image, null, null), param);
            writer.dispose();

            return baos.toByteArray();
        }
    }

    @Override
    public void closeConnection() {

    }
}
