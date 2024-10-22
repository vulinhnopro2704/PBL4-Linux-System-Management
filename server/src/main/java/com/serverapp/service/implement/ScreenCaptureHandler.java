package com.serverapp.service.implement;

import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import com.serverapp.controller.view.ClientScreenController;
import com.serverapp.enums.RequestType;
import com.serverapp.model.ClientCredentials;
import com.serverapp.service.IScreenCaptureHandler;
import com.serverapp.util.CurrentType;

public class ScreenCaptureHandler implements IScreenCaptureHandler {
    private ClientCredentials clientCredentials;
    private ClientScreenController screenCaptureController;
    private int totalChunks;
    private int receivedChunks;
    private boolean isRunning = true;

    public ScreenCaptureHandler(ClientCredentials clientCredentials, ClientScreenController screenCaptureController) {
        this.clientCredentials = clientCredentials;
        this.screenCaptureController = screenCaptureController;
        this.totalChunks = 0;
        this.receivedChunks = 0;

    }

    @Override
    public void run() {
        try {
            DataInputStream in = new DataInputStream(clientCredentials.getInputStream());
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            while (isRunning) {
                try {
                    if (CurrentType.getInstance().getType() != RequestType.SCREEN_CAPTURE) {
                        break; // Exit the loop if the request type is not SCREEN_CAPTURE
                    }

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
                } catch (EOFException e) {
                    System.out.println("Client disconnected.");
                    break; // Exit the loop if the end of the stream is reached
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }
    }

    private boolean isImageComplete() {
        return receivedChunks == totalChunks;
    }
    public void stop() throws IOException {
        isRunning = false;
        PrintWriter out = new PrintWriter(clientCredentials.getOutputStream(), true);
        out.println(RequestType.EXIT_SCREEN_CAPTURE);
    }
}