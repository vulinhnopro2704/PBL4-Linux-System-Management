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
    private boolean isRunning;
    DataInputStream in;
    ByteArrayOutputStream buffer;

    public ScreenCaptureHandler(ClientCredentials clientCredentials, ClientScreenController screenCaptureController) throws IOException {
        this.clientCredentials = clientCredentials;
        this.screenCaptureController = screenCaptureController;
        this.totalChunks = 0;
        this.receivedChunks = 0;
        in = new DataInputStream(clientCredentials.getInputStream());
        buffer = new ByteArrayOutputStream();
    }

    @Override
    public void run() {
        try {
            isRunning = true;
            while (isRunning) {
                try {
                    if (CurrentType.getInstance().getType() != RequestType.SCREEN_CAPTURE) {
                        System.out.println("Invalid request type received.");
                        break; // Exit the loop if the request type is not SCREEN_CAPTURE
                    }

                    if (totalChunks <= 0) {
                        System.out.println("Waiting for total chunks...");
                        totalChunks = in.readInt();
                        System.out.println("Total chunks: " + totalChunks);
                        if (totalChunks <= 0 || totalChunks > 1000) { // Add a reasonable upper limit for validation
                            System.out.println("Invalid total chunks received.");
                            totalChunks = 0; // Reset totalChunks to wait for a valid value
                            continue;
                        }
                    }

                    System.out.println("Waiting for image chunk length...");
                    int length = in.readInt();
                    System.out.println("Image chunk length: " + length);

                    if (length <= 0 || length > 200000) {
                        System.out.println("Invalid image length received.");
                        continue;
                    }

                    byte[] imageBytes = new byte[length];

                    System.out.println("Waiting for image chunk...");
                    in.readFully(imageBytes);
                    System.out.println("Image chunk received.");

                    if (imageBytes.length == 0) {
                        System.out.println("Empty image bytes received.");
                        continue;
                    }
                    buffer.write(imageBytes);
                    receivedChunks++;

                    if (isImageComplete()) {
                        byte[] completeImageBytes = buffer.toByteArray();
                        BufferedImage image = ImageIO.read(new ByteArrayInputStream(completeImageBytes));

                        // Resize the image to 876x510
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
                        System.out.println("Image received.");
                    }
                } catch (EOFException e) {
                    System.out.println("Client disconnected.");
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                stop();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean isImageComplete() {
        return receivedChunks == totalChunks;
    }

    public void stop() throws IOException {
        isRunning = false;
        PrintWriter out = new PrintWriter(clientCredentials.getOutputStream(), true);
        out.println(RequestType.EXIT_SCREEN_CAPTURE);
        out.flush();
        while (in.available() > 0) {
            in.readFully(new byte[in.available()]);
        }
        System.out.println("Screen capture handler stopped.");
    }
}