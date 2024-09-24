module com.clientapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.github.oshi;
    requires com.fasterxml.jackson.databind;
    requires com.google.gson;
    requires javafx.swing;
    requires static lombok;
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