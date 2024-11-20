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
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainSecurityController implements IController {

    @FXML
    private TableColumn<ClientFirewallRow, Boolean> checkboxColumn;

    @FXML
    private ListView<String> clientListView;

    @FXML
    private Button blackListButton;

    @FXML
    private Button whiteListButton;

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
    private final ObservableList<String> blackListedClients = FXCollections.observableArrayList();
    private final ObservableList<String> whiteListedClients = FXCollections.observableArrayList();
    private final IClientSecurity clientSecurityService = new ClientSecurity(this);

    private ContextMenu contextMenu = new ContextMenu();

    @FXML
    public void initialize() {
        clientSecurityService.initialize();
        MenuItem Item1 = new MenuItem("Xem cấu hình tường lửa");
        MenuItem Item2 = new MenuItem("Cấu hình tường lửa cho Client");
        MenuItem Item3 = new MenuItem("Exit");

        contextMenu.getItems().addAll(Item1, Item2, Item3);

        Item1.setOnAction(event -> {
            try {
                sendCommand(tableClient.getSelectionModel().getSelectedItem());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // Gán sự kiện cho các button
        blackListButton.setOnAction(event -> showBlackListClients());
        whiteListButton.setOnAction(event -> showWhitelist());
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

    public void addToBlackList(String ip) {
        if (!blackListedClients.contains(ip)) {
            blackListedClients.add(ip);
        }
    }

    public void addToWhiteList(String ip) {
        if (!whiteListedClients.contains(ip)) {
            whiteListedClients.add(ip);
        }
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

    @FXML
    private void showBlackListClients() {
        try {
            // Tạo cửa sổ mới (Stage) và thiết lập controller
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/component/black-list.fxml")); // Đảm bảo đường dẫn đúng với file FXML của bạn
            Parent root = loader.load();
            BlacklistController controller = loader.getController();
            Stage stage = new Stage();
            controller.setStage(stage); // Gán Stage cho controller

            // Hiển thị cửa sổ
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void showWhitelist() {
        try {
            // Tạo cửa sổ mới (Stage) và thiết lập controller
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/component/white-list.fxml"));
            Parent root = loader.load();
            WhitelistController controller = loader.getController();
            Stage stage = new Stage();
            controller.setStage(stage); // Gán Stage cho controller

            // Hiển thị cửa sổ
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        clientSecurityService.close();
    }

    @FXML
    public void sendCommand(ClientFirewallRow clientFirewallRow) throws Exception {
        String command = "sudo iptables -L -v -n";
        clientSecurityService.sendCommand(command, clientFirewallRow, txtAreaTerminalLogs);
    }

    public void log(String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String logEntry = "[" + timestamp + "] " + message;
        Platform.runLater(() -> txtAreaTerminalLogs.appendText(logEntry + "\n"));
    }
}
