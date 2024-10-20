package com.serverapp.controller.view;

import com.serverapp.controller.IController;
import com.serverapp.enums.RequestType;
import com.serverapp.util.CurrentType;
import com.serverapp.service.implement.ScreenCaptureServer;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ClientScreenController implements IController {

    @FXML
    private Label btnGeneral;

    @FXML
    private Label btnProcess;

    @FXML
    private Label btnPerformance;

    @FXML
    private Label btnScreen;

    @FXML
    private Pane screenPane;


    private ImageView imageView;

    @FXML
    public void viewchange() {
        btnGeneral.setOnMouseClicked(event -> loadPage("/view/client-general.fxml"));
        btnProcess.setOnMouseClicked(event -> loadPage("/view/client-process.fxml"));
        btnPerformance.setOnMouseClicked(event -> loadPage("/view/client-performance.fxml"));
        btnScreen.setOnMouseClicked(event -> loadPage("/view/client-screen.fxml"));
    }

    private void loadPage(String fxmlPath) {
        AppController.getInstance().loadPage(fxmlPath);
    }

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

    @Override
    public void stop() {

    }

    public void updateScreenCapture(ImageIcon imageIcon) {
        Platform.runLater(() -> {
            BufferedImage bufferedImage = (BufferedImage) imageIcon.getImage();
            Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
            imageView.setImage(fxImage);
        });
    }

}