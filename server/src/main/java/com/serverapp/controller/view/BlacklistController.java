package com.serverapp.controller.view;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import java.io.IOException;

public class BlacklistController {

    @FXML
    private ListView<String> blacklistListView;

    @FXML
    private Button addButton;

    @FXML
    private Button removeButton;

    @FXML
    private Button exitButton;

    private Stage currentStage;

    // Method to handle Add button click event
    @FXML
    public void handleAdd() {
        // Tạo cửa sổ mới khi nhấn vào nút "Add"
        Stage addIpStage = new Stage();
        addIpStage.setTitle("Thêm IP vào danh sách");

        // Tạo một TextField cho người dùng nhập địa chỉ IP
        TextField ipAddressTextField = new TextField();
        ipAddressTextField.setPromptText("Nhập địa chỉ IP");

        // Tạo một nút "Thêm" để thêm IP vào danh sách
        Button addIpButton = new Button("Thêm");
        addIpButton.setOnAction(e -> {
            String ip = ipAddressTextField.getText().trim();

            // Kiểm tra IP hợp lệ
            if (!isValidIp(ip)) {
                showAlert(AlertType.WARNING, "Định dạng IP không hợp lệ", "Vui lòng nhập địa chỉ IP hợp lệ.");
            } else {
                // Thêm IP vào danh sách nếu hợp lệ
                blacklistListView.getItems().add(ip);
                addIpStage.close();  // Đóng cửa sổ sau khi thêm

                // Thực thi lệnh iptables để chặn IP
                blockIp(ip);
            }
        });

        // Thêm các thành phần vào layout VBox
        VBox layout = new VBox(10);
        layout.getChildren().addAll(ipAddressTextField, addIpButton);

        // Tạo scene và thiết lập cho cửa sổ
        Scene scene = new Scene(layout, 300, 150);
        addIpStage.setScene(scene);

        // Hiển thị cửa sổ
        addIpStage.show();
    }

    // Phương thức kiểm tra định dạng IP hợp lệ
    private boolean isValidIp(String ip) {
        String ipPattern = "^([0-9]{1,3}\\.){3}[0-9]{1,3}$"; // Định dạng đơn giản để kiểm tra IP
        if (ip.matches(ipPattern)) {
            // Kiểm tra từng octet IP có nằm trong khoảng 0-255 không
            String[] parts = ip.split("\\.");
            for (String part : parts) {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    // Hàm hiển thị cảnh báo
    private void showAlert(AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Method to handle Remove button click event
    @FXML
    private void handleRemove() {
        String selectedIp = blacklistListView.getSelectionModel().getSelectedItem();
        if (selectedIp != null) {
            // Xóa IP khỏi danh sách
            blacklistListView.getItems().remove(selectedIp);

            // Thực thi lệnh iptables để xóa quy tắc chặn IP
            unblockIp(selectedIp);
        } else {
            showAlert(AlertType.WARNING, "Chưa chọn IP", "Vui lòng chọn một IP để xóa.");
        }
    }

    // Method to handle Exit button click event
    @FXML
    private void handleExit() {
        closeWindow();
    }

    // Method to set the stage (window) reference
    public void setStage(Stage stage) {
        this.currentStage = stage;
    }

    // Method to close the window
    private void closeWindow() {
        if (currentStage != null) {
            currentStage.close(); // Close the current stage (window)
        }
    }

    // Method to block an IP using iptables
    private void blockIp(String ip) {
        try {
            // Chặn IP bằng iptables
            String command = "iptables -A INPUT -s " + ip + " -j DROP";
            Runtime.getRuntime().exec(command);
            System.out.println("Executing: " + command);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Lỗi", "Không thể thực thi lệnh iptables.");
        }
    }

    // Method to unblock an IP using iptables
    private void unblockIp(String ip) {
        try {
            // Xóa quy tắc chặn IP bằng iptables
            String command = "iptables -D INPUT -s " + ip + " -j DROP";
            Runtime.getRuntime().exec(command);
            System.out.println("Executing: " + command);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Lỗi", "Không thể thực thi lệnh iptables.");
        }
    }
}
