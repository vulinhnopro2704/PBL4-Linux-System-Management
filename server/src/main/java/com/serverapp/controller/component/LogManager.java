package com.serverapp.controller.component;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class LogManager {
    private static LogManager instance;
    private StringProperty logMessage = new SimpleStringProperty();

    private LogManager() {}

    public static LogManager getInstance() {
        if (instance == null) {
            instance = new LogManager();
        }
        return instance;
    }

    public StringProperty logMessageProperty() {
        return logMessage;
    }

    public void appendMessage(String message) {
        logMessage.set(logMessage.get() + "\n" + message);
    }
}