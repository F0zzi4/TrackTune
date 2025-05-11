package app.tracktune.controller.authenticatedUser;

import app.tracktune.model.user.AuthenticatedUser;
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

public class AuthenticatedUserDashboardController implements Initializable {
    @FXML
    private MediaPlayer mediaPlayer;
    @FXML
    private MediaView mediaPlayerView;
    private AuthenticatedUser authUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (ViewManager.getSessionUser() instanceof AuthenticatedUser authenticatedUser) {
            this.authUser = authenticatedUser;
        }
    }

    @FXML
    public void handleLogout() {
        try{
            ViewManager.logout();
        }catch(Exception e){
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
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

    public void disposeMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }
}