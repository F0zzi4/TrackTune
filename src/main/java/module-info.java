module app.tracktune {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires javafx.media;

    opens app.tracktune to javafx.fxml;
    opens app.tracktune.controller to javafx.fxml;
    opens app.tracktune.controller.admin to javafx.fxml;

    exports app.tracktune;
    exports app.tracktune.controller;
    exports app.tracktune.controller.admin;
    exports app.tracktune.controller.pendingUser;
    exports app.tracktune.model;
    exports app.tracktune.model.user;
}