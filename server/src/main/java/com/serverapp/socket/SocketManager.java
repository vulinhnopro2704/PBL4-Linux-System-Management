package com.serverapp.socket;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.serverapp.controller.view.AppController;
import com.serverapp.enums.RequestType;
import com.serverapp.model.ClientCredentials;

import com.serverapp.service.implement.WatchDirectoryClamAVServer;
import com.serverapp.util.CurrentType;
import javafx.application.Platform;
import lombok.Getter;

import static com.serverapp.util.PushNotification.showInformationNotification;

/**
 * SocketManager là một lớp quản lý tất cả các kết nối từ phía server.
 * Nó xử lý việc gửi và nhận dữ liệu giữa server và client thông qua AES và RSA encryption.
 */
public class SocketManager {
    private static SocketManager _INSTANCE; // Singleton instance của SocketManager
    private HashMap<String, ClientCredentials> socketMap; // Lưu trữ thông tin các client đang kết nối
    private PublicKey rsaPublicKey;  // Khóa công khai RSA để gửi cho client
    private PrivateKey rsaPrivateKey; // Khóa riêng RSA để giải mã AES key
    @Getter
    private ServerSocket serverSocket; // Socket server để lắng nghe kết nối
    @Getter
    private final int port = 8080; // Cổng server
    private ExecutorService executorService = Executors.newCachedThreadPool(); // Thread pool để xử lý nhiều kết nối

    // Phương thức để lấy thể hiện duy nhất của Singleton
    /**
     * Trả về instance duy nhất của SocketManager (Singleton pattern).
     * Nếu instance chưa tồn tại, sẽ tạo mới.
     */
    public static synchronized SocketManager getInstance() {
        if (_INSTANCE == null) {
            _INSTANCE = new SocketManager();
            WatchDirectory();
        }
        return _INSTANCE;
    }

    // Private constructor để ngăn chặn việc khởi tạo từ bên ngoài
    private SocketManager() {
        socketMap = new HashMap<>();
        generateRSAKeys();  // Tạo cặp khóa RSA
        if (serverSocket == null) {
            try {
                serverSocket = new ServerSocket(port);
                startListening(); // Bắt đầu lắng nghe các kết nối
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void WatchDirectory() {
        new Thread(() -> {
            WatchDirectoryClamAVServer watchDirectoryClamAVServer = new WatchDirectoryClamAVServer(12345);
            watchDirectoryClamAVServer.start();
        }).start();
    }

    /**
     * Tạo một cặp khóa RSA (gồm private và public key).
     */
    private void generateRSAKeys() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair pair = keyGen.generateKeyPair();
            this.rsaPublicKey = pair.getPublic();
            this.rsaPrivateKey = pair.getPrivate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Bắt đầu lắng nghe các kết nối từ client và xử lý bằng thread pool.
     */
    public void startListening() {
        new Thread(() -> {
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    executorService.submit(() -> handleClientConnection(clientSocket));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Xử lý kết nối từ client, gửi public key RSA và nhận AES key.
     *
     * @param clientSocket Socket của client kết nối tới server
     */
    private void handleClientConnection(Socket clientSocket) {
        try {
            System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

            // Send COMMAND request to client
            // Gửi yêu cầu kết nối đến client
            writer.write(RequestType.CONNECTION + "\n");
            writer.flush();

            // Send RSA public key to client
            // Gửi khóa công khai RSA đến client
            String RSAPublicKey = Base64.getEncoder().encodeToString(this.rsaPublicKey.getEncoded());
            writer.write(RSAPublicKey + "\n");
            writer.flush();
            System.out.println("Public Key Sent");

            // Receive encrypted AES key from the client
            // Nhận AES key được mã hóa từ client
            String encryptedAesKey = reader.readLine();
            if (encryptedAesKey != null) {
                try {
                    // Giải mã AES key bằng RSA private key
                    SecretKey aesKey = decryptAESKey(encryptedAesKey);
                    String ip = clientSocket.getInetAddress().getHostAddress();
                    System.out.println("Client is ready: " + clientSocket.getInetAddress().getHostAddress());
                    // Lưu thông tin client và AES key vào HashMap
                    addClientCredentials(ip, new ClientCredentials(clientSocket, aesKey));
                    showInformationNotification("Client connected", "Client connected: " + ip);
                } catch (Exception e) {
                    System.out.println("Error decrypting AES key: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("Failed to receive AES key from client: " + clientSocket.getInetAddress().getHostAddress());
            }
        } catch (Exception e) {
            System.out.println("(Handle Client) Client disconnected: " + clientSocket.getInetAddress().getHostAddress() + " - " + e.getMessage());
            e.printStackTrace();
        }
        //Mọi cập nhật giao diện đều phải chạy bằng Platform.runLater
        Platform.runLater(() -> {
            AppController.getInstance().update(); // Cập nhật UI
        });
    }

    /**
     * Gửi trạng thái hiện tại đến client dựa trên địa chỉ IP.
     *
     * @param ipAddress Địa chỉ IP của client
     */
    public void sendCurrentRequestType(String ipAddress) {
        ClientCredentials clientCredentials = getClientCredentials(ipAddress);
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientCredentials.getOutputStream()));
            writer.write(CurrentType.getInstance().getType() + "\n");
            showInformationNotification("Current Request Type", "Sent Current Request Type: " + CurrentType.getInstance().getType());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Thêm Socket vào HashMap với Key là địa chỉ IP
    /**
     * Thêm thông tin của client vào danh sách quản lý, với địa chỉ IP là khóa.
     *
     * @param ip Địa chỉ IP của client
     * @param clientCredentials Thông tin của client (bao gồm Socket và AES key)
     */
    public synchronized void addClientCredentials(String ip, ClientCredentials clientCredentials) {
        socketMap.put(ip, clientCredentials);
    }

    // Xóa Socket khỏi HashMap dựa vào địa chỉ IP
    /**
     * Xóa thông tin của client khỏi danh sách quản lý.
     *
     * @param ip Địa chỉ IP của client
     */
    public synchronized void removeClientCredentials(String ip) {
        ClientCredentials clientCredentials = socketMap.get(ip);
        try {
            clientCredentials.getSocket().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        socketMap.remove(ip);
    }

    // Lấy Socket dựa vào địa chỉ IP
    /**
     * Lấy thông tin của client dựa vào địa chỉ IP.
     *
     * @param ip Địa chỉ IP của client
     * @return Thông tin của client (ClientCredentials)
     */
    public synchronized ClientCredentials getClientCredentials(String ip) {
        return socketMap.get(ip);
    }

    // Lấy tất cả các Socket hiện tại (nếu cần)
    public synchronized HashMap<String, ClientCredentials> getAllClientCredentials() {
        return new HashMap<>(socketMap); // Trả về bản sao của HashMap để tránh sửa đổi ngoài ý muốn
    }

    // Kiểm tra xem một địa chỉ IP có đang kết nối không
    /**
     * Kiểm tra xem client với địa chỉ IP đã kết nối chưa.
     *
     * @param ip Địa chỉ IP của client
     * @return true nếu client đang kết nối, false nếu không
     */
    public synchronized boolean containsClientCredentials(String ip) {
        return socketMap.containsKey(ip);
    }

    /**
     * Giải mã AES key đã mã hóa bằng RSA.
     *
     * @param encryptedKey AES key đã được mã hóa
     * @return SecretKey AES key đã giải mã
     * @throws Exception Nếu có lỗi trong quá trình giải mã
     */
    public SecretKey decryptAESKey(String encryptedKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, rsaPrivateKey);
        byte[] decodedKey = Base64.getDecoder().decode(encryptedKey);
        System.out.println("Decoded Key Length: " + decodedKey.length);
        byte[] aesKey = cipher.doFinal(decodedKey);
        return new SecretKeySpec(aesKey, "AES");
    }


    // Method to receive and decrypt the message
    /**
     * Nhận và giải mã thông điệp từ client.
     *
     * @param ipAddress Địa chỉ IP của client
     * @return Thông điệp đã được giải mã
     * @throws Exception Nếu có lỗi trong quá trình giải mã
     */
    public String receiveDecryptedMessage(String ipAddress) throws Exception {
        ClientCredentials clientCredentials = getClientCredentials(ipAddress);
        BufferedReader reader = new BufferedReader(new InputStreamReader(clientCredentials.getInputStream()));
        SecretKey aesKey = clientCredentials.getAesKey();
        String encryptedMessage = reader.readLine();
        // Check if the decrypted message is a RequestType value
        for (RequestType requestType : RequestType.values()) {
            if (requestType.name().equals(encryptedMessage)) {
                return requestType.name();
            }
        }
        System.out.println("Encrypted Message: " + encryptedMessage);
        String decryptedMessage =  decryptWithAES(encryptedMessage, aesKey);
        System.out.println("Decrypted Message: " + decryptedMessage);
        return decryptedMessage;
    }

    // Method to send encrypted message
    /**
     * Gửi thông điệp đã mã hóa tới client.
     *
     * @param message Thông điệp cần gửi
     * @param ipAddress Địa chỉ IP của client
     * @throws Exception Nếu có lỗi trong quá trình mã hóa
     */
    public void sendEncryptedMessage(String message, String ipAddress) throws Exception {
        ClientCredentials clientCredentials = getClientCredentials(ipAddress);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientCredentials.getOutputStream()));
        SecretKey aesKey = clientCredentials.getAesKey();
        String encryptedMessage = encryptWithAES(message, aesKey);
        System.out.println("Encrypted Message: " + encryptedMessage);
        writer.write(encryptedMessage + "\n");
        writer.flush();
    }

    private boolean isStreamClosed(DataInputStream stream) {
        try {
            stream.mark(1); // Đánh dấu để kiểm tra
            if (stream.read() == -1) {
                return true; // Đạt đến cuối luồng
            }
            stream.reset(); // Đặt lại nếu không phải cuối
            return false;
        } catch (IOException e) {
            return true; // Xảy ra lỗi nghĩa là luồng đã đóng
        }
    }

    // Encrypt with AES
    private String encryptWithAES(String plainText, SecretKey aesKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] iv = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        cipher.init(Cipher.ENCRYPT_MODE, aesKey, ivSpec);
        byte[] encryptedText = cipher.doFinal(plainText.getBytes("UTF-8"));

        byte[] ivAndEncryptedText = ByteBuffer.allocate(iv.length + encryptedText.length)
                .put(iv)
                .put(encryptedText)
                .array();

        return Base64.getEncoder().encodeToString(ivAndEncryptedText);
    }

    // Decrypt with AES
    /**
     * Mã hóa thông điệp sử dụng AES với chế độ CBC và PKCS5Padding.
     *
     * @param encryptedText Thông điệp gốc
     * @param aesKey Khóa AES
     * @return Thông điệp đã mã hóa (dạng Base64)
     * @throws Exception Nếu có lỗi trong quá trình mã hóa
     */
    private String decryptWithAES(String encryptedText, SecretKey aesKey) throws Exception {
        System.out.println("Encrypted Text: " + encryptedText);
        byte[] ivAndEncryptedText = Base64.getDecoder().decode(encryptedText);

        ByteBuffer byteBuffer = ByteBuffer.wrap(ivAndEncryptedText);
        byte[] iv = new byte[16];
        byteBuffer.get(iv);
        byte[] encryptedBytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(encryptedBytes);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, aesKey, ivSpec);

        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, "UTF-8");
    }

    public Boolean isAvailableToRead(String ipAddress) throws IOException {
        Socket clientSocket = getClientCredentials(ipAddress).getSocket();
        if (clientSocket == null) return false;
        BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        return reader.ready();
    }

    /**
     * Dừng server và đóng kết nối.
     * Chỉ dùng khi kết thúc chương trình
     *
     * @throws IOException Nếu có lỗi trong quá trình đóng server
     */
    public void stop() throws IOException {
        if (serverSocket != null) {
            serverSocket.close();
            _INSTANCE = null;
        }
    }
}
