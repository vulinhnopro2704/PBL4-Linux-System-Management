package com.serverapp.controller.view;

import com.serverapp.controller.component.ClientCardController;
import com.serverapp.controller.component.PanelPortController;
import com.serverapp.database.Redis;
import com.serverapp.enums.RequestType;
import com.serverapp.model.ClientCard;
import com.serverapp.service.ISystemMonitoring;
import com.serverapp.service.implement.SystemMonitoring;
import com.serverapp.util.CurrentType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import java.io.IOException;
import java.util.List;

public class MainSystemController {

    @FXML
    private FlowPane clientCardContainer;

    @FXML
    private AnchorPane panelPortInclude;

    private PanelPortController panelPortController;
    private ISystemMonitoring systemMonitoring;

    @FXML
    private void initialize() {
        addPanelPort();
        addClientCard("Host Name", "IP Address", "MAC Address", "OS Version", false);
        addClientCard("Host Name", "IP Address", "MAC Address", "OS Version", false);
        CurrentType.getInstance().setType(RequestType.SYSTEM_INFO);
        systemMonitoring = new SystemMonitoring();
        systemMonitoring.setMainSystemController(this);
        systemMonitoring.start();
    }

    public void addClientCard(String hostName, String ipAddress, String macAddress, String osVersion, Boolean isConnect) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/component/client-card.fxml"));
            AnchorPane clientCard = loader.load();

            ClientCardController controller = loader.getController();
            if (controller != null) {
                controller.setClientInfo(hostName, ipAddress, macAddress, osVersion, isConnect);
                clientCardContainer.getChildren().add(clientCard);
            } else {
                System.err.println("ClientCardController is null");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addPanelPort() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/component/panel-port.fxml"));
            AnchorPane panelPort = loader.load();
            panelPortController = loader.getController();
            panelPortInclude.getChildren().add(panelPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateListClientCard() {
        Redis redis = Redis.getInstance();
        List<ClientCard> list = redis.getAllClientCard();

        if (list != null && !list.isEmpty()) {
            // Đảm bảo cập nhật giao diện trên UI thread
            Platform.runLater(() -> {
                list.forEach(clientCard -> {
                    addClientCard(
                            clientCard.getHostName(),
                            clientCard.getIpAddress(),
                            clientCard.getMacAddress(),
                            clientCard.getOsVersion(),
                            clientCard.getIsConnect());
                });
            });
        }
    }

    public void clearClientCards() {
//        Platform.runLater(() -> {
//            clientCardContainer.getChildren().clear();
//        });
    }

    public void updateUI(){
        clearClientCards();
        updateListClientCard();
    }

    public void appendLog(String message) {
        Platform.runLater(() -> {
            if (panelPortController != null) {
                panelPortController.updateLogContent(message);
            } else {
                System.err.println("PanelPortController is not initialized.");
            }
        });
    }

    private void stop(){
        systemMonitoring.stop();
    }
}