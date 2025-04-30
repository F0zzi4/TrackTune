package app.tracktune;

import app.tracktune.config.AppConfig;
import app.tracktune.model.DatabaseManager;
import app.tracktune.utils.Strings;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;

public class Main extends Application {
    public static DatabaseManager dbManager;
    private final int APP_WIDTH = 700;
    private final int APP_HEIGHT = 550;

    @Override
    public void start(Stage stage) throws IOException {
        initDatabase();
        loadView(stage);
    }

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

    public void loadView(Stage stage) throws IOException{
        FXMLLoader root = new FXMLLoader(Main.class.getResource(Strings.MAIN_FRAME_VIEW));
        Scene scene = new Scene(root.load(), 700, 550);
        stage.setTitle(AppConfig.APP_TITLE);
        stage.setResizable(false);
        Image icon = new Image(Strings.MAIN_ICON_PATH);
        stage.getIcons().add(icon);
        setStageOnCurrentScreen(stage);
        stage.setScene(scene);
        stage.show();
    }

    private void setStageOnCurrentScreen(Stage stage){
        // Get current mouse position
        Point mousePoint = MouseInfo.getPointerInfo().getLocation();
        // Get the mouse containing screen
        Screen targetScreen = Screen.getScreens()
                .stream()
                .filter(screen -> screen.getBounds().contains(mousePoint.x, mousePoint.y))
                .findFirst()
                .orElse(Screen.getPrimary());
        // Set the root window at the center of the screen
        Rectangle2D bounds = targetScreen.getVisualBounds();
        stage.setX(bounds.getMinX() + (bounds.getWidth() - APP_WIDTH) / 2);
        stage.setY(bounds.getMinY() + (bounds.getHeight() - APP_HEIGHT) / 2);
    }

    public static void main(String[] args) {
        launch();
    }
}