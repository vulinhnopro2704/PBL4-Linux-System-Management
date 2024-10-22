package com.clientapp.service.implement;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import com.clientapp.socket.ClientSocket;
import com.clientapp.service.ISetupConnection;
import static com.clientapp.util.implement.EncodeDecoder.encryptAESKey;
import static com.clientapp.util.implement.EncodeDecoder.generateAESKey;
import static com.clientapp.util.implement.EncodeDecoder.getPublicKeyFromString;

public class SetupConnection implements ISetupConnection {
    @Override
    public void start() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(ClientSocket.getInstance().getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(ClientSocket.getInstance().getOutputStream()));

            // Receive RSA public key from the server
            String rsaPublicKeyString = reader.readLine().trim();

            if (rsaPublicKeyString == null || rsaPublicKeyString.isEmpty()) {
                System.out.println("RSA public key is empty");
                return;
            }
            System.out.println("Received RSA Public Key: " + rsaPublicKeyString);
            PublicKey rsaPublicKey = getPublicKeyFromString(rsaPublicKeyString);

            // Generate AES key and encrypt it using RSA public key
            SecretKey aesKey = generateAESKey();
            String encryptedAesKey = encryptAESKey(aesKey, rsaPublicKey);
            System.out.println("AES key: " + encryptedAesKey);
            writer.write(encryptedAesKey + "\n");
            writer.flush();
            ClientSocket.getInstance().setAesKey(aesKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        // Implementation for stopping the connection if needed
    }
}
