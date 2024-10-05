package com.serverapp.util.implement;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import com.serverapp.controller.view.ClientScreenController;
import com.serverapp.controller.view.ClientScreenController;
import com.serverapp.util.IScreenCaptureHandler;

public class ScreenCaptureHandler implements IScreenCaptureHandler {
    private Socket clientSocket;
    private ClientScreenController screenCaptureController;
    private int totalChunks;
    private int receivedChunks;

    public ScreenCaptureHandler(Socket clientSocket, ClientScreenController screenCaptureController) {
        this.clientSocket = clientSocket;
        this.screenCaptureController = screenCaptureController;
        this.totalChunks = 0;
        this.receivedChunks = 0;
    }

    @Override
    public void run() {
        try (
                DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            while (true) {
                try {
                    if (totalChunks == 0) {
                        totalChunks = in.readInt();
                    }

                    int length = in.readInt();
                    if (length <= 0) {
                        break; // Exit the loop if the length is invalid
                    }
                    byte[] imageBytes = new byte[length];
                    in.readFully(imageBytes);
                    buffer.write(imageBytes);
                    receivedChunks++;

                    if (isImageComplete()) {
                        byte[] completeImageBytes = buffer.toByteArray();
                        BufferedImage image = ImageIO.read(new ByteArrayInputStream(completeImageBytes));

                        // Resize the image to 640x360
                        BufferedImage resizedImage = new BufferedImage(876, 510, BufferedImage.TYPE_INT_RGB);
                        java.awt.Graphics2D g2d = resizedImage.createGraphics();
                        g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                        g2d.drawImage(image, 0, 0, 876, 510, null);
                        g2d.dispose();

                        // Update the UI with the resized image
                        screenCaptureController.updateScreenCapture(new ImageIcon(resizedImage));

                        // Release memory for the images
                        image.flush();
                        resizedImage.flush();

                        // Clear the buffer for the next image
                        buffer.reset();
                        totalChunks = 0;
                        receivedChunks = 0;
                    }

                    // Add a delay between updates
                    try {
                        Thread.sleep(50); // Approximately 24 FPS
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); // Restore interrupted status
                    }
                } catch (EOFException e) {
                    System.out.println("Client disconnected.");
                    break; // Exit the loop if the end of the stream is reached
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Ensure resources are released when finished
            try {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isImageComplete() {
        return receivedChunks == totalChunks;
    }
}