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
import lombok.Setter;

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
    @Setter
    private String fxmlPath = "/view/client-general.fxml";


    @FXML
     public void initialize() {
        addPanelPort();
        currentClientIp = AppController.getInstance().getCurrentClientIp();
        clientDetail = Redis.getInstance().getClientDetail(currentClientIp);
        if (clientDetail != null) {
            System.out.println("Client Detail: " + clientDetail);
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
        btnGeneral.setOnMouseClicked(event -> {
            if (fxmlPath != null && !fxmlPath.isEmpty() && !fxmlPath.equals("/view/client-general.fxml")) {
                setFxmlPath("/view/client-general.fxml");
                loadPage(fxmlPath);
            }
        });

        btnProcess.setOnMouseClicked(event -> {
            if (fxmlPath != null && !fxmlPath.isEmpty() && !fxmlPath.equals("/view/client-process.fxml")) {
                setFxmlPath("/view/client-process.fxml");
                loadPage(fxmlPath);
            }
        });

        btnPerformance.setOnMouseClicked(event -> {
            if (fxmlPath != null && !fxmlPath.isEmpty() && !fxmlPath.equals("/view/client-performance.fxml")) {
                setFxmlPath("/view/client-performance.fxml");
                loadPage(fxmlPath);
            }
        });

        btnScreen.setOnMouseClicked(event -> {
            if (fxmlPath != null && !fxmlPath.isEmpty() && !fxmlPath.equals("/view/client-screen.fxml")) {
                setFxmlPath("/view/client-screen.fxml");
                loadPage(fxmlPath);
            }
        });
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
    }// Add methods to handle button clicks or other interactions if needed
}