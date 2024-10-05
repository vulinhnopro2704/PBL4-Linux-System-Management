package com.serverapp.controller.view;

import java.io.IOException;
import java.util.List;

import com.serverapp.controller.component.ClientCardController;
import com.serverapp.controller.component.PanelPortController;
import com.serverapp.enums.RequestType;
import com.serverapp.model.ClientCard;
import com.serverapp.model.Redis;

import com.serverapp.util.ISystemMonitoring;
import com.serverapp.util.implement.CurrentType;
import com.serverapp.util.implement.SystemMonitoring;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;

public class MainController {
    @FXML
    private FlowPane clientCardContainer;

    @FXML
    private Label btnGeneral;

    @FXML
    private Label btnProcess;

    @FXML
    private Label btnPerformance;

    @FXML
    private Label btnScreen;

    @FXML
    private AnchorPane panelPortInclude;

    private PanelPortController panelPortController;
    private ISystemMonitoring server;

    @FXML
    public void initialize() {
        try {
            // Load panel-port.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/component/panel-port.fxml"));
            AnchorPane panelPortRoot = loader.load(); // Lấy root từ panel-port.fxml

            CurrentType.getInstance().setType(RequestType.SYSTEM_INFO);
            // Initialize and start the TCP server
            server = new SystemMonitoring();
            server.setMainController(this);
            server.start();

            // Set controller từ panel-port.fxml
            panelPortController = loader.getController();
            if (panelPortInclude != null) {
                // Thêm panelPortRoot vào panelPortInclude (AnchorPane trong main-view.fxml)
                panelPortInclude.getChildren().setAll(panelPortRoot); // Đặt nội dung của panelPortInclude thành panelPortRoot
            } else {
                System.err.println("PanelPortController is null");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        updateListClientCard();
    }

    private void updateListClientCard() {
        Redis redis = Redis.getInstance();
        List<ClientCard> list = redis.getAllClientCard();
        if (list != null && list.size() > 0) {
            list.stream().forEach(clientCard -> {
                addClientCard(
                        clientCard.getHostName(),
                        clientCard.getIpAddress(),
                        clientCard.getMacAddress(),
                        clientCard.getOsVersion(),
                        clientCard.getIsConnect());
            });
        }
    }

    // Hàm để load trang mới
    private void loadPage(String fxmlFile) {
        try {
            stop();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            // Lấy stage hiện tại và thay đổi scene
            Stage stage = (Stage) btnGeneral.getScene().getWindow();
            stage.setScene(new Scene(root));

            // Explicitly set the current controller to null
            FXMLLoader currentLoader = new FXMLLoader(getClass().getResource("/view/main-view.fxml"));
            currentLoader.setController(null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addClientCard(String hostName, String ipAddress, String macAddress, String osVersion, Boolean isConnect) {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/component/client-card.fxml"));
                AnchorPane clientCard = loader.load();

                ClientCardController controller = loader.getController();
                if (controller != null) {
                    controller.setClientInfo(hostName, ipAddress, macAddress, osVersion, isConnect);

                    clientCardContainer.getChildren().add(clientCard);
                } else {
                    appendLog("ClientCardController is null");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void clearClientCards() {
        Platform.runLater(() -> {
            clientCardContainer.getChildren().clear();
        });
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
        // Stop the server when the application is closed
        if (server != null) {
            server.stop();
        }
    }
}