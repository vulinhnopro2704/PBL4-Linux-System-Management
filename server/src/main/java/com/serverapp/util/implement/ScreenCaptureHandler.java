package com.serverapp.util.implement;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import com.serverapp.controller.view.ScreenCaptureController;
import com.serverapp.util.IScreenCaptureHandler;

public class ScreenCaptureHandler implements IScreenCaptureHandler {
    private Socket clientSocket;
    private ScreenCaptureController screenCaptureController;

    public ScreenCaptureHandler(Socket clientSocket, ScreenCaptureController screenCaptureController) {
        this.clientSocket = clientSocket;
        this.screenCaptureController = screenCaptureController;
    }

    @Override
    public void run() {
        try (DataInputStream in = new DataInputStream(clientSocket.getInputStream())) {
            while (true) {
                try {
                    int length = in.readInt();
                    if (length <= 0) {
                        break; // Exit the loop if the length is invalid
                    }
                    byte[] imageBytes = new byte[length];
                    in.readFully(imageBytes);
                    BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));

                    // Cập nhật hình ảnh trong UI
                    screenCaptureController.updateScreenCapture(new ImageIcon(image));

                    // Giải phóng bộ nhớ cho hình ảnh
                    image.flush();

                    // Thêm một khoảng thời gian giữa các lần cập nhật
                    try {
                        Thread.sleep(50); // Khoảng 24 FPS
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); // Khôi phục trạng thái ngắt
                    }
                } catch (EOFException e) {
                    System.out.println("Client disconnected.");
                    break; // Exit the loop if the end of the stream is reached
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Đảm bảo giải phóng tài nguyên khi kết thúc
            try {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}