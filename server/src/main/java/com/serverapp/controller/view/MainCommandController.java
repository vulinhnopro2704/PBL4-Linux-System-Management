package com.serverapp.controller.view;

import com.serverapp.controller.IController;
import com.serverapp.database.Redis;
import com.serverapp.model.ClientCommnandRow;
import com.serverapp.service.IClientCommand;
import com.serverapp.service.implement.ClientCommand;
import com.serverapp.socket.SocketManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainCommandController implements IController {
    @FXML
    private TableColumn<ClientCommnandRow, Boolean> checkboxColumn;

    @FXML
    private ListView<String> clientListView;

    @FXML
    private TextArea txtAreaCommand;

    @FXML
    private TableView<ClientCommnandRow> tableClient;

    @FXML
    private TableColumn<ClientCommnandRow, String> desktopNameColumn;
    @FXML
    private TableColumn<ClientCommnandRow, String> ipAddressColumn;
    @FXML
    private TableColumn<ClientCommnandRow, String> macAddressColumn;

    @FXML
    private TextArea txtAreaTerminalLogs;

    private final ObservableList<String> clientList = FXCollections.observableArrayList();
    private final IClientCommand  clientCommandService = new ClientCommand(this);

    @FXML
    public void initialize() {
        setupTableColumns();
        clientCommandService.initialize();
    }

    @Override
    public void update() {
        Platform.runLater(() -> {
            ObservableList<ClientCommnandRow> data = Redis.getInstance().getAllAvailableClient();
            tableClient.setItems(data);
            tableClient.setEditable(true);
        });
    }

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

    public void addClientToList(String ip) {
        clientList.add(ip);
        clientListView.setItems(clientList);
        clientListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateConsole();
            }
        });
    }

    public void updateConsole() {
        String selectedClient = clientListView.getSelectionModel().getSelectedItem();
        if (selectedClient == null) {
            return;
        }
        String consoleLogs = Redis.getInstance().getConsoleLogs(selectedClient);
        Platform.runLater(() -> txtAreaTerminalLogs.setText(consoleLogs));
    }

    @Override
    public void stop() {
        clientCommandService.close();
    }

    @FXML
    public void sendCommand() throws Exception {
        String command = txtAreaCommand.getText().trim();
        clientCommandService.sendCommand(command, tableClient, txtAreaTerminalLogs);
    }

    public void log(String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String logEntry = "[" + timestamp + "] " + message;
        Platform.runLater(() -> txtAreaTerminalLogs.appendText(logEntry + "\n"));
    }
}