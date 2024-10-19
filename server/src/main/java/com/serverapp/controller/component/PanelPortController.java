package com.serverapp.controller.component;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class PanelPortController {
    @FXML
    private TextArea logContent;

    @FXML
    public void initialize() {
        // Initialization logic if needed
    }

    // Method to update the log content
    public void updateLogContent(String log) {
        logContent.appendText(log + "\n");
    }
}