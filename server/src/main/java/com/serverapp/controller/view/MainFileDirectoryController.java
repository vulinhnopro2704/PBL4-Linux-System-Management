package com.serverapp.controller.view;

import com.serverapp.Server;
import com.serverapp.controller.IController;
import com.serverapp.database.Redis;
import com.serverapp.enums.RequestType;
import com.serverapp.model.ClientCommnandRow;
import com.serverapp.model.FileSendDetail;
import com.serverapp.socket.SocketManager;
import com.serverapp.util.CurrentType;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;

import java.io.*;

import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;


import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.serverapp.model.ClientCredentials;
import javafx.stage.FileChooser;

import static com.serverapp.util.AlertHelper.showAlert;


public class MainFileDirectoryController implements IController {
    @FXML
    private ImageView btnUpfile;

    @FXML
    private TableView<FileSendDetail> tableFile;

    @FXML
    private TableColumn<FileSendDetail, String> fileNameCol;

    @FXML
    private TableColumn<FileSendDetail, String> ipClientColumn;

    @FXML
    private TableColumn<FileSendDetail, Double> statusCol;

    @FXML
    private TableColumn<FileSendDetail, Long> sizeFileCol;

    @FXML
    private TableView<ClientCommnandRow> tableClient;

    @FXML
    private TableColumn<ClientCommnandRow, Boolean> checkboxColumn;

    @FXML
    private TableColumn<ClientCommnandRow, String> desktopNameColumn;

    @FXML
    private TableColumn<ClientCommnandRow, String> ipAddressColumn;

    @FXML
    private TableColumn<ClientCommnandRow, String> macAddressColumn;

    private ObservableList<FileSendDetail> fileDetails;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private volatile boolean isRunning = false;

    public void setupTableColumns() {
        checkboxColumn.setCellValueFactory(cellData -> cellData.getValue().checkboxProperty());
        checkboxColumn.setCellFactory(CheckBoxTableCell.forTableColumn(checkboxColumn));
        checkboxColumn.setEditable(true);

        desktopNameColumn.setCellValueFactory(new PropertyValueFactory<>("desktopName"));
        ipAddressColumn.setCellValueFactory(new PropertyValueFactory<>("ipAddress"));
        macAddressColumn.setCellValueFactory(new PropertyValueFactory<>("macAddress"));

        checkboxColumn.setResizable(false);
        checkboxColumn.setSortable(false);
        desktopNameColumn.setResizable(false);
        desktopNameColumn.setSortable(false);
        ipAddressColumn.setResizable(false);
        ipAddressColumn.setSortable(false);
        macAddressColumn.setResizable(false);
        macAddressColumn.setSortable(false);

        update();
    }

    public void setupTableFile() {
        fileDetails = FXCollections.observableArrayList();

        fileNameCol.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        ipClientColumn.setCellValueFactory(new PropertyValueFactory<>("ipAddress"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("progress"));
        statusCol.setCellFactory(ProgressBarTableCell.forTableColumn());
        sizeFileCol.setCellValueFactory(new PropertyValueFactory<>("sizeFile"));

        fileNameCol.setResizable(false);
        fileNameCol.setSortable(false);
        ipClientColumn.setResizable(false);
        ipClientColumn.setSortable(false);
        statusCol.setResizable(false);
        statusCol.setSortable(false);
        sizeFileCol.setResizable(false);
        sizeFileCol.setSortable(false);

        tableFile.setItems(fileDetails);
    }

    @FXML
    public void initialize() {
        setupTableFile();
        setupTableColumns();
        try {
            CurrentType.getInstance().setType(RequestType.FILE_TRANSFER);
            isRunning = true;
            Platform.runLater(() -> {
                HashMap<String, ClientCredentials> clients = SocketManager.getInstance().getAllClientCredentials();
                clients.forEach((ip, clientData) -> {
                    BufferedWriter writer = null;
                    try {
                        writer = new BufferedWriter(new OutputStreamWriter(clientData.getOutputStream()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        writer.write(RequestType.FILE_TRANSFER + "\n");
                        writer.flush();

                    } catch (IOException e) {
                        log("Error initializing file transfer for client: " + ip);
                        throw new RuntimeException(e);
                    }
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update() {
        Platform.runLater(() -> {
            ObservableList<ClientCommnandRow> data = Redis.getInstance().getAllAvailableClient();
            tableClient.setItems(data);
            tableClient.setEditable(true);
        });
    }

    @FXML
    public void sendFile() {
        if (!isRunning) {
            log("File transfer is not running.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to Upload");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(btnUpfile.getScene().getWindow());
        if (selectedFile == null) {
            showAlert(Alert.AlertType.WARNING, "No File Selected", "Please select a file", "Please select at least one file to send!");
            return;
        }
        log("File selected: " + selectedFile.getAbsolutePath());

        List<ClientCommnandRow> checkedClients = tableClient.getItems().stream()
                .filter(ClientCommnandRow::isCheckbox)
                .toList();

        if (checkedClients.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Client Selected", "Please select a client", "Please select at least one client to send the file to.");
            return;
        }

        for (ClientCommnandRow clientRow : checkedClients) {
            try {
                String clientAddress = clientRow.getIpAddress();
                long fileSize = selectedFile.length();

                FileSendDetail fileDetail = new FileSendDetail(selectedFile.getName(), 0, clientAddress, fileSize);
                Platform.runLater(() -> fileDetails.add(fileDetail));

                SocketManager.getInstance().sendMessage("FILE_TRANSFER", clientAddress);
                SocketManager.getInstance().sendEncryptedFile(clientAddress, selectedFile, fileDetail);

            } catch (Exception e) {
                log("Error sending file to client: " + clientRow.getIpAddress() + " - " + e.getMessage());
            }
        }
    }

    @Override
    public void stop() {
        isRunning = false;
        log("Stopping file transfer...");

        HashMap<String, ClientCredentials> clients = SocketManager.getInstance().getAllClientCredentials();
        clients.forEach((ip, clientData) -> {
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new OutputStreamWriter(clientData.getOutputStream()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                writer.write(RequestType.EXIT_FILE_SCREEN + "\n");
                writer.flush();

            } catch (IOException e) {
                log("Error initializing file transfer for client: " + ip);
                throw new RuntimeException(e);
            }
        });
        executor.shutdown();
        executor.shutdownNow();
        log("File transfer stopped.");
    }

    public void log(String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        System.out.println("[" + timestamp + "] " + message);
    }
}
