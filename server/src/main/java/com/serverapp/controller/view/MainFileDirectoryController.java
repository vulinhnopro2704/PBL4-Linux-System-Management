package com.serverapp.controller.view;

import com.serverapp.controller.IController;
import com.serverapp.database.Redis;
import com.serverapp.model.ClientCommnandRow;
import com.serverapp.model.FileSendDetail;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.serverapp.util.AlertHelper.showAlert;

public class MainFileDirectoryController implements IController {
    @FXML
    private TableView<FileSendDetail> tableFile;
    @FXML
    private TableView<ClientCommnandRow> tableClient;
    @FXML
    private TableColumn<FileSendDetail, String> fileNameCol;
    @FXML
    private TableColumn<FileSendDetail, String> ipClientColumn;
    @FXML
    private TableColumn<FileSendDetail, Double> statusCol;
    @FXML
    private TableColumn<FileSendDetail, Long> sizeFileCol;
    @FXML
    private TableColumn<ClientCommnandRow, Boolean> checkboxColumn;
    @FXML
    private TableColumn<ClientCommnandRow, String> desktopNameColumn;
    @FXML
    private TableColumn<ClientCommnandRow, String> ipAddressColumn;
    @FXML
    private TableColumn<ClientCommnandRow, String> macAddressColumn;

    private ObservableList<FileSendDetail> fileDetails;
    private final ExecutorService clientExecutor = Executors.newCachedThreadPool(); // Thread pool để gửi file
    private final AtomicBoolean isRunning = new AtomicBoolean(true); // Trạng thái server
    private ServerSocket serverSocket;
    private final Map<String, Socket> connectedClients = new ConcurrentHashMap<>(); // Quản lý các client kết nối

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
    public void initialize() throws IOException {
        serverSocket = new ServerSocket(2208);
        startServer();
        setupTableFile();
        setupTableColumns();
    }

    /**
     * Khởi động server để lắng nghe kết nối từ các client
     */
    private void startServer() {
        clientExecutor.submit(() -> {
            log("Server is running and waiting for client connections...");
            while (isRunning.get()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    String clientIp = clientSocket.getInetAddress().getHostAddress();
                    log("Client connected: " + clientIp);

                    // Lưu client vào danh sách
                    connectedClients.put(clientIp, clientSocket);
                } catch (IOException e) {
                    if (isRunning.get()) {
                        log("Error accepting client connection: " + e.getMessage());
                    }
                }
            }
        });
    }

    /**
     * Gửi file tới các client đã chọn
     */
    @FXML
    public void sendFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to Upload");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(null);
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
            String clientIp = clientRow.getIpAddress();
            Socket clientSocket = connectedClients.get(clientIp);

            if (clientSocket == null) {
                log("Client not connected: " + clientIp);
                continue;
            }

            clientExecutor.submit(() -> {
                try (DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
                     FileInputStream fis = new FileInputStream(selectedFile)) {

                    long fileSize = selectedFile.length();
                    FileSendDetail fileDetail = new FileSendDetail(selectedFile.getName(), 0, clientIp, fileSize);
                    Platform.runLater(() -> fileDetails.add(fileDetail));

                    // Gửi thông tin file
                    dos.writeUTF("FILE_TRANSFER");
                    dos.writeUTF(selectedFile.getName());
                    dos.writeLong(fileSize);

                    // Gửi nội dung file
                    byte[] buffer = new byte[8192];
                    long totalBytesSent = 0;
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        dos.write(buffer, 0, bytesRead);
                        totalBytesSent += bytesRead;

                        double progress = (double) totalBytesSent / fileSize;
                        Platform.runLater(() -> fileDetail.setProgress(progress));
                    }

                    log("File sent successfully to " + clientIp);

                } catch (IOException e) {
                    log("Error sending file to " + clientIp + ": " + e.getMessage());
                }
            });
        }
    }

    @Override
    public void stop() {
        isRunning.set(false);
        try {
            serverSocket.close();
            connectedClients.values().forEach(clientSocket -> {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    log("Error closing client socket: " + e.getMessage());
                }
            });
        } catch (IOException e) {
            log("Error closing server socket: " + e.getMessage());
        }
        clientExecutor.shutdownNow();
        log("Server stopped.");
    }

    @Override
    public void update() {
        Platform.runLater(() -> {
            ObservableList<ClientCommnandRow> data = Redis.getInstance().getAllAvailableClient();
            tableClient.setItems(data);
            tableClient.setEditable(true);
        });
    }

    private void log(String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        System.out.println("[" + timestamp + "] " + message);
    }
}
