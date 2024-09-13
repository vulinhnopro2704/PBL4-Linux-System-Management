
package com.serverapp.controller;

import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.List;

import com.serverapp.model.ClientCard;
import com.serverapp.model.Redis;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
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
    private Circle iconStatus;
    private Scene scene;
    private Stage stage;
    private Parent root;

    @FXML
    private void handleCardClick(MouseEvent event) {
        // Handle card click event
        System.out.println("Card clicked: " + txtIpAddress.getText() + ", " + txtOSVersion.getText());
    }

    public void setClientInfo(String hostName, String ipAddress, String macAddress, String osVersion, Boolean isConnect) {
        txtHostName.setText(hostName);
        txtIpAddress.setText(ipAddress);
        txtMACAddress.setText(macAddress);
        txtOSVersion.setText(osVersion);
        if (isConnect != null && isConnect) {
            iconStatus.setFill(Color.web("#04ef72")); // Green color
        } else {
            iconStatus.setFill(Color.web("#ff0000")); // Red color
        }
    }

    @FXML
    private void handleCardClick(javafx.scene.input.MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/client-view.fxml"));
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