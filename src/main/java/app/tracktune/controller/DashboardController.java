package app.tracktune.controller;

import app.tracktune.utils.Strings;
import app.tracktune.view.ViewManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import java.io.File;

public class DashboardController {
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
}