
package com.serverapp.controller.component;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import com.serverapp.controller.view.ClientProcessController;
import com.serverapp.controller.view.ClientViewController;
import com.serverapp.controller.view.MainController;

import com.serverapp.model.ClientCard;
import com.serverapp.model.ClientDetail;
import com.serverapp.model.Redis;
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
    private Label txtIPAddress;

    @FXML
    private Label txtMACAddress;

    @FXML
    private Label txtOSVersion;

    @FXML
    private Circle iconStatus;


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
        try {
            Redis redis = Redis.getInstance();
            ClientDetail clientDetail = redis.getClientDetail(txtIPAddress.getText());
            if (clientDetail != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/client-view.fxml"));
                Parent root = loader.load();

                // Lấy đối tượng ClientViewController từ FXMLLoader
                ClientViewController clientViewController = loader.getController();

                // Truyền IP cho ClientViewController
                clientViewController.setClientIp(txtIPAddress.getText());

                // Lấy stage hiện tại và chuyển màn hình
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            }else {
                // Xử lý nếu clientDetail là null (không tìm thấy ClientDetail với IP đã nhập)
                System.out.println("ClientDetail not found for IP: " + txtIPAddress.getText());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}