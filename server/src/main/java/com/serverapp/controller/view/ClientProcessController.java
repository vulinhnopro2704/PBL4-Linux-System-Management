package com.serverapp.controller.view;

import com.serverapp.controller.component.ProcessTableController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientProcessController {

    @FXML
    private Label btnGeneral;

    @FXML
    private Label btnProcess;

    @FXML
    private Label btnPerformance;

    @FXML
    private Label btnScreen;

    private String clientIp;

    @FXML
    private AnchorPane processTableContainer;

    // Phương thức để load và nhúng client-processtable.fxml
    private void loadProcessTable(String clientIp) {
        try {
            // Load FXML của ProcessTable
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/component/client-processtable.fxml"));
            Parent processTableView = loader.load();

            // Lấy controller của ProcessTable
            ProcessTableController processTableController = loader.getController();

            // Truyền clientIp cho ProcessTableController
            processTableController.setClientIp(clientIp);

            // Thêm ProcessTable vào processTableContainer (AnchorPane)
            processTableContainer.getChildren().clear();  // Xóa các thành phần cũ
            processTableContainer.getChildren().add(processTableView);  // Thêm bảng mới
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Phương thức này để gán giá trị IP và load bảng tiến trình khi IP đã được gán
    public void setProcessClientIp(String ip) {
        this.clientIp = ip;
        System.out.println("Received IP in ClientProcessController: " + clientIp);

        // Kiểm tra và load bảng tiến trình nếu clientIp đã được gán
        if (clientIp != null && !clientIp.isEmpty()) {
            loadProcessTable(clientIp);
        }
    }

    // Khởi tạo controller
    @FXML
    public void initialize() {
        // Khi initialize() chạy, clientIp có thể chưa được truyền, vì vậy không làm gì tại đây.
    }

    @FXML
    public void viewchange() {
        btnGeneral.setOnMouseClicked(event -> loadPage("/view/client-view.fxml", clientIp));
        btnProcess.setOnMouseClicked(event -> loadPage("/view/client-process.fxml", clientIp));  // Truyền clientIP vào đây
        btnPerformance.setOnMouseClicked(event -> loadPage("/view/client-performance.fxml", clientIp));
        btnScreen.setOnMouseClicked(event -> loadPage("/view/client-screen.fxml", clientIp));
    }

    // Hàm để load trang mới và truyền clientIP đến controller
    private void loadPage(String fxmlFile, String clientIP) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            // Lấy controller của trang và truyền clientIP
            Object controller = loader.getController();

            // Kiểm tra và truyền clientIP đến controller tương ứng
            if (controller instanceof ClientViewController) {
                ((ClientViewController) controller).setClientIp(clientIP);
            } else if (controller instanceof ClientProcessController) {
                ((ClientProcessController) controller).setProcessClientIp(clientIP);
            } else if (controller instanceof ClientPerformanceController) {
                System.out.println("ClientPerformanceController: chưa triển khai.");
            } else if (controller instanceof ClientScreenController) {
                System.out.println("ClientScreenController: chưa triển khai.");
            }

            // Lấy stage hiện tại và thay đổi scene
            Stage stage = (Stage) btnProcess.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
