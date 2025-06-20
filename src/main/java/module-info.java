module app.tracktune {
    requires javafx.swing;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires javafx.media;
    requires MaterialFX;
    requires java.sql.rowset;
    requires org.kordamp.ikonli.javafx;
    requires jdk.compiler;
    requires org.apache.pdfbox;

    opens app.tracktune to javafx.fxml;
    opens app.tracktune.controller to javafx.fxml;
    opens app.tracktune.controller.admin to javafx.fxml;
    opens app.tracktune.controller.authenticatedUser to javafx.fxml;
    opens app.tracktune.controller.authentication to javafx.fxml;
    opens app.tracktune.controller.common to javafx.fxml;

    exports app.tracktune;
    exports app.tracktune.utils;
    exports app.tracktune.controller;
    exports app.tracktune.controller.admin;
    exports app.tracktune.controller.pendingUser;
    exports app.tracktune.controller.authenticatedUser;
    exports app.tracktune.controller.authentication;
    exports app.tracktune.controller.common;
    exports app.tracktune.model;
    exports app.tracktune.model.user;
    exports app.tracktune.model.genre;
    exports app.tracktune.model.author;
    exports app.tracktune.model.resource;
    exports app.tracktune.model.track;
    exports app.tracktune.model.musicalInstrument;
    exports app.tracktune.model.comments;
    opens app.tracktune.utils to javafx.fxml;
}