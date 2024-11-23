package com.serverapp.controller.component;

import com.serverapp.controller.view.AppController;
import com.serverapp.controller.view.ClientProcessController;
import com.serverapp.database.Redis;
import com.serverapp.model.ClientDetail;
import com.serverapp.model.ClientProcess;
import com.serverapp.socket.SocketManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import javafx.collections.transformation.FilteredList;

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

    ClientProcessController clientProcessController;

    private FilteredList<ClientProcess> filteredData;

    private ContextMenu contextMenu = new ContextMenu();
    // Sử dụng observableProcessList toàn cục để theo dõi tiến trình
    private ObservableList<ClientProcess> observableProcessList = FXCollections.observableArrayList();

    // Hàm kết thúc tiến trình dựa trên PID
    public static void killProcess(int pid) {
        String os = System.getProperty("os.name").toLowerCase();
        String killCommand;

        if (os.contains("win")) {
            killCommand = "taskkill /F /PID " + pid;  // Lệnh cho Windows
        } else {
            killCommand = "kill -9 " + pid;  // Lệnh cho Linux
        }

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", killCommand);
            if (!os.contains("win")) {
                processBuilder = new ProcessBuilder("bash", "-c", killCommand);
            }

            Process process = processBuilder.start();
            process.waitFor();
            System.out.println("Process " + pid + " has been killed on " + os);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("Failed to kill process " + pid);
        }
    }

    // Hàm xử lý sự kiện chuột
    public void clickHandler(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) {  // Chuột phải
            if (tableView.getSelectionModel().getSelectedItem() != null) {
                contextMenu.show(tableView, event.getScreenX(), event.getScreenY());
                System.out.println("Right-click detected on: " + tableView.getSelectionModel().getSelectedItem());
            }
        } else if (event.getButton() == MouseButton.PRIMARY) { // Chuột trái
            if (tableView.getSelectionModel().getSelectedItem() != null) {
                System.out.println("PID: " + tableView.getSelectionModel().getSelectedItem().getProcessID());
            }
        }
    }

    // Khởi tạo bảng và liên kết dữ liệu với các cột
    @FXML
    public void initialize() {
        clientProcessController = new ClientProcessController();
        try {
            MenuItem killProcessItem = new MenuItem("Kill Process");
            MenuItem exitItem = new MenuItem("Exit");

            contextMenu.getItems().addAll(killProcessItem, exitItem);

            processIDCol.setCellValueFactory(new PropertyValueFactory<>("processID"));
            processNameCol.setCellValueFactory(new PropertyValueFactory<>("processName"));
            processPathCol.setCellValueFactory(new PropertyValueFactory<>("processPath"));
            cpuUsageCol.setCellValueFactory(new PropertyValueFactory<>("CPUusage"));
            ramUsageCol.setCellValueFactory(new PropertyValueFactory<>("RAMusage"));

            killProcessItem.setOnAction(event -> {
                ClientProcess selectedProcess = tableView.getSelectionModel().getSelectedItem();
                if (selectedProcess != null) {
                    int pid = Integer.parseInt(selectedProcess.getProcessID());
                    System.out.println("Killing process with PID: " + pid);
                    try {
                        SocketManager.getInstance().sendEncryptedMessage(Integer.toString(pid),AppController.getInstance().getCurrentClientIp());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    observableProcessList.remove(selectedProcess);
                    tableView.refresh();
                }
            });

            exitItem.setOnAction(event -> System.out.println("Exit menu item clicked."));

            // Khởi tạo filteredData dựa trên observableProcessList
            filteredData = new FilteredList<>(observableProcessList, p -> true);
            tableView.setItems(filteredData); // Đặt filteredData làm nguồn cho tableView

            updateProcessTable();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    // Cập nhật bảng tiến trình từ Redis
    public void updateProcessTable() {
        List<ClientProcess> clientProcesses = Redis.getInstance().getClientProcessView();
        observableProcessList.setAll(clientProcesses);
    }

    public void filterProcessByPIDOrName(String keyword) {
        filteredData.setPredicate(clientProcess -> {
            if (keyword == null || keyword.isEmpty()) {
                return true; // Hiển thị tất cả tiến trình nếu không có chuỗi tìm kiếm
            }

            String lowerCaseKeyword = keyword.toLowerCase();

            // Kiểm tra nếu từ khóa nằm trong PID hoặc tên tiến trình
            return clientProcess.getProcessID().toLowerCase().contains(lowerCaseKeyword) ||
                    clientProcess.getProcessName().toLowerCase().contains(lowerCaseKeyword);
        });
    }
}
