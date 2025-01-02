package com.serverapp.controller.view;

import com.serverapp.controller.IController;
import com.serverapp.database.Redis;
import com.serverapp.model.ClientFirewallRow;
import com.serverapp.service.IClientSecurity;
import com.serverapp.service.implement.ClientSecurity;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainSecurityController implements IController {

    @FXML
    private TableColumn<ClientFirewallRow, Boolean> checkboxColumn;

    @FXML
    private ListView<String> clientListView;

    @FXML
    private TextArea txtAreaCommand;

    @FXML
    private TableView<ClientFirewallRow> tableClient;

    @FXML
    private TableColumn<ClientFirewallRow, String> desktopNameColumn;
    @FXML
    private TableColumn<ClientFirewallRow, String> ipAddressColumn;
    @FXML
    private TableColumn<ClientFirewallRow, String> macAddressColumn;

    @FXML
    private TextArea txtAreaTerminalLogs;

    private final ObservableList<String> clientList = FXCollections.observableArrayList();
    private final IClientSecurity clientSecurityService = new ClientSecurity(this);

    private ContextMenu contextMenu = new ContextMenu();
    @FXML
    public void initialize() {
        clientSecurityService.initialize();
        MenuItem Item1 = new MenuItem("Xem cấu hình tường lửa");
        MenuItem Item2 = new MenuItem("Cấu hình tường lửa cho Client");
        MenuItem Item3 = new MenuItem("Exit");

        contextMenu.getItems().addAll(Item1, Item2, Item3);
    }

    @Override
    public void update() {
    }

    public void setupTableColumns(ObservableList<ClientFirewallRow> data) {
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

        tableClient.setItems(data);
        tableClient.setEditable(true);
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

    // Hàm xử lý sự kiện chuột
    public void clickHandler(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) {  // Chuột phải
            if (tableClient.getSelectionModel().getSelectedItem() != null) {
                contextMenu.show(tableClient, event.getScreenX(), event.getScreenY());
                System.out.println("Right-click detected on: " + tableClient.getSelectionModel().getSelectedItem());
            }
        } else if (event.getButton() == MouseButton.PRIMARY) { // Chuột trái
            if (tableClient.getSelectionModel().getSelectedItem() != null) {
                System.out.println(tableClient.getSelectionModel().getSelectedItem());
            }
        }
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
    }

    @FXML
    public void sendCommand() throws Exception {

    }

    public void log(String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String logEntry = "[" + timestamp + "] " + message;
        Platform.runLater(() -> txtAreaTerminalLogs.appendText(logEntry + "\n"));
    }
}
