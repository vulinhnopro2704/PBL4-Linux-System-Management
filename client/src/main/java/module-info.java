module com.clientapp {

    requires eu.hansolo.tilesfx;
    requires com.fasterxml.jackson.databind;
    requires com.google.gson;
    requires static lombok;
    requires java.desktop;
    requires com.github.oshi;
    requires jdk.management;

    opens com.clientapp to javafx.fxml;
    exports com.clientapp;
    exports com.clientapp.model;
    exports com.clientapp.util;
    exports com.clientapp.enums;
    exports com.clientapp.service;
    opens com.clientapp.model to com.google.gson;
    exports com.clientapp.socket;
    opens com.clientapp.socket to javafx.fxml;
    exports com.clientapp.service.implement;
}