package com.myapp.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class MainController {

    @FXML
    private void handleConnectToServer(ActionEvent event) {
        //TODO: Code to connect to server

        // Show information dialog if connected to server successfully
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText("Connected to server.");
        alert.showAndWait();
    }
}
