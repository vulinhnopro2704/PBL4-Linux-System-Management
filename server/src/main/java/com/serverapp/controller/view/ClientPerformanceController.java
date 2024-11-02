package com.serverapp.controller.view;

import com.serverapp.controller.IController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import lombok.Setter;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientPerformanceController implements IController {

    @FXML
    private Label btnGeneral;

    @FXML
    private Label btnProcess;

    @FXML
    private Label btnPerformance;

    @FXML
    private Label btnScreen;

    @FXML
    private AnchorPane clientChartInclude;

    @FXML
    private AnchorPane panelPortInclude;

    @FXML
    public void initialize() {
        addPanelPort();
        addClientChart();
    }
    @Setter
    private String fxmlPath = "/view/client-performance.fxml";

    @Override
    public void update() {

    }

    @Override
    public void stop() {
        System.out.println("ClientPerformanceController stop");
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

    public void addClientChart() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/component/client-chart.fxml"));
            AnchorPane clientChart = loader.load();

            clientChartInclude.getChildren().add(clientChart);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}