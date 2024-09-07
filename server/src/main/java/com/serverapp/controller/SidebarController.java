package com.serverapp.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;

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

    // Add your event handling methods here
    @FXML
    private void handleSystemMonitoring() {
        // Handle System Monitoring button click
        System.out.println("System Monitoring button clicked");
    }

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
}