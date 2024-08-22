package com.myapp.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;

public class ClientController {
    @FXML
    private void handleSendFile(ActionEvent event) {
        // Code to send file to server
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText("File sent to server.");
        alert.showAndWait();
    }
}
