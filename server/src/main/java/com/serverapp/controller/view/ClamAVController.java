package com.serverapp.controller.view;

import com.serverapp.controller.IController;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ClamAVController implements IController {
    @FXML private CheckBox recursiveCheck, infectedOnlyCheck, verboseCheck, moveCheck, copyCheck, removeCheck, scanMailCheck, scanArchiveCheck, scanPdfCheck, scanOle2Check;
    @FXML private TextField moveDirField, copyDirField, logFileField, directoryField;
    @FXML private Button chooseDirectoryButton, scanButton;
    @FXML private ToggleButton realtimeScanToggle;
    @FXML private TextArea resultArea;

    public void initialize() {
        chooseDirectoryButton.setOnAction(e -> chooseDirectory());
        scanButton.setOnAction(e -> startScan());
        realtimeScanToggle.setOnAction(e -> toggleRealtimeScan());
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
