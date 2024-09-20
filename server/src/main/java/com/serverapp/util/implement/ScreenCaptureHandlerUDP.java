package com.serverapp.util.implement;

import com.serverapp.controller.view.ScreenCaptureController;
import com.serverapp.util.IScreenCaptureHandler;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

public class ScreenCaptureHandlerUDP implements IScreenCaptureHandler {
    private DatagramSocket socket;
    private ScreenCaptureController screenCaptureController;

    public ScreenCaptureHandlerUDP(int serverPort, ScreenCaptureController screenCaptureController) {
        this.screenCaptureController = screenCaptureController;
        try {
            // Mở socket UDP trên cổng server
            this.socket = new DatagramSocket(new InetSocketAddress(serverPort));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            byte[] buffer = new byte[65535]; // Kích thước tối đa gói UDP
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            while (true) {
                // Nhận gói dữ liệu
                socket.receive(packet);

                // Đọc ảnh từ gói dữ liệu nhận được
                ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
                BufferedImage image = ImageIO.read(bais);

                // Cập nhật hình ảnh trong UI
                screenCaptureController.updateScreenCapture(new ImageIcon(image));

                // Giải phóng bộ nhớ cho hình ảnh
                image.flush();

                // Thêm thời gian chờ giữa các lần nhận ảnh
                Thread.sleep(200); // Khoảng 24 FPS
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            // Đảm bảo giải phóng tài nguyên
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }
}
