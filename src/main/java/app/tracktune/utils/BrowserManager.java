package app.tracktune.utils;

import app.tracktune.view.ViewManager;
import javafx.scene.control.Alert;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class BrowserManager {
    public static void browse(String url) {
        try {
            URI uri = new URI(url);
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(uri);
            } else {
                ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERR_GENERAL, Strings.ERROR_DURING_OPEN_BROWSER, Alert.AlertType.ERROR);
            }
        } catch (IOException | URISyntaxException e) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERR_GENERAL, Strings.ERROR_DURING_OPEN_BROWSER, Alert.AlertType.ERROR);
            System.err.println(e.getMessage());
        }
    }
}