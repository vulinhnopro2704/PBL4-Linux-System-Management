module com.myapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires eu.hansolo.tilesfx;
    requires jsch;

    opens com.myapp to javafx.fxml;
    exports com.myapp;
    exports com.myapp.controller;
    opens com.myapp.controller to javafx.fxml;
}