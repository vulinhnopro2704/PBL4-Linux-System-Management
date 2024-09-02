module com.clientapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires transitive javafx.graphics;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.github.oshi;

    opens com.clientapp to javafx.fxml;
    exports com.clientapp;
    exports com.clientapp.controller;
    opens com.clientapp.controller to javafx.fxml;
}