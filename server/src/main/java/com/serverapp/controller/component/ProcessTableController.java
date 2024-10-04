package com.serverapp.controller.component;

import com.serverapp.model.ClientDetail;
import com.serverapp.model.ClientProcess;
import com.serverapp.model.Redis;
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

    private String clientIp;

    private ContextMenu contextMenu = new ContextMenu();
    private ObservableList<ClientProcess> observableProcessList = FXCollections.observableArrayList();

    // Hàm để thiết lập IP client
    public void setClientIp(String ip) {
        this.clientIp = ip;
        System.out.println("Received IP in ProcessTableController: " + clientIp);
    }

    // Hàm kết thúc tiến trình dựa trên PID
    public static void killProcess(int pid) {
        String killCommand = "taskkill /F /PID " + pid;

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", killCommand);
            Process process = processBuilder.start();
            process.waitFor();
            System.out.println("Process " + pid + " has been killed.");
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
        try {
            // Tạo các mục MenuItem cho ContextMenu
            MenuItem killProcessItem = new MenuItem("Kill Process");
            MenuItem exitItem = new MenuItem("Exit");

            // Thêm các mục vào ContextMenu
            contextMenu.getItems().addAll(killProcessItem, exitItem);

            // Liên kết cột với các thuộc tính của ClientProcess
            processIDCol.setCellValueFactory(new PropertyValueFactory<>("processID"));
            processNameCol.setCellValueFactory(new PropertyValueFactory<>("processName"));
            processPathCol.setCellValueFactory(new PropertyValueFactory<>("processPath"));
            cpuUsageCol.setCellValueFactory(new PropertyValueFactory<>("CPUusage"));
            ramUsageCol.setCellValueFactory(new PropertyValueFactory<>("RAMusage"));

            // Gắn sự kiện cho MenuItem "Kill Process"
            killProcessItem.setOnAction(event -> {
                ClientProcess selectedProcess = tableView.getSelectionModel().getSelectedItem();
                if (selectedProcess != null) {
                    int pid = Integer.parseInt(selectedProcess.getProcessID());
                    System.out.println("Killing process with PID: " + pid);
                    killProcess(pid);  // Gọi hàm killProcess

                    // Xóa tiến trình khỏi danh sách sau khi kill
                    observableProcessList.remove(selectedProcess);
                    tableView.refresh();  // Cập nhật lại bảng
                }
            });

            // Gắn sự kiện cho MenuItem "Exit"
            exitItem.setOnAction(event -> {
                System.out.println("Exit menu item clicked.");
                // Bạn có thể thêm hành động thoát hoặc xử lý khác
            });

            // Khởi tạo một Timeline để cập nhật bảng mỗi 5 giây
            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> updateProcessTable()));
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();  // Bắt đầu chạy Timeline

            updateProcessTable();  // Cập nhật bảng lần đầu khi khởi tạo
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Cập nhật bảng tiến trình
    public void updateProcessTable() {
        Redis redis = Redis.getInstance();
        List<ClientDetail> list = redis.getAllClientDetail();

        if (list != null && list.size() > 0) {
            for (ClientDetail detail : list) {
                System.out.println("Checking client with IP: " + detail.getIpAddress());
                if (Objects.equals(detail.getIpAddress(), clientIp)) {
                    System.out.println("Found matching client IP: " + clientIp);
                    List<ClientProcess> processList = detail.getProcessDetails();
                    System.out.println("Number of processes found: " + processList.size());

                    // Cập nhật ObservableList
                    observableProcessList.setAll(processList);
                    tableView.setItems(observableProcessList);
                    break;
                }
            }
        } else {
            System.out.println("ClientDetail list is empty or null.");
        }
    }
}
