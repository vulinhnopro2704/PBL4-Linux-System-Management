package com.clientapp;

import com.clientapp.util.INetworkClient;
import com.clientapp.util.IScreenCaptureClient;
import com.clientapp.util.ISystemInfoCollector;
import com.clientapp.util.implement.ScreenCaptureClient;
import com.clientapp.util.implement.ScreenCaptureClientUDP;
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

        // Close the connection
        networkClient.closeConnection();


//        IScreenCaptureClient screenCaptureClient = new ScreenCaptureClient("localhost", 9999);
//        new Thread(() -> {
//            while (true) {
//                screenCaptureClient.captureAndSendScreen();
//                try {
//                    Thread.sleep(80); // Capture and send every second
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();

//        ScreenCaptureClientUDP screenCaptureClient = new ScreenCaptureClientUDP("localhost", 9999);
//        new Thread(() -> {
//            while (true) {
//                screenCaptureClient.captureAndSendScreen();
//                try {
//                    Thread.sleep(200); // Capture and send every second
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

