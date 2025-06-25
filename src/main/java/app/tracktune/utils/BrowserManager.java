package app.tracktune.utils;

import app.tracktune.view.ViewManager;
import javafx.scene.control.Alert;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Singleton class responsible for managing browser-related operations within the application.
 *
 * <p>This class ensures that there is only one instance controlling browser interactions,
 * providing a centralized way to open URLs or perform other browser-related tasks.</p>
 */
public class BrowserManager {
    /**
     * Singleton instance of {@code BrowserManager}.
     * Ensures a single point of access to the browser routing
     */
    private static BrowserManager instance;
    /**
     * Create the instance of the browser manager following singleton pattern
     */
    private BrowserManager() {}

    /**
     * Retrieves the singleton instance of BrowserManager.
     *
     * @return the BrowserManager instance
     */
    public static BrowserManager getInstance() {
        return instance;
    }

    /**
     * Initializes the browser manager safely
     */
    public static void initialize() {
        if (instance == null) {
            instance = new BrowserManager();
        }
    }

    /**
     * Opens the given URL in the default system browser.
     *
     * @param url the URL to open
     */
    public void browse(String url) {
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
