package app.tracktune.controller.administrator;

import app.tracktune.model.user.PendingUser;
import app.tracktune.utils.Strings;
import app.tracktune.view.ViewManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {
    @FXML
    private MediaPlayer mediaPlayer;
    @FXML
    private MediaView mediaPlayerView;

    private void initMediaPlayer() {
        try{
            String videoPath = "C:\\Users\\ACER\\Videos\\test3.mp4";
            Media media = new Media(new File(videoPath).toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayerView.setMediaPlayer(mediaPlayer);
            mediaPlayer.play();
        }catch(Exception e){
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.MEDIA_NOT_SUPPORTED, Strings.MEDIA_NOT_SUPPORTED, Alert.AlertType.ERROR);
            disposeMediaPlayer();
        }
    }

    @FXML
    public void viewTracks(){
        initMediaPlayer();
    }

    @FXML
    public void handleLogout() {
        ViewManager.navigateToLogin();
    }

    public void disposeMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    @FXML
    private Label LbStatusValue;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Object user = ViewManager.getUser();

        if (user instanceof PendingUser) {
            PendingUser pendingUser = (PendingUser) user;
            int status = pendingUser.getStatus().ordinal();
            switch (status) {
                case 0 -> LbStatusValue.setStyle("-fx-text-fill: orange;");
                case 2 -> LbStatusValue.setStyle("-fx-text-fill: #870505;");
            }

            LbStatusValue.setText(pendingUser.getStatus().toString());
        } else {
            LbStatusValue.setText("Utente non valido");
            LbStatusValue.setStyle("-fx-text-fill: gray;");
        }
    }
}