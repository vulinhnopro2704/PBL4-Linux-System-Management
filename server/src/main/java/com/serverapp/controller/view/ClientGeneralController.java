package com.serverapp.controller.view;

import com.serverapp.controller.IController;
import com.serverapp.controller.component.ClientDetailController;
import com.serverapp.database.Redis;
import com.serverapp.model.ClientDetail;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class ClientGeneralController implements IController {

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

    private ClientDetail clientDetail;
    private String currentClientIp;

    @FXML
     public void initialize() {
        addPanelPort();
        currentClientIp = AppController.getInstance().getCurrentClientIp();
        clientDetail = Redis.getInstance().getClientDetail(currentClientIp);
        if (clientDetail != null) {
            System.out.println("Client Detail: " + clientDetail.toString());
            Platform.runLater(() -> {
                addClientDetail();
            });
        }
        else  {
            System.out.println("Client Detail is null");
        }
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

    public void addClientDetail() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/component/client-detail.fxml"));
            AnchorPane clientDetailComponent = loader.load();
            ClientDetailController clientDetailController = loader.getController();
            clientDetailController.receiveClientDetail(clientDetail);

            clientDetailInclude.getChildren().add(clientDetailComponent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Add methods to handle button clicks or other interactions if needed
}