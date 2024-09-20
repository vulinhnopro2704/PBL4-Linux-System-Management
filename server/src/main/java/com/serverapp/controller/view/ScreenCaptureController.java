package com.serverapp.controller.view;

import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

import javax.swing.*;


public class ScreenCaptureController {
    @FXML
    private Pane screenPane;
    private SwingNode swingNode;

    @FXML
    public void initialize() {
        swingNode = new SwingNode();
        screenPane.getChildren().add(swingNode);

    }

    public void updateScreenCapture(ImageIcon imageIcon) {
        Platform.runLater(() -> {
            swingNode.setContent(new JLabel(imageIcon));
        });
    }
}
