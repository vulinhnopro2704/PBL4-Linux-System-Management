package com.serverapp.controller.view;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.serverapp.controller.IController;
import com.serverapp.controller.component.ProcessTableController;
import com.serverapp.database.Redis;
import com.serverapp.enums.RequestType;
import com.serverapp.model.ClientCredentials;
import com.serverapp.model.ClientDetail;
import com.serverapp.model.ClientProcess;
import com.serverapp.service.implement.ProcessDetailServer;
import com.serverapp.service.implement.ScreenCaptureServer;
import com.serverapp.socket.SocketManager;
import com.serverapp.util.CurrentType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import lombok.Setter;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    private AnchorPane processTableContainer;

    @FXML
    private AnchorPane panelPortInclude;

    @Setter
    private String fxmlPath = "/view/client-process.fxml";


    @FXML
    TextField searchField;


    private String currentClientIp;

    ProcessDetailServer processDetailServer;

    ProcessTableController processTableController;
    // Phương thức để load và nhúng client-processtable.fxml
    private void loadProcessTable() {
        try {
            // Load FXML của ProcessTable
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/component/client-processtable.fxml"));
            Parent processTableView = loader.load();
            processTableController = loader.getController();
            // Thêm ProcessTable vào processTableContainer (AnchorPane)
            processTableContainer.getChildren().clear();  // Xóa các thành phần cũ
            processTableContainer.getChildren().add(processTableView);  // Thêm bảng mới

            // Thiết lập bộ lọc tìm kiếm
            setupSearchField();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void setupSearchField() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (processTableController != null) {
                processTableController.filterProcessByPIDOrName(newValue);
            }
        });
    }


    @FXML
    public void initialize() throws IOException {
        System.out.println("Client Process Controller run");
        CurrentType.getInstance().setType(RequestType.PROCESS_LIST);
        currentClientIp = AppController.getInstance().getCurrentClientIp();
        SocketManager.getInstance().sendCurrentRequestType(currentClientIp);
        loadProcessTable();
        processDetailServer = new ProcessDetailServer(this);
        processDetailServer.start();
    }


    @Override
    public void update() {
        Platform.runLater(() -> {
            processTableController.updateProcessTable();
        });
        System.out.println("Update complete");
    }

    @Override
    public void stop() throws IOException {
        processDetailServer.stop();
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
}
