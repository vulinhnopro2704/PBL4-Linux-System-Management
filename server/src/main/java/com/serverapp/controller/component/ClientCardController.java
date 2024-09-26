
package com.serverapp.controller.component;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

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
            List<ClientCard> list = redis.getAllClientCard();
            if (list != null && list.size() > 0) {
                for (ClientCard card : list) {
                    if (Objects.equals(card.getIpAddress(), txtIPAddress.getText())) {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/client-view.fxml"));
                        Parent root = loader.load();

                        // Lấy stage hiện tại và chuyển màn hình
                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        Scene scene = new Scene(root);
                        stage.setScene(scene);
                        stage.show();
                    }else{
                        System.out.println("Cant Catch");
                    }
                }
            }
//            if (clientDetail != null) {
//                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/client-view.fxml"));
//                Parent root = loader.load();
//
//                // Lấy stage hiện tại và chuyển màn hình
//                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
//                Scene scene = new Scene(root);
//                stage.setScene(scene);
//                stage.show();
//                System.out.println(clientDetail.getCpuModel());
//            }else {
//                // Xử lý nếu clientDetail là null (không tìm thấy ClientDetail với IP đã nhập)
//                System.out.println("ClientDetail not found for IP: " + ipAddress);
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}