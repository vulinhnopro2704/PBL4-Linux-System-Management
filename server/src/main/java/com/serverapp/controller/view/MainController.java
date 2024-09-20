package com.serverapp.controller.view;

import java.io.IOException;
import java.util.List;

import com.serverapp.controller.component.ClientCardController;
import com.serverapp.model.ClientCard;
import com.serverapp.model.Redis;

import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;

public class MainController {
    @FXML
    private FlowPane clientCardContainer;

    @FXML
    private TextArea logArea;

    private int currentRow = 0;
    private int currentColumn = 0;

    @FXML
    private Label btnGeneral;

    @FXML
    private Label btnProcess;

    @FXML
    private Label btnPerformance;

    @FXML
    private Label btnScreen;

    // Khởi tạo controller
    @FXML
    public void viewchange() {
        btnGeneral.setOnMouseClicked(event -> loadPage("/view/client-view.fxml"));
        btnProcess.setOnMouseClicked(event -> loadPage("/view/client-process.fxml"));
        btnPerformance.setOnMouseClicked(event -> loadPage("/view/client-performance.fxml"));
        btnScreen.setOnMouseClicked(event -> loadPage("/view/client-screen.fxml"));
    }

    // Hàm để load trang mới
    private void loadPage(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            // Lấy stage hiện tại và thay đổi scene
            Stage stage = (Stage) btnGeneral.getScene().getWindow();
            stage.setScene(new Scene(root));
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