package app.tracktune.controller.admin;

import app.tracktune.model.user.Administrator;
import app.tracktune.utils.Frames;
import app.tracktune.utils.Strings;
import app.tracktune.view.ViewManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for managing the admin dashboard and their related functionalities.
 * The dashboard includes a media player to display a video, handles user logout,
 * and supports switching between different views in the main content
 */
public class AdminDashboardController implements Initializable {
    @FXML private StackPane mainContent;
    @FXML private MediaView mediaPlayerView;
    private MediaPlayer mediaPlayer;
    private Administrator admin;
    private Node dashboardContent;

    /**
     * Initializes the controller by checking if the logged-in user is an {@link Administrator}
     * and assigns it to the {@code admin} variable.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dashboardContent = mainContent.getChildren().getFirst();
        if (ViewManager.getSessionUser() instanceof Administrator administrator) {
            admin = administrator;
        }
    }

    /**
     * Loads and displays the dashboard view by updating the main content area by initial content
     */
    @FXML
    private void handleDashboard(){
        try{
            mainContent.getChildren().setAll(dashboardContent);
        } catch(Exception e) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
            System.err.println(e.getMessage());
        }
    }

    /**
     * Starts the media player to play a video when the "View Tracks" button is clicked.
     */
    @FXML
    public void handleTracks() {
        try{
            initMediaPlayer();
        } catch(Exception e) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
            System.err.println(e.getMessage());
        }
    }

    /**
     * Loads and displays the Genre view by updating the main content area of the dashboard.
     */
    @FXML
    private void handleGenre(){
        setMainContent(Frames.GENRES_VIEW_PATH_VIEW_PATH);
    }

    /**
     * Loads and displays the requests view by updating the main content area of the dashboard.
     */
    @FXML
    public void handleRequests() {
        setMainContent(Frames.REQUESTS_VIEW_PATH);
    }

    @FXML
    public void handleAuthors() {
        setMainContent(Frames.REQUESTS_AUTHORS_PATH);
    }

    /**
     * Sets the main content area of the dashboard to display a new view.
     * The new view is loaded from the specified FXML file path.
     *
     * @param contentPath Path to the FXML file to load and display in the main content area.
     */
    private void setMainContent(String contentPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(contentPath));
            Parent view = loader.load();
            mainContent.getChildren().setAll(view);
        } catch (IOException e) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.MEDIA_NOT_SUPPORTED, Strings.MEDIA_NOT_SUPPORTED, Alert.AlertType.ERROR);
            System.err.println(e.getMessage());
        }
    }

    /**
     * Logs out the current user by calling the {@link ViewManager#logout()} method.
     * Displays an error alert if the logout process fails.
     */
    @FXML
    public void handleLogout() {
        try{
            ViewManager.logout();
        } catch(Exception e) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
            System.err.println(e.getMessage());
        }
    }

    /**
     * Initializes the media player with a video file located at a hardcoded path.
     * If the media file is not supported or cannot be loaded, an error alert is displayed.
     */
    private void initMediaPlayer() {
        String videoPath = "C:\\Users\\ACER\\Videos\\test3.mp4";
        Media media = new Media(new File(videoPath).toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        mediaPlayerView.setMediaPlayer(mediaPlayer);
        mediaPlayer.play();
    }

    /**
     * Stops the media player if it is playing, releasing resources used by the player.
     */
    public void disposeMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }
}
