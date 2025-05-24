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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class ResourceFileController extends Controller implements Initializable {
    @FXML private StackPane fileContainer;
    @FXML private HBox videoToolBox;
    @FXML private Label lblTitle;


    private final ResourceConverter resourceConverter;
    private MediaPlayer mediaPlayer;

    //CONSTANTS
    private final int defaultSkipTime = 10;
    private boolean isPlaying = false;
    private Slider sliderProgress;
    private Label lblTimer;
    private Label lblDuration;
    private Stage fullStage = null;


    public ResourceFileController(Resource resource) {
        resourceConverter = new ResourceConverter(resource);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            Node resourceNode = resourceConverter.createMediaNode(fileContainer.getPrefWidth(), fileContainer.getPrefHeight());
            boolean isMultimedia = resourceNode instanceof MediaView;

            if (!isMultimedia) {
                int defaultGapTitle = 10;
                lblTitle.setLayoutY(lblTitle.getLayoutY() + defaultGapTitle);
                int defaultGapContainerToolBox = 30;
                fileContainer.setLayoutY(fileContainer.getLayoutY() + defaultGapContainerToolBox);
                fileContainer.getChildren().add(resourceNode);
                videoToolBox.setVisible(false);
                return;
            }

            setUpMediaPlayer(resourceNode);

        } catch (TrackTuneException ex) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, ex.getMessage(), Alert.AlertType.ERROR);
            disposeMediaPlayer();
            handleReturn();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            disposeMediaPlayer();
            handleReturn();
        }
    }

    private void setUpMediaPlayer(Node resourceNode) {
        mediaPlayer = ((MediaView) resourceNode).getMediaPlayer();

        initSlider();
        initLabels();

        setupSliderListeners();


        HBox videoControls = new HBox(10, lblTimer, sliderProgress, lblDuration);
        videoControls.setAlignment(Pos.CENTER);
        videoControls.setPadding(new Insets(10));
        applyStyles();

        VBox videoLayout = new VBox(resourceNode, videoControls);
        videoLayout.setAlignment(Pos.CENTER);

        fileContainer.getChildren().add(videoLayout);

        mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> updateTimeLabels(newTime));
        mediaPlayer.setOnReady(() -> sliderProgress.setDisable(false));

        videoToolBox.setVisible(true);
        handlePlayPause();
    }

    private void initSlider() {
        sliderProgress = new Slider(0, 100, 0);
        sliderProgress.setPrefWidth(fileContainer.getPrefWidth());
        sliderProgress.setDisable(true);
        HBox.setHgrow(sliderProgress, Priority.ALWAYS);
    }

    private void initLabels() {
        lblTimer = new Label("00:00");
        lblDuration = new Label("00:00");
        lblTimer.setMinWidth(Label.USE_PREF_SIZE);
        lblDuration.setMinWidth(Label.USE_PREF_SIZE);
    }

    private void applyStyles() {
        sliderProgress.getStyleClass().add("media-slider");
        lblTimer.getStyleClass().add("media-time-label");
        lblDuration.getStyleClass().add("media-time-label");
    }

    private void setupSliderListeners() {
        sliderProgress.valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
            if (!isChanging) {
                seekToSliderPosition();
            }
        });

        sliderProgress.valueProperty().addListener((obs, oldVal, newVal) -> updateSliderStyle(newVal.doubleValue()));
    }

    private void updateTimeLabels(javafx.util.Duration newTime) {
        lblTimer.setText(formatDuration(newTime));
        javafx.util.Duration total = mediaPlayer.getTotalDuration();
        javafx.util.Duration remaining = total.subtract(newTime);
        lblDuration.setText("-" + formatDuration(remaining));

        if (!sliderProgress.isValueChanging() && total.toSeconds() > 0) {
            sliderProgress.setValue((newTime.toSeconds() / total.toSeconds()) * 100);
        }
    }

    private void seekToSliderPosition() {
        double total = mediaPlayer.getTotalDuration().toSeconds();
        if (total > 0) {
            double seekTime = (sliderProgress.getValue() / 100) * total;
            mediaPlayer.seek(javafx.util.Duration.seconds(seekTime));
        }
    }

    private void updateSliderStyle(double progress) {
        String css = String.format(Locale.US,
                "-fx-background-color: linear-gradient(to right, " +
                        "#ff8c00 0%%, #ff8c00 %.2f%%, " +
                        "rgba(255,140,0,0.3) %.2f%%, rgba(255,140,0,0.3) 100%%);",
                progress, progress
        );
        Node track = sliderProgress.lookup(".track");
        if (track != null) {
            track.setStyle(css);
        }
    }


    private String formatDuration(javafx.util.Duration duration) {
        int minutes = (int) duration.toMinutes();
        int seconds = (int) (duration.toSeconds() % 60);
        return String.format("%02d:%02d", minutes, seconds);
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

    @FXML
    void handleFullView() {
        if (mediaPlayer == null) return;

        try {
            MediaView fullMediaView = new MediaView(mediaPlayer);
            fullMediaView.setPreserveRatio(true);
            fullMediaView.setFitWidth(Screen.getPrimary().getBounds().getWidth());
            fullMediaView.setFitHeight(Screen.getPrimary().getBounds().getHeight());

            VBox layout = new VBox(fullMediaView);
            layout.setStyle("-fx-background-color: black");
            VBox.setVgrow(fullMediaView, Priority.ALWAYS);

            Scene fullScene = new Scene(layout);
            fullStage = new Stage();
            fullStage.setScene(fullScene);
            fullStage.setFullScreen(true);
            fullStage.initOwner(fileContainer.getScene().getWindow());

            fullScene.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    fullStage.close();
                }
            });

            fullStage.setOnCloseRequest(event -> {
                fullStage = null;
            });

            fullStage.show();

        } catch (Exception e) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.MEDIA_ERROR, Alert.AlertType.ERROR);
            System.err.println(Strings.MEDIA_ERROR + e.getMessage());
        }
    }
}


