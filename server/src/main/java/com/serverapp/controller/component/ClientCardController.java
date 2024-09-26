
package com.serverapp.controller.component;

import com.serverapp.controller.view.AppController;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import lombok.Setter;

public class ClientCardController {
    @FXML
    private Label txtHostName;

    @FXML
    private Label txtIPAddress;

    @FXML
    private Label txtMACAddress;

    @FXML
    private Label txtOSVersion;

    @FXML
    private Circle iconStatus;

    @Setter
    private String fxmlPath;

    public void setClientInfo(String hostName, String ipAddress, String macAddress, String osVersion, Boolean isConnect) {
        txtHostName.setText(hostName);
        txtIPAddress.setText(ipAddress);
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
        setFxmlPath("/view/client-general.fxml");
        AppController.getInstance().loadPage(this.fxmlPath);
    }
}