package com.serverapp.controller.component;

import com.serverapp.model.ClientProcess;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
public class ClientProcessController {

    // Liên kết với bảng và các cột trong FXML
    @FXML
    private TableView<ClientProcess> tableView;

    @FXML
    private TableColumn<ClientProcess, String> processIDCol;

    @FXML
    private TableColumn<ClientProcess, String> processNameCol;

    @FXML
    private TableColumn<ClientProcess, String> processPathCol;

    @FXML
    private TableColumn<ClientProcess, String> cpuUsageCol;

    @FXML
    private TableColumn<ClientProcess, String> ramUsageCol;

    // Khởi tạo bảng và liên kết dữ liệu với các cột
    @FXML
    public void initialize() {
        // Liên kết cột với các thuộc tính của ClientProcess
        processIDCol.setCellValueFactory(new PropertyValueFactory<>("processID"));
        processNameCol.setCellValueFactory(new PropertyValueFactory<>("processName"));
        processPathCol.setCellValueFactory(new PropertyValueFactory<>("processPath"));
        cpuUsageCol.setCellValueFactory(new PropertyValueFactory<>("CPUusage"));
        ramUsageCol.setCellValueFactory(new PropertyValueFactory<>("RAMusage"));

        // Ví dụ dữ liệu mẫu
        ObservableList<ClientProcess> processList = FXCollections.observableArrayList(
                new ClientProcess("123", "Process A", "/path/to/A", "25%", "120MB"),
                new ClientProcess("456", "Process B", "/path/to/B", "15%", "512MB")
        );

        // Thiết lập dữ liệu cho bảng
        tableView.setItems(processList);
    }

    // Hàm này có thể gọi để cập nhật dữ liệu từ List<ClientProcess>
    public void updateTableData(List<ClientProcess> clientProcesses) {
        ObservableList<ClientProcess> observableList = FXCollections.observableArrayList(clientProcesses);
        tableView.setItems(observableList);
    }
}

