package com.serverapp;

import com.serverapp.controller.MainController;
import com.serverapp.model.ChatModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/serverapp/server/hello-view.fxml"));
            Parent root = loader.load();

//            MainController controller = loader.getController();
//            ChatModel.startServer(controller);

            primaryStage.setTitle("Server Application");
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception to the console
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
