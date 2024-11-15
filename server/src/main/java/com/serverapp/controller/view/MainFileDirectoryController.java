package com.serverapp.controller.view;

import com.serverapp.controller.IController;
import com.serverapp.database.Redis;
import com.serverapp.enums.RequestType;
import com.serverapp.model.ClientCommnandRow;
import com.serverapp.model.ClientCredentials;
import com.serverapp.model.FileSendDetail;
import com.serverapp.socket.SocketManager;
import com.serverapp.util.CurrentType;
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

    private final ObservableList<FileSendDetail> fileDetails = FXCollections.observableArrayList();
    private final ExecutorService clientExecutor = Executors.newFixedThreadPool(5);
    private final AtomicBoolean isRunning = new AtomicBoolean(true);
    private static final Map<String, Socket> connectedClients = new ConcurrentHashMap<>();
    private int port;

    @FXML
    public void initialize() throws IOException {
        SocketManager.getInstance().FileSend();
        setupTableFile();
        setupTableClient();
        try {
            CurrentType.getInstance().setType(RequestType.FILE_TRANSFER);
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

    private void setupTableClient() {
        checkboxColumn.setCellValueFactory(cellData -> cellData.getValue().checkboxProperty());
        checkboxColumn.setCellFactory(CheckBoxTableCell.forTableColumn(checkboxColumn));
        checkboxColumn.setEditable(true);
        setupTableColumn(desktopNameColumn, "desktopName");
        setupTableColumn(ipAddressColumn, "ipAddress");
        setupTableColumn(macAddressColumn, "macAddress");
        update();
    }

    private void setupTableFile() {
        setupTableColumn(fileNameCol, "fileName");
        setupTableColumn(ipClientColumn, "ipAddress");
        setupTableColumn(sizeFileCol, "sizeFile");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("progress"));
        statusCol.setCellFactory(ProgressBarTableCell.forTableColumn());
        tableFile.setItems(fileDetails);
    }

    private <T> void setupTableColumn(TableColumn<T, ?> column, String property) {
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setResizable(false);
        column.setSortable(false);
    }

    public void start(int port) {
        this.port = port;
        clientExecutor.execute(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                log("Server is listening on port " + port);
                while (isRunning.get()) {
                    Socket socket = serverSocket.accept();
                    log("New client connected");
                    clientExecutor.execute(() -> handleClient(socket));
                }
            } catch (IOException e) {
                logError("Error starting server", e);
            }
        });
    }

    private void handleClient(Socket socket) {
        String clientIp = socket.getInetAddress().getHostAddress();
        log("Connected to client: " + clientIp);
        connectedClients.put(clientIp, socket);
        try {
            while (socket.isConnected()) {
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            logError("Error handling client: " + clientIp, e);
        } finally {
            log("Client disconnected: " + clientIp);
        }
    }

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

        List<ClientCommnandRow> checkedClients = tableClient.getItems().stream()
                .filter(ClientCommnandRow::isCheckbox)
                .toList();

        if (checkedClients.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Client Selected", "Please select a client", "Please select at least one client to send the file to.");
            return;
        }

        for (ClientCommnandRow clientRow : checkedClients) {
            clientExecutor.execute(() -> sendFileToClient(selectedFile, clientRow.getIpAddress()));

        }
    }

    private void sendFileToClient(File file, String clientIp) {
        Socket clientSocket = connectedClients.get(clientIp);
        if (clientSocket == null) {
            log("Client " + clientIp + " is not connected.");
            return;
        }

        try {
            DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
            FileInputStream fis = new FileInputStream(file);
            long fileSize = file.length();
            FileSendDetail fileDetail = new FileSendDetail(file.getName(), 0, clientIp, fileSize);
            Platform.runLater(() -> fileDetails.add(fileDetail));

            dos.writeUTF("FILE_TRANSFER");
            dos.writeUTF(file.getName());
            dos.writeLong(fileSize);

            byte[] buffer = new byte[8192];
            long totalBytesSent = 0;
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, bytesRead);
                totalBytesSent += bytesRead;

                double progress = (double) totalBytesSent / fileSize;
                Platform.runLater(() -> fileDetail.setProgress(progress));
            }

            log("File sent successfully to client: " + clientIp);

        } catch (IOException e) {
            logError("Error sending file to client: " + clientIp, e);
        }
    }

    @Override
    public void stop() {
        isRunning.set(false);
        clientExecutor.shutdownNow();
        CurrentType.getInstance().setType(RequestType.EXIT_FILE_SCREEN);
        log("Server stopped.");
        System.out.println("Server File Send stopped.");
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

    private void logError(String message, Exception e) {
        log(message + " - " + e.getMessage());
        e.printStackTrace();
    }
}

