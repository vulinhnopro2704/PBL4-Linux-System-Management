package com.serverapp.controller.view;

import com.serverapp.controller.IController;
import com.serverapp.socket.SocketManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

public class AppController implements IController {
    private static AppController _INSTANCE;
    private IController currentController;
    @Getter
    @Setter
    private String currentClientIp;

    @FXML
    public AnchorPane contentArea;

    @FXML
    private Button btnSystemMonitoring;

    @FXML
    private Button btnCommandPrompt;

    @FXML
    private Button btnFileAndDirectory;

    @FXML
    private Button btnInstallAndUpdate;

    @FXML
    private Button btnSecurityManagement;

    @FXML
    private Button btnSetting;

    @Setter
    private String fxmlPath;

    public static AppController getInstance() {
        if (_INSTANCE == null) {
            _INSTANCE = new AppController();
        }
        return _INSTANCE;
    }

    @FXML
    public void initialize() {
        _INSTANCE = this;
        SocketManager.getInstance().startListening();
        btnSystemMonitoring.getStyleClass().setAll("active-button");
        loadPage("/view/main-system-view.fxml");
    }

    // Reset button styles to default
    private void resetButtonStyles() {
        btnSystemMonitoring.getStyleClass().setAll("normal-button");
        btnCommandPrompt.getStyleClass().setAll("normal-button");
        btnFileAndDirectory.getStyleClass().setAll("normal-button");
        btnInstallAndUpdate.getStyleClass().setAll("normal-button");
        btnSecurityManagement.getStyleClass().setAll("normal-button");
        btnSetting.getStyleClass().setAll("normal-button");
    }

    @FXML
    private void handleSystemMonitoring(MouseEvent event) {
        if (!fxmlPath.equals("/view/main-system-view.fxml")){
            resetButtonStyles();
            btnSystemMonitoring.getStyleClass().setAll("active-button");
            System.out.println("Navigating to System Monitoring");
            loadPage("/view/main-system-view.fxml");
        }
    }

    @FXML
    private void handleCommandPrompt(MouseEvent event) {
        if (!fxmlPath.equals("/view/main-command-view.fxml")){
            resetButtonStyles();
            btnCommandPrompt.getStyleClass().setAll("active-button");
            System.out.println("Navigating to Command Prompt");
            loadPage("/view/main-command-view.fxml");
        }
    }

    @FXML
    private void handleFileAndDirectory(MouseEvent event) {
        if (!fxmlPath.equals("/view/main-filedirectory-view.fxml")){
            resetButtonStyles();
            btnFileAndDirectory.getStyleClass().setAll("active-button");
            System.out.println("Navigating to File and Directory");
            loadPage("/view/main-filedirectory-view.fxml");
        }
    }

    @FXML
    private void handleInstallAndUpdate(MouseEvent event) {
        if (!fxmlPath.equals("/view/main-install-view.fxml")){
            resetButtonStyles();
            btnInstallAndUpdate.getStyleClass().setAll("active-button");
            loadPage("/view/main-install-view.fxml");
        }
    }

    @FXML
    private void handleSecurityManagement(MouseEvent event) {
        if (!fxmlPath.equals("/view/main-security-view.fxml")){
            resetButtonStyles();
            btnSecurityManagement.getStyleClass().setAll("active-button");
            System.out.println("Navigating to Security");
            loadPage("/view/main-security-view.fxml");
        }
    }

    @FXML
    private void handleSetting(MouseEvent event) {
        if (!fxmlPath.equals("/view/main-setting-view.fxml")){
            resetButtonStyles();
            btnSetting.getStyleClass().setAll("active-button");
            loadPage("/view/main-setting-view.fxml");
        }
    }

    // Method to load pages into the contentArea
    public void loadPage(String fxmlPath) {
        if (fxmlPath != null && !fxmlPath.isEmpty() && !fxmlPath.equals(this.fxmlPath)) {
            try {
                System.out.println(fxmlPath);
                setFxmlPath(fxmlPath);
                if (currentController != null) {
                    currentController.stop();
                }
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Parent root = loader.load();
                currentController = loader.getController();

                contentArea.getChildren().clear();
                contentArea.getChildren().add(root);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void update() {
        if (currentController != null) {
            currentController.update();
        }
        else {
            System.out.println("Current Controller is NULL, can't update");
        }
    }

    @Override
    public void stop() {
        System.out.println("AppController stop");
        _INSTANCE = null;
        Platform.exit();
    }
}