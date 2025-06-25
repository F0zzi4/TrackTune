package app.tracktune;

import app.tracktune.exceptions.TrackTuneException;
import app.tracktune.utils.*;
import app.tracktune.view.ViewManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

/**
 * Main class for launching the TrackTune JavaFX application.
 */
public class Main extends Application {

    /**
     * Singleton instance of the {@link DatabaseManager} used to manage DB operations.
     */
    public static DatabaseManager dbManager;

    /**
     * Primary application stage, used as the root window for the application.
     */
    public static Stage root;

    /**
     * Main procedure to start the application
     * @param root : Main frame that will be shown
     */
    @Override
    public void start(Stage root){
        try{
            Main.root = root;
            initDatabase();
            ViewManager.initView(Frames.LOGIN_VIEW_PATH);
        }catch(TrackTuneException e){
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERR_GENERAL, e.getMessage(), Alert.AlertType.ERROR);
        }
        catch(Exception e){
            System.err.println(e.getMessage());
        }
    }

    /**
     * Initialize connection with database
     */
    private void initDatabase(){
        dbManager = DatabaseManager.getInstance();
        if(!dbManager.isConnected()){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(Strings.ERROR);
            alert.setHeaderText(Strings.CONN_FAILED);
            alert.setContentText(Strings.DB_CONN_FAILED);
            alert.showAndWait();
            Platform.exit();
        }
    }

    /**
     * Entry point of the program
     * @param args : Arguments given by the input
     */
    public static void main(String[] args) {
        launch();
    }
}