
package com.serverapp.controller.component;

import java.io.IOException;

import com.serverapp.controller.view.MainController;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class ClientCardController {
    final private MainController mainController = new MainController();

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


   /* @FXML
    private void handleCardClick(MouseEvent event) {
        // Handle card click event
        mainController.appendLog("Card clicked: " + txtIpAddress.getText() + ", " + txtOSVersion.getText());
    }
*/
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