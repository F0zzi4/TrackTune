package app.tracktune.config;

import java.io.File;

public class AppConfig {
    // Application settings
    public static final String APP_TITLE = "TrackTune";
    
    // Data storage settings
    public static final String DATA_DIR = "src/resources/data";
    public static final String DATABASE_PATH = DATA_DIR + "/login.db";
    
    // Create data directory if it doesn't exist
    static {
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            if(dataDir.mkdirs())
                System.out.println("Data directory created");
        }
    }
}