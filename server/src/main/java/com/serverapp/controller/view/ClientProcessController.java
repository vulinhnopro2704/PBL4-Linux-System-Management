package com.serverapp.controller.view;

import com.serverapp.model.ClientCard;
import com.serverapp.model.ClientDetail;
import com.serverapp.model.ClientProcess;
import com.serverapp.model.Redis;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    public void setProcessClientIp(String ip) {
        this.clientIp = ip;
        System.out.println("Received IP in ClientProcessController: " + clientIp);
    }

    // Khởi tạo controller
    @FXML
    public void viewchange() {
        btnGeneral.setOnMouseClicked(event -> loadPage("/view/client-view.fxml", clientIp));
        btnProcess.setOnMouseClicked(event -> loadPage("/view/client-process.fxml", clientIp));  // Thêm clientIP vào đây
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
            System.out.println("Client IP to pass: " + clientIP); // Kiểm tra giá trị clientIP trước khi truyền
            if (controller instanceof ClientViewController) {
                ((ClientViewController) controller).setClientIp(clientIP);
            } else if (controller instanceof ClientProcessController) {
                ((ClientProcessController) controller).setProcessClientIp(clientIP); // Truyền IP vào đây
            } else if (controller instanceof ClientPerformanceController) {
                System.out.println("ClientPerformanceController: chưa triển khai.");
            } else if (controller instanceof ClientScreenController) {
                System.out.println("ClientScreenController: chưa triển khai.");
            }

            // Kiểm tra và truyền clientIP đến controller tương ứng
            System.out.println("Client IP to pass: " + clientIP); // Kiểm tra giá trị clientIP trước khi truyền

            // Lấy stage hiện tại và thay đổi scene
            Stage stage = (Stage) btnProcess.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

