package app.tracktune;

import app.tracktune.config.AppConfig;
import app.tracktune.model.DatabaseManager;
import app.tracktune.utils.Strings;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    public static DatabaseManager dbManager;

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
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource(Strings.MAIN_FRAME_VIEW));
        Scene scene = new Scene(fxmlLoader.load(), 520, 420);
        stage.setTitle(AppConfig.APP_TITLE);
        Image icon = new Image(Strings.MAIN_ICON_PATH);
        stage.getIcons().add(icon);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}