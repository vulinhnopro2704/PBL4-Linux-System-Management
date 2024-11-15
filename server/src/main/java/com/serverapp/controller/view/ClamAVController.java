package com.serverapp.controller.view;

import com.serverapp.controller.IController;
import com.serverapp.database.Redis;
import com.serverapp.model.ClamAV;
import com.serverapp.model.ClientCard;
import com.serverapp.model.ClientCommnandRow;
import com.serverapp.model.ClientDetail;
import com.serverapp.service.implement.DetectMalware;
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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class ClamAVController implements IController {
    @FXML private CheckBox recursiveCheck, infectedOnlyCheck, verboseCheck, moveCheck, copyCheck, removeCheck, scanMailCheck, scanArchiveCheck, scanPdfCheck, scanOle2Check;
    @FXML private TextField moveDirField, copyDirField, logFileField, directoryField;
    @FXML private Button chooseDirectoryButton, scanButton;
    @FXML private ToggleButton realtimeScanToggle;
    @FXML private TextArea resultArea;
    @FXML private TableView<ClientCommnandRow> clientTable;
    @FXML private TableColumn<ClientCommnandRow, Boolean> checkBoxColumn;
    @FXML private TableColumn<ClientCommnandRow, String> hostNameColumn, ipAddressColumn, macAddressColumn;

    DetectMalware detectMalware;

    @FXML
    public void initialize() {
        detectMalware = new DetectMalware();
        chooseDirectoryButton.setOnAction(e -> chooseDirectory());
        scanButton.setOnAction(e -> startScan());
        realtimeScanToggle.setOnAction(e -> toggleRealtimeScan());
        setupTableColumns();
    }

    public void setupTableColumns() {
        checkBoxColumn.setCellValueFactory(cellData -> cellData.getValue().checkboxProperty());
        checkBoxColumn.setCellFactory(CheckBoxTableCell.forTableColumn(checkBoxColumn));
        checkBoxColumn.setEditable(true);

        hostNameColumn.setCellValueFactory(new PropertyValueFactory<>("desktopName"));
        ipAddressColumn.setCellValueFactory(new PropertyValueFactory<>("ipAddress"));
        macAddressColumn.setCellValueFactory(new PropertyValueFactory<>("macAddress"));

        checkBoxColumn.setResizable(false);
        checkBoxColumn.setSortable(false);
        hostNameColumn.setResizable(false);
        hostNameColumn.setSortable(false);
        ipAddressColumn.setResizable(false);
        ipAddressColumn.setSortable(false);
        macAddressColumn.setResizable(false);
        macAddressColumn.setSortable(false);

        update();
    }

    @Override
    public void update() {
        Platform.runLater(() -> {
            ObservableList<ClientCommnandRow> data = Redis.getInstance().getAllAvailableClient();
            clientTable.setItems(data);
            clientTable.setEditable(true);
        });
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
                    .copyCheck(copyCheck.isSelected())
                    .copyDirField(copyDirField.getText())
                    .scanArchiveCheck(scanArchiveCheck.isSelected())
                    .moveDirField(moveDirField.getText())
                    .scanMailCheck(scanMailCheck.isSelected())
                    .scanOle2Check(scanOle2Check.isSelected())
                    .scanPdfCheck(scanPdfCheck.isSelected())
                    .build();
            List<String> checkedClient = clientTable.getItems().stream()
                    .filter(row -> row.checkboxProperty().get()) // Filter rows where the checkbox is checked
                    .map(row -> row.getIpAddress())     // Map to the desired property (e.g., a String)
                    .toList();               // Collect the results into a List

            detectMalware.send(checkedClient, clamAV);
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
