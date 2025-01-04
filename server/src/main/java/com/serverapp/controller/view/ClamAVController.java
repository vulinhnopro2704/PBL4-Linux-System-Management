package com.serverapp.controller.view;

import com.serverapp.controller.IController;
import com.serverapp.database.Redis;
import com.serverapp.enums.RequestType;
import com.serverapp.model.ClamAV;
import com.serverapp.model.ClientCard;
import com.serverapp.model.ClientCommnandRow;
import com.serverapp.model.ClientDetail;
import com.serverapp.service.implement.DetectMalware;
import com.serverapp.socket.SocketManager;
import com.serverapp.util.CurrentType;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClamAVController implements IController {
    @FXML private CheckBox recursiveCheck, infectedOnlyCheck, verboseCheck, moveCheck, copyCheck, removeCheck, scanMailCheck, scanArchiveCheck, scanPdfCheck, scanOle2Check;
    @FXML private TextField moveDirField, copyDirField, logFileField, directoryField;
    @FXML private Button chooseDirectoryButton, scanButton;
    @FXML private ToggleButton realtimeScanToggle;
    @FXML private TextArea resultArea;
    DetectMalware detectMalware;

    @FXML
    public void initialize() {
        detectMalware = new DetectMalware();
        chooseDirectoryButton.setOnAction(e -> chooseDirectory());
        scanButton.setOnAction(e -> startScan());
        realtimeScanToggle.setOnAction(e -> toggleRealtimeScan());
        CurrentType.getInstance().setType(RequestType.DETECT_MALWARE);
    }

    @Override
    public void update() {
    }

    @Override
    public void stop() throws IOException {

    }

    private void chooseDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(new Stage());
        if (selectedDirectory != null) {
            directoryField.setText(selectedDirectory.getAbsolutePath());
        }
    }

    private void startScan() {
        if (directoryField.getText().trim().length() > 0) {
            ClamAV clamAV = ClamAV.builder()
                    .directoryPath(directoryField.getText())
                    .recursiveCheck(recursiveCheck.isSelected())
                    .moveCheck(moveCheck.isSelected())
                    .removeCheck(removeCheck.isSelected())
                    .copyCheck(copyCheck.isSelected())
                    .infectedOnlyCheck(infectedOnlyCheck.isSelected())
                    .copyDirField(copyDirField.getText())
                    .scanArchiveCheck(scanArchiveCheck.isSelected())
                    .moveDirField(moveDirField.getText())
                    .scanMailCheck(scanMailCheck.isSelected())
                    .scanOle2Check(scanOle2Check.isSelected())
                    .scanPdfCheck(scanPdfCheck.isSelected())
                    .verboseCheck(verboseCheck.isSelected())
                    .logFileField(logFileField.getText())
                    .build();

            // Xây dựng lệnh clamscan
            List<String> command = buildClamscanCommand(clamAV);

            // Chạy lệnh clamscan trên server
            runClamscan(command);
        }
    }

    private List<String> buildClamscanCommand(ClamAV clamAV) {
        List<String> command = new ArrayList<>();
        command.add("clamscan");

        // Thêm các tùy chọn vào lệnh từ đối tượng ClamAV
        if (clamAV.getRecursiveCheck() != null && clamAV.getRecursiveCheck()) command.add("-r");
        if (clamAV.getMoveCheck() != null && clamAV.getMoveCheck()) command.add("--move=" + clamAV.getMoveDirField());
        if (clamAV.getCopyCheck() != null && clamAV.getCopyCheck()) command.add("--copy=" + clamAV.getCopyDirField());
        if (clamAV.getRemoveCheck() != null && clamAV.getRemoveCheck()) command.add("--remove");
        if (clamAV.getInfectedOnlyCheck() != null && clamAV.getInfectedOnlyCheck()) command.add("--infected");
        if (clamAV.getScanArchiveCheck() != null && clamAV.getScanArchiveCheck()) command.add("--archive");
        if (clamAV.getScanMailCheck() != null && clamAV.getScanMailCheck()) command.add("--mail");
        if (clamAV.getScanOle2Check() != null && clamAV.getScanOle2Check()) command.add("--ole2");
        if (clamAV.getScanPdfCheck() != null && clamAV.getScanPdfCheck()) command.add("--pdf");
        if (clamAV.getVerboseCheck() != null && clamAV.getVerboseCheck()) command.add("--verbose");
        if (clamAV.getLogFileField() != null && !clamAV.getLogFileField().isEmpty()) {
            command.add("--log=" + clamAV.getLogFileField());
        }

        // Thêm đường dẫn thư mục cần quét vào cuối lệnh
        command.add(clamAV.getDirectoryPath());

        return command;
    }

    private void runClamscan(List<String> command) {
        try {
            // Tạo ProcessBuilder và chạy lệnh
            System.out.println("Running clamscan with command: " + command);
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true); // Kết hợp cả output và error stream

            // Thực thi lệnh
            Process process = processBuilder.start();

            // Đọc output stream của lệnh clamscan
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder output = new StringBuilder();
            List<String> suspiciousFiles = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                if (line.contains("FOUND")) {
                    suspiciousFiles.add(line); // Thêm các tệp đáng nghi vào danh sách
                }
            }

            // Hiển thị kết quả quét trong TextArea
            Platform.runLater(() -> resultArea.setText(output.toString()));

            // Hiển thị cảnh báo nếu tìm thấy tệp đáng nghi
            if (!suspiciousFiles.isEmpty()) {
                Platform.runLater(() -> showSuspiciousFilesAlert(suspiciousFiles));
            }
        } catch (IOException e) {
            e.printStackTrace();
            Platform.runLater(() -> showAlert("Error", "An error occurred while running Clamscan", Alert.AlertType.ERROR));
        }
    }


    private void toggleRealtimeScan() {

    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuspiciousFilesAlert(List<String> suspiciousFiles) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Cảnh báo");
        alert.setHeaderText("Tìm thấy các tệp đáng nghi!");

        VBox fileListContainer = new VBox();
        for (String file : suspiciousFiles) {
            TitledPane titledPane = new TitledPane(file, new Label(file));
            fileListContainer.getChildren().add(titledPane);
        }

        ScrollPane scrollPane = new ScrollPane(fileListContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(200);

        alert.getDialogPane().setContent(scrollPane);
        alert.showAndWait();
    }
}
