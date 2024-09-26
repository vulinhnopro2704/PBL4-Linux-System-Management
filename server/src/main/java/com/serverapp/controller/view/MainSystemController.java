package com.serverapp.controller.view;

import com.serverapp.controller.component.ClientCardController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import java.io.IOException;

public class MainSystemController {

    @FXML
    private FlowPane clientCardContainer;

    @FXML
    private AnchorPane panelPortInclude;

    @FXML
    private void initialize() {
        // Initialization logic if needed
        addClientCard("Host Name 1", "192.168.1.1", "00-0C-29-34-E9-B1", "Linux 22.04 LTS", true);
        addClientCard("Host Name 2", "192.168.1.2", "00-0C-29-34-E9-B2", "Windows 10", false);
        addClientCard("Host Name 3", "192.168.1.3", "00-0C-29-34-E9-B3", "macOS 11.2", true);
        addPanelPort();
    }

    public void addClientCard(String hostName, String ipAddress, String macAddress, String osVersion, Boolean isConnect) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/component/client-card.fxml"));
            AnchorPane clientCard = loader.load();

            ClientCardController controller = loader.getController();
            if (controller != null) {
                controller.setClientInfo(hostName, ipAddress, macAddress, osVersion, isConnect);
//                controller.setAppController(getAppController()); // Set the AppController instance

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

            panelPortInclude.getChildren().add(panelPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}