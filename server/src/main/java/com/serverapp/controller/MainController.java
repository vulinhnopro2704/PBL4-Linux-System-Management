package com.serverapp.controller;

import com.serverapp.model.ChatModel;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class MainController {

    @FXML
    private ImageView adminAvatar;

    @FXML
    private Label adminName;

    @FXML
    private Label adminIpAddress;

    @FXML
    private ListView<String> chatListView;

    @FXML
    private TextField messageInput;

    @FXML
    private Button sendButton;

    @FXML
    private Button imageButton;

    private ObservableList<String> chatMessages;

    public void initialize() {
        chatMessages = FXCollections.observableArrayList();
        chatListView.setItems(chatMessages);

        sendButton.setOnAction(event -> sendMessage());
        imageButton.setOnAction(event -> selectImage());
    }

    private void sendMessage() {
        String message = messageInput.getText();
        if (!message.isEmpty()) {
            chatMessages.add("Server: " + message);
            // Call the server to send the message to the client
            ChatModel.sendToClient(message);
            messageInput.clear();
        }
    }

    private void selectImage() {
        // Handle image selection and sending logic
        System.out.println("Select and send an image or video to the client");
    }

    public void receiveMessage(String message) {
        chatMessages.add("Client: " + message);
    }
}
