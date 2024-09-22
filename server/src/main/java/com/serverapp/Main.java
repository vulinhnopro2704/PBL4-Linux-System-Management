package com.serverapp;

import com.serverapp.controller.view.ScreenCaptureController;
import com.serverapp.util.ITCPServer;
import com.serverapp.util.implement.ScreenCaptureServer;
import com.serverapp.util.implement.ScreenCaptureServerUDP;
import com.serverapp.util.implement.TCPServer;
import com.serverapp.controller.view.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main-view.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();

//        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/screen-capture.fxml"));
//        Parent root = loader.load();
//        ScreenCaptureController screenCaptureController = loader.getController();
//
//        // Initialize and start the screen capture server
//        screenCaptureController.initialize();
//        ScreenCaptureServer server = new ScreenCaptureServer(9999, screenCaptureController);
//        server.start();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}