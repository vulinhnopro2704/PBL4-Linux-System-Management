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

    opens com.serverapp to javafx.fxml;
    exports com.serverapp;
    exports com.serverapp.controller.view;
    opens com.serverapp.controller.view to javafx.fxml;
    exports com.serverapp.controller.component;
    opens com.serverapp.controller.component to javafx.fxml;
}