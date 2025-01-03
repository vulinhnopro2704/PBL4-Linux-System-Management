package com.clientapp.socket;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import com.clientapp.enums.RequestType;
import com.clientapp.service.implement.WatchDirectoryClamAVClient;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientSocket {
    private Socket socket;
    private String serverIp = "localhost";
    private int serverPort = 8080;
    private SecretKey aesKey;
    private InputStream inputStream;
    private OutputStream outputStream;

    private static ClientSocket _INSTANCE;

    private ClientSocket() {
        try {
            socket = new Socket(serverIp, serverPort);
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ClientSocket getInstance() {
        if (_INSTANCE == null) {
            _INSTANCE = new ClientSocket();
            watchDirectory();
        }
        return _INSTANCE;
    }

    public Socket getClientSocket() {
        return socket;
    }

    public static void watchDirectory() {
        new Thread(() -> {
            WatchDirectoryClamAVClient watchDirectoryClamAVClient = new WatchDirectoryClamAVClient();
            watchDirectoryClamAVClient.run();
        }).start();
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Boolean isAvailableToRead() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        return reader.ready();
    }

    // Method to send encrypted message
    public void sendEncryptedMessage(String message) throws Exception {
        for (RequestType requestType : RequestType.values()) {
            if (requestType.name().equals(message)) {
                System.out.println("Don't send request type by EncryptedMessage!!!");
                return;
            }
        }
        String encryptedMessage = encryptWithAES(message, aesKey);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
        writer.write(encryptedMessage + "\n");
        writer.flush();
    }

    // Method to receive and decrypt message
    public String receiveDecryptedMessage() throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        // Check if the decrypted message is a RequestType value
        String encryptedMessage = reader.readLine();
        for (RequestType requestType : RequestType.values()) {
            if (requestType.name().equals(encryptedMessage)) {
                return requestType.name();
            }
        }
        return decryptWithAES(encryptedMessage, aesKey);
    }

    public String receiveMessage() throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.readLine();
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    public void receiveDecryptedFile() throws Exception {
        String downloadDir = "D:\\FileReceive";
        try{
            DataInputStream dataInputStream = new DataInputStream(getInputStream());
//            // Đảm bảo thư mục lưu trữ tồn tại
//            File directory = new File(downloadDir);
//            if (!directory.exists() && !directory.mkdirs()) {
//                throw new IOException("Failed to create directory: " + downloadDir);
//            }

            // Nhận tên tệp
            int nameLength = dataInputStream.readInt();
            byte[] fileNameBytes = new byte[nameLength];
            dataInputStream.readFully(fileNameBytes);
            String fileName = new String(fileNameBytes, StandardCharsets.UTF_8);

            // Tạo file trong thư mục chỉ định
            File file = new File(downloadDir, fileName);
            if (file.exists()) {
                System.out.println("File already exists, renaming to avoid overwrite.");
                file = new File(downloadDir, System.currentTimeMillis() + "_" + fileName);
            }

            // Nhận kích thước tệp
            long fileSize = dataInputStream.readLong();

            try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int bytesRead;
                while (fileSize > 0 && (bytesRead = dataInputStream.read(buffer, 0, (int) Math.min(buffer.length, fileSize))) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                    fileSize -= bytesRead;
                }
            }

            System.out.println("File received: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error receiving file: " + e.getMessage());
        }
    }

    // Encrypt message with AES
    private String encryptWithAES(String plainText, SecretKey aesKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] iv = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        cipher.init(Cipher.ENCRYPT_MODE, aesKey, ivSpec);
        byte[] encryptedText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        byte[] ivAndEncryptedText = ByteBuffer.allocate(iv.length + encryptedText.length)
                .put(iv)
                .put(encryptedText)
                .array();

        return Base64.getEncoder().encodeToString(ivAndEncryptedText);
    }

    // Decrypt message with AES
    private String decryptWithAES(String encryptedText, SecretKey aesKey) throws Exception {
        System.out.println("Encrypted text: " + encryptedText);
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
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    public void sendExitCommand() {
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
            writer.write(RequestType.EXIT_COMMNAD_SCREEN.name() + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Ham gui khong ma hoa
    // Send message without encryption for testing purposes
    public void sendPlainMessage(String message) {
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
            writer.write(message + "\n");
            writer.flush();
            System.out.println("Sent plain message: " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return socket.isConnected();
    }
}

