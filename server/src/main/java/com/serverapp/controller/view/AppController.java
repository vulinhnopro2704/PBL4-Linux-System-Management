package com.serverapp.controller.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class AppController {

    @FXML
    private StackPane contentArea;

    @FXML
    private TextArea logMessage;

    // Hàm xử lý khi nhấn vào System Monitoring Sidebar
    public void handleSystemMonitoring(MouseEvent event) throws IOException {
        // Tải file systemmonitoringpage.fxml vào vùng content
        loadPage("/view/systemmonitoringpage.fxml");
    }

    // Hàm xử lý khi nhấn vào File and Directory Sidebar
    public void handleFileAndDirectory(ActionEvent event) throws IOException {
        // Tải file fileandirectpage.fxml vào vùng content
        loadPage("/view/fileandirectpage.fxml");
    }

    // Hàm xử lý khi nhấn vào Detail
    public void handleDetail(ActionEvent event) throws IOException {
        // Tải file detail.fxml vào vùng content
        loadPage("/view/detail.fxml");
    }

    // Hàm xử lý khi nhấn vào Process
    public void handleProcess(ActionEvent event) throws IOException {
        // Tải file process.fxml vào vùng content
        loadPage("/view/process.fxml");
    }

    // Hàm tiện ích để load các trang vào vùng contentArea
    private void loadPage(String fxmlFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent page = loader.load();

        // Xóa tất cả các trang hiện tại và thêm trang mới
        contentArea.getChildren().clear();
        contentArea.getChildren().add(page);
    }
}
