package app.tracktune.config;

import java.io.File;

/**
 * Configuration class for application-wide settings related to the TrackTune app.
 * <p>
 * This class defines constants used for UI titles, database paths, and handles
 * creation of the data directory if it does not exist.
 */
public class AppConfig {

    /**
     * The title of the application, displayed in the window title bars.
     */
    public static final String APP_TITLE = "TrackTune";

    /**
     * Path to the directory where application data is stored.
     * <p>
     * This version is intended for debugging and points to the source resources folder.
     * Uncomment the deployment version for production builds.
     */
    public static final String DATA_DIR = "classes/database"; // For deployment
    //public static final String DATA_DIR = "src/resources/database"; // For debugging

    /**
     * Full path to the SQLite database file used by the application.
     */
    public static final String DATABASE_PATH = DATA_DIR + "/database.db";

    // Static block to ensure the data directory exists when the application starts.
    static {
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            if (dataDir.mkdirs())
                System.out.println("Data directory created");
        }
    }
}