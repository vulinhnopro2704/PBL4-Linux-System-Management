package com.serverapp.controller;

import javafx.fxml.FXML;

import java.io.IOException;

public interface IController {
    @FXML
    void initialize() throws IOException;
    void update();
    void stop();
}
