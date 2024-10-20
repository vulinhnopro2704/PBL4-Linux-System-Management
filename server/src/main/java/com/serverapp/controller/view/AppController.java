package com.serverapp.controller.view;

import com.serverapp.controller.IController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import lombok.Setter;

import java.io.IOException;

public class AppController implements IController {
    private static AppController _INSTANCE;
    private IController currentController;

    public static AppController getInstance() {
        if (_INSTANCE == null) {
            _INSTANCE = new AppController();
        }
        return _INSTANCE;
    }

    @FXML
    public void initialize() {
        _INSTANCE = this;
        btnSystemMonitoring.getStyleClass().setAll("active-button");
        setFxmlPath("/view/main-system-view.fxml");
        loadPage(this.fxmlPath);
    }

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
        resetButtonStyles();
        btnSystemMonitoring.getStyleClass().setAll("active-button");
        setFxmlPath("/view/main-system-view.fxml");
        loadPage(this.fxmlPath);
    }

    @FXML
    private void handleCommandPrompt(MouseEvent event) {
        resetButtonStyles();
        btnCommandPrompt.getStyleClass().setAll("active-button");
        setFxmlPath("/view/main-command-view.fxml");
        loadPage(this.fxmlPath);
    }

    @FXML
    private void handleFileAndDirectory(MouseEvent event) {
        resetButtonStyles();
        btnFileAndDirectory.getStyleClass().setAll("active-button");
        setFxmlPath("/view/main-filedirectory-view.fxml");
        loadPage(this.fxmlPath);
    }

    @FXML
    private void handleInstallAndUpdate(MouseEvent event) {
        resetButtonStyles();
        btnInstallAndUpdate.getStyleClass().setAll("active-button");
        setFxmlPath("/view/main-install-view.fxml");
        loadPage(this.fxmlPath);
    }

    @FXML
    private void handleSecurityManagement(MouseEvent event) {
        resetButtonStyles();
        btnSecurityManagement.getStyleClass().setAll("active-button");
        setFxmlPath("/view/main-security-view.fxml");
        loadPage(this.fxmlPath);
    }

    @FXML
    private void handleSetting(MouseEvent event) {
        resetButtonStyles();
        btnSetting.getStyleClass().setAll("active-button");
        setFxmlPath("/view/main-setting-view.fxml");
        loadPage(this.fxmlPath);
    }

    // Method to load pages into the contentArea
    public void loadPage(String fxmlPath) {
        if (fxmlPath != null) {
            try {
                System.out.println(fxmlPath);
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

    @Override
    public void stop() {
        System.out.println("AppController stop");
        _INSTANCE = null;
        Platform.exit();
    }
}