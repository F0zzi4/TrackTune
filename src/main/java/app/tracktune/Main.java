package app.tracktune;

import app.tracktune.config.AppConfig;
import app.tracktune.exceptions.TrackTuneException;
import app.tracktune.model.DatabaseManager;
import app.tracktune.utils.Strings;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;

public class Main extends Application {
    public static DatabaseManager dbManager;
    public static Stage root;
    private final int APP_WIDTH = 700;
    private final int APP_HEIGHT = 560;
    private static final double ICON_WIDTH = 50;
    private static final double ICON_HEIGHT = 50;

    /**
     * Main procedure to start the application
     * @param root : Main frame that will be shown
     */
    @Override
    public void start(Stage root){
        try{
            Main.root = root;
            initDatabase();
            loadView(root);
        }catch(TrackTuneException e){
            setAndShowAlert(Strings.ERROR, Strings.ERROR, e.getMessage(), Alert.AlertType.ERROR, root);
        }
        catch(Exception e){
            System.err.println(e.getMessage());
        }
    }

    /**
     * Initialize connection with database
     */
    public void initDatabase(){
        dbManager = new DatabaseManager();
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
     * Load basic configuration for the given root
     * @param root : Frame that will be shown
     * @throws IOException : Input / Output Exception
     */
    public void loadView(Stage root) throws IOException{
        FXMLLoader viewLoader = new FXMLLoader(Main.class.getResource(Strings.MAIN_FRAME_VIEW));
        Scene scene = new Scene(viewLoader.load(), 700, 550);
        root.setTitle(AppConfig.APP_TITLE);
        root.setResizable(false);
        Image icon = new Image(Strings.MAIN_ICON_PATH);
        root.getIcons().add(icon);
        setStageOnCurrentScreen(root);
        root.setScene(scene);
        root.show();
    }

    /**
     * Set the given stage on the current screen
     * @param stage : Frame that will be set
     */
    private void setStageOnCurrentScreen(Stage stage){
        // get current mouse position
        Point mousePoint = MouseInfo.getPointerInfo().getLocation();
        // get the mouse containing screen
        Screen targetScreen = Screen.getScreens()
                .stream()
                .filter(screen -> screen.getBounds().contains(mousePoint.x, mousePoint.y))
                .findFirst()
                .orElse(Screen.getPrimary());
        // set the root window at the exact center of the screen
        Rectangle2D bounds = targetScreen.getVisualBounds();
        stage.setX(bounds.getMinX() + (bounds.getWidth() - APP_WIDTH) / 2);
        stage.setY(bounds.getMinY() + (bounds.getHeight() - APP_HEIGHT) / 2);
    }

    /**
     * Set and show a specific alert
     * @param title : Title to be shown
     * @param header : Header to be shown
     * @param content : Content to be shown
     */
    public static void setAndShowAlert(String title, String header, String content, Alert.AlertType type, Stage stage){
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.initOwner(stage);
        alert.showAndWait();
    }

    /**
     * Entry point of the program
     * @param args : Arguments given by the input
     */
    public static void main(String[] args) {
        launch();
    }
}