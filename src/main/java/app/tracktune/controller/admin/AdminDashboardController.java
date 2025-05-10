package app.tracktune.controller.admin;

import app.tracktune.model.user.Administrator;
import app.tracktune.utils.Strings;
import app.tracktune.view.ViewManager;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {
    @FXML
    private MediaPlayer mediaPlayer;
    @FXML
    private MediaView mediaPlayerView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (ViewManager.getSessionUser() instanceof Administrator admin) {
            // TODO
        } else {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.SOMETHING_WENT_WRONG, Strings.ERR_USER_NOT_ALLOWED, Alert.AlertType.ERROR);
        }
    }

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
}