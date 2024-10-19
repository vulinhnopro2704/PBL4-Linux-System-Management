package com.serverapp.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;

public class SocketManager {
    private static SocketManager instance;
    private HashMap<String, Socket> socketMap;  // Key: Địa chỉ IP, Value: Socket
    private PublicKey rsaPublicKey;
    private PrivateKey rsaPrivateKey;

    // Phương thức để lấy thể hiện duy nhất của Singleton
    public static synchronized SocketManager getInstance() {
        if (instance == null) {
            instance = new SocketManager();
        }
        return instance;
    }

    // Private constructor để ngăn chặn việc khởi tạo từ bên ngoài
    private SocketManager() {
        socketMap = new HashMap<>();
        generateRSAKeys();
    }

    // Tạo cặp khóa RSA
    private void generateRSAKeys() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair pair = keyGen.generateKeyPair();
            rsaPublicKey = pair.getPublic();
            rsaPrivateKey = pair.getPrivate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Thêm Socket vào HashMap với Key là địa chỉ IP
    public synchronized void addSocket(String ip, Socket socket) {
        socketMap.put(ip, socket);
    }

    // Xóa Socket khỏi HashMap dựa vào địa chỉ IP
    public synchronized void removeSocket(String ip) {
        socketMap.remove(ip);
    }

    // Lấy Socket dựa vào địa chỉ IP
    public synchronized Socket getSocket(String ip) {
        return socketMap.get(ip);
    }

    // Lấy tất cả các Socket hiện tại (nếu cần)
    public synchronized HashMap<String, Socket> getAllSockets() {
        return new HashMap<>(socketMap); // Trả về bản sao của HashMap để tránh sửa đổi ngoài ý muốn
    }

    // Kiểm tra xem một địa chỉ IP có đang kết nối không
    public synchronized boolean containsSocket(String ip) {
        return socketMap.containsKey(ip);
    }

    // Mã hóa gói tin trước khi gửi (ví dụ: AES)
    public byte[] encryptMessage(String message) {
        // Sử dụng một thuật toán mã hóa (AES chẳng hạn)
        // Trả về mảng byte đã mã hóa
        // (Bỏ qua chi tiết mã hóa để giữ mã đơn giản)
        return message.getBytes();  // Placeholder
    }

    // Giải mã gói tin sau khi nhận
    public String decryptMessage(byte[] encryptedMessage) {
        // Sử dụng thuật toán giải mã để chuyển đổi mảng byte về String
        // (Bỏ qua chi tiết giải mã để giữ mã đơn giản)
        return new String(encryptedMessage);  // Placeholder
    }

    // Gửi gói tin đã mã hóa tới một Socket dựa vào địa chỉ IP
    public void sendMessage(String ip, String message) throws IOException {
        Socket socket = getSocket(ip);
        if (socket != null) {
            byte[] encryptedMessage = encryptMessage(message);
            OutputStream out = socket.getOutputStream();
            out.write(encryptedMessage);
        } else {
            throw new IOException("Socket không tồn tại cho IP: " + ip);
        }
    }

    // Nhận và giải mã gói tin từ một Socket dựa vào địa chỉ IP
    public String receiveMessage(String ip) throws IOException {
        Socket socket = getSocket(ip);
        if (socket != null) {
            InputStream in = socket.getInputStream();
            byte[] encryptedMessage = new byte[1024];  // Giả sử gói tin có kích thước cố định
            in.read(encryptedMessage);
            return decryptMessage(encryptedMessage);
        } else {
            throw new IOException("Socket không tồn tại cho IP: " + ip);
        }
    }
}
