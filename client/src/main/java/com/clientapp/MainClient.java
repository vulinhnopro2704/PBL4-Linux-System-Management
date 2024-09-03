package com.clientapp;

import com.clientapp.util.INetworkClient;
import com.clientapp.util.ISystemInfoCollector;
import com.clientapp.util.implement.SystemInfoCollector;

import com.clientapp.util.implement.TCPClient;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainClient extends Application {

    @Override
    public void start(Stage stage)  {
        ISystemInfoCollector infoCollector = new SystemInfoCollector();
        INetworkClient networkClient = new TCPClient(infoCollector);

        // Send system info to server
        networkClient.sendSystemInfo();

        // Double-check System Information of Client Computer
        System.out.println(infoCollector.getSystemInfoAsJson());
    }

    public static void main(String[] args) {
        launch(args);
    }
}

