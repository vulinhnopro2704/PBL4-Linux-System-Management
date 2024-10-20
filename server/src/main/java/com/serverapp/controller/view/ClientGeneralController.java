package com.serverapp.controller.view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class ClientGeneralController {

    @FXML
    private Label btnGeneral;

    @FXML
    private Label btnProcess;

    @FXML
    private Label btnPerformance;

    @FXML
    private Label btnScreen;

    @FXML
    private AnchorPane clientDetailInclude;

    @FXML
    private AnchorPane panelPortInclude;

    @FXML
    private void initialize() {
        addPanelPort();
        addClientDetail();
    }
    @FXML
    public void viewchange() {
        btnGeneral.setOnMouseClicked(event -> loadPage("/view/client-general.fxml"));
        btnProcess.setOnMouseClicked(event -> loadPage("/view/client-process.fxml"));
        btnPerformance.setOnMouseClicked(event -> loadPage("/view/client-performance.fxml"));
        btnScreen.setOnMouseClicked(event -> loadPage("/view/client-screen.fxml"));

    }

    private void loadPage(String fxmlPath) {
        AppController.getInstance().loadPage(fxmlPath);
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

    public void addClientDetail() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/component/client-detail.fxml"));
            AnchorPane clientChart = loader.load();

            clientDetailInclude.getChildren().add(clientChart);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Add methods to handle button clicks or other interactions if needed
}