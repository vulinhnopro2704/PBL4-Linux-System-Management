package com.serverapp.controller;

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
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

public class MainController {
    @FXML
    private GridPane clientCardContainer;

    @FXML
    private TextArea logArea;

    private int currentRow = 0;
    private int currentColumn = 0;

    public void addClientCard(String hostName, String ipAddress, String macAddress, String osVersion, Boolean isConnect) {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/component/client-card.fxml"));
                AnchorPane clientCard = loader.load();

                ClientCardController controller = loader.getController();
                if (controller != null) {
                    controller.setClientInfo(hostName, ipAddress, macAddress, osVersion, isConnect);

//                    clientCardContainer.add(, currentColumn, currentRow);
                    clientCardContainer.getChildren().add(clientCard);
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

    public void clearClientCards() {
        Platform.runLater(() -> {
            clientCardContainer.getChildren().clear();
            currentRow = 0;
            currentColumn = 0;
        });
    }

    public void updateUI(){
        clearClientCards();
        List<ClientCard> list = Redis.getInstance().getAllClientCard();
        list.stream().forEach(clientCard -> {
            addClientCard(
                    clientCard.getHostName(),
                    clientCard.getIpAddress(),
                    clientCard.getMacAddress(),
                    clientCard.getOsVersion(),
                    clientCard.getIsConnect());
        });
    }

    public void appendLog(String message) {
        Platform.runLater(() -> {
            logArea.appendText(message + "\n");
        });
    }
}