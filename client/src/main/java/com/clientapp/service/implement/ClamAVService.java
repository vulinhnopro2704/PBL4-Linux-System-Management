package com.clientapp.service.implement;

import com.clientapp.enums.RequestType;
import com.clientapp.model.ClamAV;
import com.clientapp.model.ClamAVResponse;
import com.clientapp.socket.ClientSocket;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ClamAVService {
    Thread mainThread;

    public ClamAVResponse startScan(ClamAV clamAV) {
        List<String> command = buildCommand(clamAV);
        return runClamAVScan(command);
    }

    private List<String> buildCommand(ClamAV clamAV) {
        List<String> command = new ArrayList<>();
        command.add("clamscan");

        if (clamAV.recursiveCheck) command.add("-r");
        if (clamAV.infectedOnlyCheck) command.add("-i");
        if (clamAV.verboseCheck) command.add("-v");
        if (clamAV.moveCheck && !clamAV.moveDirField.isEmpty())
            command.add("--move=" + clamAV.moveDirField);
        if (clamAV.copyCheck && !clamAV.copyDirField.isEmpty())
            command.add("--copy=" + clamAV.copyDirField);
        if (clamAV.removeCheck) command.add("--remove");
        if (clamAV.scanMailCheck) command.add("--scan-mail");
        if (clamAV.scanArchiveCheck) command.add("--scan-archive");
        if (clamAV.scanPdfCheck) command.add("--scan-pdf");
        if (clamAV.scanOle2Check) command.add("--scan-ole2");
        if (!clamAV.logFileField.isEmpty()) command.add("--log=" + clamAV.logFileField);

        if (clamAV.directoryPath != null && !clamAV.directoryPath.isEmpty()) command.add(clamAV.directoryPath);

        return command;
    }


    private ClamAVResponse runClamAVScan(List<String> command) {
        List<String> suspiciousFiles = new ArrayList<>();
        StringBuilder message = new StringBuilder();
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                message.append(line).append("\n");
                if (line.contains("FOUND")) {
                    suspiciousFiles.add(line);
                }
            }
            process.waitFor();
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
        }
        return new ClamAVResponse(message.toString(), suspiciousFiles);
    }

    public void start() {
        mainThread = new Thread(() -> {
            ClientSocket clientSocket = ClientSocket.getInstance();
            Gson gson = new Gson();
            while (true) {
                try {
                    String line = null;
                    if (clientSocket.isAvailableToRead()) {
                        line = clientSocket.receiveDecryptedMessage();
                    }
                    if (line == null || line.trim().isEmpty())
                        continue;
                    try {
                        if (RequestType.valueOf(line.trim()) == RequestType.STOP_DETECT_MALWARE) {
                            break;
                        }
                    }
                    catch (IllegalArgumentException ie) {
                        ie.printStackTrace();
                    }

                    ClamAV clamAVObject = gson.fromJson(line, ClamAV.class);
                    if (clamAVObject != null) {
                        ClamAVResponse response = startScan(clamAVObject);
                        String json = gson.toJson(response);

                        clientSocket.sendEncryptedMessage(json);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            stop();
        });
        mainThread.start();
    }

    public void stop() {
        mainThread.interrupt();
        System.out.println("Stop detect malware");
    }
}
