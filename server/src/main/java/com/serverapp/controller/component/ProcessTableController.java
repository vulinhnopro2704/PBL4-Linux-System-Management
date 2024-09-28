package com.serverapp.controller.component;

import com.serverapp.model.ClientDetail;
import com.serverapp.model.ClientProcess;
import com.serverapp.model.Redis;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProcessTableController {
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

    private String clientIp; // Thêm biến để lưu IP client

    public void setClientIp(String ip) {
        this.clientIp = ip;
        System.out.println("Received IP in ProcessTableController: " + clientIp);
    }

    // Khởi tạo bảng và liên kết dữ liệu với các cột
    @FXML
    public void initialize() {
        try {
            // Liên kết cột với các thuộc tính của ClientProcess
            processIDCol.setCellValueFactory(new PropertyValueFactory<>("processID"));
            processNameCol.setCellValueFactory(new PropertyValueFactory<>("processName"));
            processPathCol.setCellValueFactory(new PropertyValueFactory<>("processPath"));
            cpuUsageCol.setCellValueFactory(new PropertyValueFactory<>("CPUusage"));
            ramUsageCol.setCellValueFactory(new PropertyValueFactory<>("RAMusage"));

            Redis redis = Redis.getInstance();
            List<ClientDetail> list = redis.getAllClientDetail();

            if (list != null && list.size() > 0) {
                for (ClientDetail detail : list) {
                    System.out.println("Checking client with IP: " + detail.getIpAddress());
                    if (Objects.equals(detail.getIpAddress(), clientIp)) {
                        System.out.println("Found matching client IP: " + clientIp);
                        List<ClientProcess> processList = detail.getProcessDetails();
                        System.out.println("Number of processes found: " + processList.size());

                        // Sử dụng FXCollections để tạo ObservableList từ List
                        ObservableList<ClientProcess> observableProcessList = FXCollections.observableArrayList(processList);
                        tableView.setItems(observableProcessList);
                        break;
                    }
                }
            } else {
                System.out.println("ClientDetail list is empty or null.");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
