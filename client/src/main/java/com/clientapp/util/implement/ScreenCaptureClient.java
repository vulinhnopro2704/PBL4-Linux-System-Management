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
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ScreenCaptureClient implements IScreenCaptureClient {
    private String serverAddress;
    private int serverPort;
    private Socket socket;
    private DataOutputStream out;

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

    public void captureAndSendScreen() {
        try {
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            Robot robot = new Robot();
            BufferedImage screenCapture = robot.createScreenCapture(screenRect);


           BufferedImage resizedImage = resizeImage(screenCapture, 640, 360); // Thay đổi kích thước nếu cần

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, "jpg", baos); // Có thể điều chỉnh chất lượng nếu cần

            // Nén ảnh
            byte[] imageBytes = compressImage(resizedImage, "jpg", 0.5f); // Adjust the quality as needed

            out.writeInt(imageBytes.length);
            out.write(imageBytes);
            out.flush();

            baos.close(); // Giải phóng tài nguyên
        } catch (AWTException | IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] compressImage(BufferedImage image, String formatName, float quality) throws IOException {
        // Kiểm tra nếu tỷ lệ chất lượng ngoài khoảng từ 0 đến 1
        if (quality < 0f || quality > 1f) {
            throw new IllegalArgumentException("Chất lượng nén phải nằm trong khoảng từ 0.0 đến 1.0");
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {

            // Tìm ImageWriter phù hợp với định dạng
            ImageWriter writer = ImageIO.getImageWritersByFormatName(formatName).next();
            writer.setOutput(ios);

            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(quality); // Tỷ lệ chất lượng nén
            }

            // Ghi hình ảnh với các tham số nén đã đặt
            writer.write(null, new IIOImage(image, null, null), param);

            writer.dispose();

            // In kích thước của hình ảnh đã nén
            byte[] compressedImage = baos.toByteArray();
            System.out.println("Compressed image size: " + compressedImage.length);

            return compressedImage;

        } catch (IOException e) {
            // Ghi log hoặc ném ngoại lệ khi có lỗi xảy ra
            throw new IOException("Lỗi khi nén ảnh: " + e.getMessage(), e);
        }
    }

    // Phương thức để thay đổi kích thước hình ảnh
    private BufferedImage resizeImage(BufferedImage originalImage, int newWidth, int newHeight) {
        Image resultingImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_DEFAULT);
        BufferedImage outputImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(resultingImage, 0, 0, null);
        g2d.dispose();

        return outputImage;
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