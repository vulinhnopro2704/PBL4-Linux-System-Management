package com.serverapp.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class ClientCardController {

    @FXML
    private Label txtHostName;

    @FXML
    private Label txtIpAddress;

    @FXML
    private Label txtMACAddress;

    @FXML
    private Label txtOSVersion;

    @FXML
    private void handleCardClick(MouseEvent event) {
        // Handle card click event
        System.out.println("Card clicked: " + txtIpAddress.getText() + ", " + txtOSVersion.getText());
    }

    public void setClientInfo(String hostName, String ipAddress, String macAddress, String osVersion) {
        txtHostName.setText(hostName);
        txtIpAddress.setText(ipAddress);
        txtMACAddress.setText(macAddress);
        txtOSVersion.setText(osVersion);
    }
}