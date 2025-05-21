package app.tracktune.controller.authenticatedUser;

import app.tracktune.controller.Controller;
import app.tracktune.model.resource.Resource;
import app.tracktune.utils.Frames;
import app.tracktune.utils.Strings;
import app.tracktune.view.ViewManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class ResourceFileController extends Controller implements Initializable {
    @FXML private StackPane fileContainer;
    private final Resource resource;
    private MediaPlayer mediaPlayer;

    public ResourceFileController(Resource resource) {
        this.resource = resource;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (resource != null) {
            Node resourceView = createPreview(resource, fileContainer.getPrefWidth(), fileContainer.getPrefHeight());
            fileContainer.getChildren().add(resourceView);

            if (resourceView instanceof ImageView) {
                ((ImageView) resourceView).setFitWidth(fileContainer.getPrefWidth());
                ((ImageView) resourceView).setFitHeight(fileContainer.getPrefHeight());
                ((ImageView) resourceView).setPreserveRatio(true);
            }

            //TODO - Make param byte[] to load video from db
            //initMediaPlayer("C:\\Users\\ACER\\Videos\\test3.mp4");
        }
    }

    /**
     * Initializes the media player with a video file located at a specific path.
     * The video will be displayed inside the fileContainer.
     * If the video cannot be loaded or played, an error alert is shown.
     */
    private void initMediaPlayer(String videoPath) {
        try {
            Media media = new Media(new File(videoPath).toURI().toString());

            mediaPlayer = new MediaPlayer(media);

            MediaView mediaPlayerView = new MediaView(mediaPlayer);
            mediaPlayerView.setPreserveRatio(true);

            mediaPlayerView.fitWidthProperty().bind(fileContainer.widthProperty());
            mediaPlayerView.fitHeightProperty().bind(fileContainer.heightProperty());

            fileContainer.getChildren().add(mediaPlayerView);

            mediaPlayer.play();
        } catch (Exception e) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.MEDIA_ERROR, Alert.AlertType.ERROR);
            System.err.println(Strings.MEDIA_ERROR + e.getMessage());
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
     * Handles the return button click, going back to the previous view.
     */
    @FXML
    private void handleReturn() {
        try {
            if (parentController instanceof AuthenticatedUserDashboardController authController) {
                ViewManager.setMainContent(Frames.RESOURCES_VIEW_PATH, authController.mainContent, parentController);
            }
        } catch (Exception e) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
            System.err.println(e.getMessage());
        }
    }
}