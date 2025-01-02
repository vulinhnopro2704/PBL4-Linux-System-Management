module com.serverapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;


    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires jsch;
    requires java.json;
    requires static lombok;
    requires com.google.gson;
    requires com.github.oshi;
    requires javafx.swing;
    requires java.logging;
    requires jdk.management;
    requires java.prefs;

    opens com.serverapp to javafx.fxml;
    exports com.serverapp;
    exports com.serverapp.controller.view;
    opens com.serverapp.controller.view to javafx.fxml;
    exports com.serverapp.controller.component;
    exports com.serverapp.socket;
    opens com.serverapp.socket to javafx.fxml;
    exports com.serverapp.service;
    opens com.serverapp.service to javafx.fxml;
    exports com.serverapp.service.implement;
    opens com.serverapp.service.implement to javafx.fxml;
    exports com.serverapp.util;
    opens com.serverapp.util to javafx.fxml;
    exports com.serverapp.model;
    opens com.serverapp.model to javafx.fxml;
    exports com.serverapp.database;
    opens com.serverapp.database to javafx.fxml;
    exports com.serverapp.enums;
    opens com.serverapp.enums to javafx.fxml;
    exports com.serverapp.controller;
    opens com.serverapp.controller to javafx.fxml;
    opens com.serverapp.controller.component to javafx.fxml;

}