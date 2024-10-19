package com.clientapp.util.implement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ShellCommandExecutor  {
    public static String executeShellCommand(String command) throws IOException {
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
}
