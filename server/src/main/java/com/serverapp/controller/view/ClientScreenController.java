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
import lombok.Setter;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;

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

    @Setter
    private String fxmlPath = "/view/client-screen.fxml";

    private ImageView imageView;
    private String currentClientIp;
    ScreenCaptureServer screenCaptureServer;

    @FXML
    public void initialize() throws IOException {
        System.out.println("Client Screen Controller run");
        // Initialize and start the screen capture server
        CurrentType.getInstance().setType(RequestType.SCREEN_CAPTURE);
        currentClientIp = AppController.getInstance().getCurrentClientIp();
        screenCaptureServer = new ScreenCaptureServer(this);

        screenCaptureServer.start();
        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.fitWidthProperty().bind(screenPane.widthProperty());
        imageView.fitHeightProperty().bind(screenPane.heightProperty());
        screenPane.getChildren().add(imageView);
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

    @Override
    public void update() {

    }

    @Override
    public void stop() throws IOException {
        screenCaptureServer.stop();
    }

    public void updateScreenCapture(ImageIcon imageIcon) {
        Platform.runLater(() -> {
            BufferedImage bufferedImage = (BufferedImage) imageIcon.getImage();
            Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
            imageView.setImage(fxImage);
        });
    }

}