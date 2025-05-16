package app.tracktune.controller.authenticatedUser;

import app.tracktune.model.user.AuthenticatedUser;
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

public class AuthenticatedUserDashboardController implements Initializable {
    private MediaPlayer mediaPlayer;
    @FXML private MediaView mediaPlayerView;
    @FXML private StackPane mainContent;
    @FXML private Label LblWelcome;
    private AuthenticatedUser authUser;
    private Node dashboardContent;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dashboardContent = mainContent.getChildren().getFirst();
        if (ViewManager.getSessionUser() instanceof AuthenticatedUser authenticatedUser) {
            this.authUser = authenticatedUser;
            LblWelcome.setText(LblWelcome.getText() + " " + authenticatedUser.getName()+" "+authenticatedUser.getSurname());
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
     * Loads and displays the discover view by updating the main content area
     */
    @FXML
    public void handleDiscover() {
        try{
            setMainContent(Frames.DISCOVER_VIEW_PATH);
        }catch(Exception e){
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
            System.err.println(e.getMessage());
        }
    }

    /**
     * Loads and displays the tracks view by updating the main content area
     */
    @FXML
    public void handleResources(){
        try{
            setMainContent(Frames.RESOURCES_VIEW_PATH);
        }catch(Exception e){
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
            System.err.println(e.getMessage());
        }
    }

    /**
     * Loads and displays the activities view by updating the main content area.
     * It's a log about the activities done by the logged user
     */
    @FXML
    public void handleActivities(){
        try{

        }catch(Exception e){
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
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
        }catch(Exception e){
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
            System.err.println(e.getMessage());
        }
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
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
            System.err.println(e.getMessage());
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
            System.err.println(e.getMessage());
            disposeMediaPlayer();
        }
    }

    public void disposeMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }
}