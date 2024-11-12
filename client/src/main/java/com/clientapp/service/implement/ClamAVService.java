package com.clientapp.service.implement;

import com.clientapp.model.ClamAV;
import com.clientapp.model.ClamAVResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ClamAVService {
    public void startScan(ClamAV clamAV) {
        List<String> command = buildCommand(clamAV);
        runClamAVScan(command);
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
}
