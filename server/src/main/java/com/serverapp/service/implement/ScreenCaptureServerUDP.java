package com.serverapp.service.implement;

import com.serverapp.controller.view.AppController;
import com.serverapp.controller.view.ClientScreenControllerUDP;
import com.serverapp.enums.RequestType;
import com.serverapp.model.ScreenPacket;
import com.serverapp.service.IScreenCaptureHandler;
import com.serverapp.socket.SocketManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScreenCaptureServerUDP implements IScreenCaptureHandler {
    private final ClientScreenControllerUDP screenCaptureController;
    private final ExecutorService clientHandlerPool;
    private final DatagramSocket socket;
    private boolean isRunning;
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    public ScreenCaptureServerUDP(ClientScreenControllerUDP screenCaptureController) throws IOException {
        this.screenCaptureController = screenCaptureController;
        this.clientHandlerPool = Executors.newSingleThreadExecutor();
        this.socket = new DatagramSocket(9876); // Use an appropriate port number
    }

    @Override
    public void run() {
        System.out.println("Screen capture server UDP started.");
        isRunning = true;
        clientHandlerPool.submit(() -> {
            while (isRunning) {
                try {
                    byte[] receiveData = new byte[65536]; // Tăng kích thước buffer
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    socket.receive(receivePacket);

                    System.out.println("Received screen packet.");
                    String json = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    System.out.println("Received JSON: " + json);

                    try {
                        ScreenPacket screenPacket = ScreenPacket.fromJson(json);
                        System.out.println("ScreenPacket parsed successfully.");
                        // Xử lý gói tin
                        processScreenPacket(screenPacket);
                    } catch (Exception e) {
                        System.err.println("Error parsing JSON: " + e.getMessage());
                        e.printStackTrace();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private void processScreenPacket(ScreenPacket screenPacket) {
        System.out.println("Processing screen packet...");
        try {
            if (!isValidScreenPacket(screenPacket)) {
                System.out.println("Invalid screen packet received.");
                return;
            }

            appendToBuffer(screenPacket);
            System.out.println("Received chunk " + screenPacket.chunkIndex + " of " + screenPacket.totalChunks);

            if (isLastChunk(screenPacket)) {
                handleCompleteImage();
            }
        } catch (IOException e) {
            e.printStackTrace();
            buffer.reset(); // Ensure buffer is cleared on error
        }
    }

    /**
     * Validates if the screen packet is valid.
     */
    private boolean isValidScreenPacket(ScreenPacket screenPacket) {
        return screenPacket.totalChunks > 0 && screenPacket.chunkIndex > 0;
    }

    /**
     * Appends the received chunk data to the buffer.
     */
    private void appendToBuffer(ScreenPacket screenPacket) throws IOException {
        buffer.write(screenPacket.data);
    }

    /**
     * Checks if the received chunk is the last chunk.
     */
    private boolean isLastChunk(ScreenPacket screenPacket) {
        return screenPacket.chunkIndex == screenPacket.totalChunks;
    }

    /**
     * Handles the processing of the complete image once all chunks are received.
     */
    private void handleCompleteImage() {
        try {
            System.out.println("All chunks received. Processing the image...");

            byte[] completeImageBytes = buffer.toByteArray();
            BufferedImage image = decodeImage(completeImageBytes);

            if (image == null) {
                System.out.println("Failed to decode the image. Skipping.");
                buffer.reset(); // Reset buffer for the next image
                return;
            }

//            BufferedImage resizedImage = resizeImage(image, 876, 510);
            updateUI(image);

            // Release memory and reset buffer for the next image
            image.flush();
//            resizedImage.flush();
            buffer.reset();
            System.out.println("Image processed and displayed successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            buffer.reset(); // Ensure buffer is cleared on error
        }
    }

    /**
     * Decodes the image from a byte array.
     */
    private BufferedImage decodeImage(byte[] imageBytes) throws IOException {
        return ImageIO.read(new ByteArrayInputStream(imageBytes));
    }

    /**
     * Resizes the given image to the specified width and height.
     */
    private BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g2d = resizedImage.createGraphics();
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.drawImage(originalImage, 0, 0, width, height, null);
        g2d.dispose();
        return resizedImage;
    }

    /**
     * Updates the UI with the resized image.
     */
    private void updateUI(BufferedImage resizedImage) {
        screenCaptureController.updateScreenCapture(new ImageIcon(resizedImage));
    }


    public void sendRequest(RequestType requestType) {
        try {
            String message = requestType.toString();  // Chuyển Enum thành chuỗi
            byte[] sendData = message.getBytes();
            InetAddress address = InetAddress.getByName(AppController.getInstance().getCurrentClientIp());
            int port = 10000;
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, port);
            socket.send(sendPacket);  // Gửi gói tin UDP
            System.out.println("Sent request: " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() throws Exception {
        sendRequest(RequestType.EXIT_SCREEN_CAPTURE);
        System.out.println("Screen capture server UDP stopped.");
        isRunning = false;
        socket.close();
        clientHandlerPool.shutdown();
    }
}