package com.serverapp.controller.view;

import com.serverapp.enums.RequestType;
import com.serverapp.util.implement.CurrentType;
import com.serverapp.util.implement.ScreenCaptureServer;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ScreenCaptureController {
    @FXML
    private Pane screenPane;
    private ImageView imageView;

    @FXML
    public void initialize() throws IOException {
        // Initialize and start the screen capture server
        CurrentType.getInstance().setType(RequestType.SCREEN_CAPTURE);
        ScreenCaptureServer server = new ScreenCaptureServer(this);

        server.start();
        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.fitWidthProperty().bind(screenPane.widthProperty());
        imageView.fitHeightProperty().bind(screenPane.heightProperty());
        screenPane.getChildren().add(imageView);
    }

    public void updateScreenCapture(ImageIcon imageIcon) {
        Platform.runLater(() -> {
            BufferedImage bufferedImage = (BufferedImage) imageIcon.getImage();
            Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
            imageView.setImage(fxImage);
        });
    }
}