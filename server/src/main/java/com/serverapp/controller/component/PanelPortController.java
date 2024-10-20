package com.serverapp.controller.component;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PanelPortController {
    @FXML
    private TextArea logContent;

    @FXML
    public void initialize() {
        // Initialization logic if needed
    }

    // Method to update the log content
    public void updateLogContent(String log) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String logEntry = "[" + timestamp + "] " + log;
        logContent.appendText(logEntry + "\n");
    }
}