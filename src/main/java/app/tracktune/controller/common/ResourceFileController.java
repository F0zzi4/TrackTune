package app.tracktune.controller.common;

import app.tracktune.controller.Controller;
import app.tracktune.controller.admin.AdminDashboardController;
import app.tracktune.controller.authenticatedUser.AuthenticatedUserDashboardController;
import app.tracktune.exceptions.TrackTuneException;
import app.tracktune.model.resource.Resource;
import app.tracktune.utils.Frames;
import app.tracktune.utils.ResourceConverter;
import app.tracktune.utils.Strings;
import app.tracktune.view.ViewManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.ResourceBundle;

public class ResourceFileController extends Controller implements Initializable {
    @FXML private StackPane fileContainer;
    @FXML private HBox videoToolBox;
    @FXML private Label lblTitle;
    private final ResourceConverter resourceConverter;
    private MediaPlayer mediaPlayer;
    //CONSTANTS
    private final int defaultSkipTime = 10;
    private final int defaultGapContainerToolBox = 30;
    private final int defaultGapTitle = 10;
    private boolean isPlaying = false;

    public ResourceFileController(Resource resource) {
        resourceConverter = new ResourceConverter(resource);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try{
            Node resourceNode = resourceConverter.createMediaNode(fileContainer.getPrefWidth(), fileContainer.getPrefHeight());
            fileContainer.getChildren().add(resourceNode);
            boolean isMultimedia = resourceNode instanceof MediaView;
            videoToolBox.setVisible(isMultimedia);

            if (!isMultimedia) {
                lblTitle.setLayoutY(lblTitle.getLayoutY() + defaultGapTitle);
                fileContainer.setLayoutY(fileContainer.getLayoutY() + defaultGapContainerToolBox);
            }else{
                mediaPlayer = ((MediaView) resourceNode).getMediaPlayer();
                handlePlayPause();
            }
        }catch(TrackTuneException ex) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, ex.getMessage(), Alert.AlertType.ERROR);
            disposeMediaPlayer();
            handleReturn();
        }catch(Exception e){
            System.err.println(e.getMessage());
            disposeMediaPlayer();
            handleReturn();
        }
    }

    /**
     * Stops the media player if it's active and releases the used resources.
     */
    public void disposeMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }
    }

    /**
     * Starts or resumes the media playback.
     * If an error occurs during playback, an alert is shown and the media player is disposed.
     */
    @FXML
    private void handlePlayPause() {
        try {
            if (mediaPlayer != null) {
                Node node = videoToolBox.getChildren().getFirst();

                if (node instanceof Button button && button.getGraphic() instanceof FontIcon icon) {
                    if (isPlaying) {
                        mediaPlayer.pause();
                        isPlaying = false;
                        icon.setIconLiteral("mdi2p-play");
                    } else {
                        mediaPlayer.play();
                        isPlaying = true;
                        icon.setIconLiteral("mdi2p-pause");
                    }
                }
            }
        } catch (Exception e) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.MEDIA_ERROR, Alert.AlertType.ERROR);
            System.err.println(Strings.MEDIA_ERROR + e.getMessage());
            disposeMediaPlayer();
        }
    }

    /**
     * Stops the media playback.
     * If an error occurs while stopping, an alert is shown and the media player is disposed.
     */
    @FXML
    private void handleStop() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }
        } catch (Exception e) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.MEDIA_ERROR, Alert.AlertType.ERROR);
            System.err.println(Strings.MEDIA_ERROR + e.getMessage());
            disposeMediaPlayer();
        }
    }

    /**
     * Seeks the media playback backward by a fixed number of seconds.
     * If the current time is less than the skip duration, it seeks to the beginning.
     * Displays an error alert in case of failure.
     */
    @FXML
    private void handleGoBackward() {
        try {
            if (mediaPlayer != null) {
                double currentTime = mediaPlayer.getCurrentTime().toSeconds();
                double seekTime = Math.max(currentTime - defaultSkipTime, 0);
                mediaPlayer.seek(javafx.util.Duration.seconds(seekTime));
            }
        } catch (Exception e) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.MEDIA_ERROR, Alert.AlertType.ERROR);
            System.err.println(Strings.MEDIA_ERROR + e.getMessage());
            disposeMediaPlayer();
        }
    }

    /**
     * Seeks the media playback forward by a fixed number of seconds.
     * If the result exceeds the total duration, it seeks to the end.
     * Displays an error alert in case of failure.
     */
    @FXML
    private void handleGoForward() {
        try {
            if (mediaPlayer != null) {
                double currentTime = mediaPlayer.getCurrentTime().toSeconds();
                double totalDuration = mediaPlayer.getTotalDuration().toSeconds();
                double seekTime = Math.min(currentTime + defaultSkipTime, totalDuration);
                mediaPlayer.seek(javafx.util.Duration.seconds(seekTime));
            }
        } catch (Exception e) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.MEDIA_ERROR, Alert.AlertType.ERROR);
            System.err.println(Strings.MEDIA_ERROR + e.getMessage());
            disposeMediaPlayer();
        }
    }

    /**
     * Toggles the mute state of the media player.
     * If the media player is currently muted, it will be unmuted and vice versa.
     * Also updates the button's icon accordingly.
     */
    @FXML
    private void handleMute() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.setMute(!mediaPlayer.isMute());

                Node node = videoToolBox.getChildren().get(8);
                if (node instanceof Button button && button.getGraphic() instanceof FontIcon icon) {
                    if (mediaPlayer.isMute()) {
                        icon.setIconLiteral("mdi2v-volume-high");
                    } else {
                        icon.setIconLiteral("mdi2v-volume-off");
                    }
                }
            }
        } catch (Exception e) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.MEDIA_ERROR, Alert.AlertType.ERROR);
            System.err.println(Strings.MEDIA_ERROR + e.getMessage());
            disposeMediaPlayer();
        }
    }

    /**
     * Handles the return button click, going back to the previous view.
     */
    @FXML
    private void handleReturn() {
        try {
            if (parentController instanceof AuthenticatedUserDashboardController authController) {
                ViewManager.setMainContent(Frames.MY_RESOURCES_VIEW_PATH, authController.mainContent, parentController);
            }
            else if(parentController instanceof AdminDashboardController adminController){
                ViewManager.setMainContent(Frames.TRACKS_VIEW_PATH_VIEW_PATH, adminController.mainContent, parentController);
            }
            disposeMediaPlayer();
        } catch (Exception e) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
            System.err.println(e.getMessage());
            disposeMediaPlayer();
        }
    }
}