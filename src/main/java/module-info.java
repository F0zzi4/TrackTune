module app.tracktune {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires java.desktop;

    opens app.tracktune to javafx.fxml;
    exports app.tracktune;
    exports app.tracktune.controller;
    exports app.tracktune.model;
    opens app.tracktune.controller to javafx.fxml;
}