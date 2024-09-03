package com.serverapp.controller;

import com.serverapp.util.ITCPServer;
import com.serverapp.util.implement.TCPServer;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;

public class MainController {
    @FXML private TextField portField;
    @FXML private TextArea logArea;
    @FXML private TextField hostnameField;
    @FXML private TextField cpuInfoField;
    @FXML private TextField memoryInfoField;
    @FXML private TextField osInfoField;
    @FXML private ListView<String> networkInfoListView;
    @FXML private ListView<String> diskUsageInfoListView;

    private ITCPServer server;

    @FXML
    private void startServer() {
        try {
            int port = Integer.parseInt(portField.getText());
            server = new TCPServer();
            server.setPort(port);
            server.setMainController(this);
            server.start();
            appendLog("Server started on port " + port);
        } catch (NumberFormatException e) {
            appendLog("Invalid port number");
        }
    }

    @FXML
    private void stopServer() {
        if (server != null) {
            server.stop();
            appendLog("Server stopped");
        }
    }

    public void appendLog(String message) {
        logArea.appendText(message + "\n");
    }

    public void updateClientInfo(String jsonData) {
        try (JsonReader jsonReader = Json.createReader(new StringReader(jsonData))) {
            JsonObject json = jsonReader.readObject();

            hostnameField.setText(json.getString("hostname"));
            cpuInfoField.setText(json.getString("cpuInfo"));
            memoryInfoField.setText(json.getString("memoryInfo"));
            osInfoField.setText(json.getString("osInfo"));

            networkInfoListView.getItems().clear();
            JsonArray networkInfo = json.getJsonArray("networkInfo");
            for (int i = 0; i < networkInfo.size(); i++) {
                networkInfoListView.getItems().add(networkInfo.getString(i));
            }

            diskUsageInfoListView.getItems().clear();
            JsonArray diskUsageInfo = json.getJsonArray("diskUsageInfo");
            for (int i = 0; i < diskUsageInfo.size(); i++) {
                diskUsageInfoListView.getItems().add(diskUsageInfo.getString(i));
            }
        }
    }
}