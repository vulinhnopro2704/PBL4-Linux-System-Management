package com.serverapp.controller.component;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;

public class SidebarController {

    @FXML
    private ImageView PBL4Logo;

    @FXML
    private Button btnSystemMonitoring;

    @FXML
    private Button btnUserAndAccess;

    @FXML
    private Button btnFileAndDirectory;

    @FXML
    private Button btnInstallAndUpdate;

    @FXML
    private Button btnSecurityManagement;

    @FXML
    private Button btnSetting;


    @FXML
    private void handleUserAndAccess() {
        // Handle User & Access button click
        System.out.println("User & Access button clicked");
    }

    @FXML
    private void handleFileAndDirectory() {
        // Handle File & Directory button click
        System.out.println("File & Directory button clicked");
    }

    @FXML
    private void handleInstallAndUpdate() {
        // Handle Install & Update button click
        System.out.println("Install & Update button clicked");
    }

    @FXML
    private void handleSecurityManagement() {
        // Handle Security Management button click
        System.out.println("Security Management button clicked");
    }

    @FXML
    private void handleSetting() {
        // Handle Setting button click
        System.out.println("Setting button clicked");
    }

    @FXML
    private void handleSystemMonitoring(javafx.scene.input.MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main-view.fxml"));
            Parent root = loader.load();

            // Lấy stage hiện tại và chuyển màn hình
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}