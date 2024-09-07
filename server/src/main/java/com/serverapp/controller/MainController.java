package com.serverapp.controller;

import java.io.IOException;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

public class MainController {
    @FXML
    private GridPane clientCardContainer;

    private int currentRow = 0;
    private int currentColumn = 0;

    public void addClientCard(String hostName, String ipAddress, String macAddress, String osVersion) {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/component/client-card.fxml"));
                AnchorPane clientCard = loader.load();

                ClientCardController controller = loader.getController();
                if (controller != null) {
                    controller.setClientInfo(hostName, ipAddress, macAddress, osVersion);

                    clientCardContainer.add(clientCard, currentColumn, currentRow);

                    // Update column and row for next card
                    currentColumn++;
                    if (currentColumn == 2) {
                        currentColumn = 0;
                        currentRow++;
                    }
                } else {
                    System.err.println("ClientCardController is null");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}