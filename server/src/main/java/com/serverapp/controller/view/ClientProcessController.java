package com.serverapp.controller.view;

import com.serverapp.controller.IController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class ClientProcessController implements IController {

    @FXML
    private Label btnGeneral;

    @FXML
    private Label btnProcess;

    @FXML
    private Label btnPerformance;

    @FXML
    private Label btnScreen;

    @FXML
    private AnchorPane clientProcesstableInclude;

    @FXML
    private AnchorPane panelPortInclude;

    @FXML
    public void initialize() {
        addPanelPort();
        addClientProcessTable();
    }

    @Override
    public void update() {

    }

    @Override
    public void stop() {

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

    private void addClientProcessTable() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/component/client-processtable.fxml"));
            AnchorPane clientProcessTable = loader.load();

            clientProcesstableInclude.getChildren().add(clientProcessTable);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}