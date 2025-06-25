package app.tracktune.controller.common;

import app.tracktune.Main;
import app.tracktune.controller.Controller;
import app.tracktune.controller.admin.AdminDashboardController;
import app.tracktune.controller.authenticatedUser.AuthenticatedUserDashboardController;
import app.tracktune.utils.SessionManager;
import app.tracktune.exceptions.TrackTuneException;
import app.tracktune.utils.DatabaseManager;
import app.tracktune.model.author.Author;
import app.tracktune.model.comments.Comment;
import app.tracktune.model.genre.Genre;
import app.tracktune.model.musicalInstrument.MusicalInstrument;
import app.tracktune.model.resource.MultimediaResource;
import app.tracktune.model.resource.Resource;
import app.tracktune.model.resource.ResourceTypeEnum;
import app.tracktune.model.track.Track;
import app.tracktune.model.user.Administrator;
import app.tracktune.model.user.User;
import app.tracktune.utils.Frames;
import app.tracktune.utils.ResourceManager;
import app.tracktune.utils.Strings;
import app.tracktune.view.ViewManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

public class ResourceFileController extends Controller implements Initializable {
    /** Container pane for displaying the media file (audio/video). */
    @FXML private StackPane fileContainer;

    /** Toolbox containing video controls (play, pause, etc.). */
    @FXML private HBox videoToolBox;

    /** Label showing the title of the current media/resource. */
    @FXML private Label lblTitle;

    /** Box containing metadata information related to the media/resource. */
    @FXML private VBox metadataBox;

    /** Text field for entering user comments on the resource. */
    @FXML private TextField commentField;

    /** VBox holding all comments or related UI elements. */
    @FXML private VBox commentVBox;

    /** Button to start or manage video/audio segment actions. */
    @FXML private Button segmentButton;

    /** Manages resource-related operations and media creation. */
    private final ResourceManager resourceManager;

    /** JavaFX media player for controlling audio/video playback. */
    private MediaPlayer mediaPlayer;

    /** The current track associated with the media being played. */
    private Track track;

    /** Default time in seconds to skip forward/backward during playback. */
    private final int defaultSkipTime = 10;

    /** Flag indicating whether the media is currently playing. */
    private boolean isPlaying = false;

    /** Slider UI control for showing and adjusting media playback progress. */
    private Slider sliderProgress;

    /** Label displaying the current playback time (elapsed). */
    private Label lblTimer;

    /** Label displaying the total duration of the media. */
    private Label lblDuration;

    /** Stage used for full-screen playback mode (optional). */
    private Stage fullStage = null;

    /**
     * Constructs a {@code ResourceFileController} and initializes the {@link ResourceManager}
     * with the provided resource.
     *
     * @param resource the resource to be managed and displayed by this controller
     */
    public ResourceFileController(Resource resource) {
        resourceManager = ResourceManager.getInstance();
        resourceManager.setResource(resource);
    }

    /**
     * Initializes the controller and sets up the media or resource display based on the provided resource type.
     * <p>
     * This method configures the UI layout and event handling for the resource view. If the resource is a multimedia file
     * (such as video or audio), the media player is set up and a timer is started to track media readiness.
     * Non-multimedia resources (e.g., images or PDFs) are displayed without media controls.
     * <p>
     * Additionally:
     * <ul>
     *     <li>Sets the window close event to properly dispose of the media player.</li>
     *     <li>Displays resource metadata and enables specific UI components based on resource type (e.g., segment button for PDFs and videos).</li>
     *     <li>Handles exceptions by showing an error alert and safely returning to the previous view.</li>
     * </ul>
     *
     * @param location  the location used to resolve relative paths for the root object (unused in this method)
     * @param resources the resources used to localize the root object (unused in this method)
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            Platform.runLater(() -> Main.root.setOnCloseRequest(_ -> disposeMediaPlayer()));
            Node resourceNode = resourceManager.createMediaNode(fileContainer.getPrefWidth(), fileContainer.getPrefHeight(), false);
            boolean isMultimedia = resourceNode instanceof MediaView;

            if (!isMultimedia) {
                int defaultGapTitle = 10;
                int defaultGapContainerToolBox = 30;

                lblTitle.setLayoutY(lblTitle.getLayoutY() + defaultGapTitle);
                fileContainer.setLayoutY(fileContainer.getLayoutY() + defaultGapContainerToolBox);
                fileContainer.getChildren().add(resourceNode);
                videoToolBox.setVisible(false);
                metadataBox.getChildren().add(setDetailsInfo());
                metadataBox.setAlignment(Pos.CENTER);

                if(resourceManager.getResource().getType().equals(ResourceTypeEnum.pdf))
                    segmentButton.setVisible(true);
            }
            else{
                segmentButton.setVisible(true);
                setupMediaPlayer(resourceNode);
                metadataBox.getChildren().add(setDetailsInfo());
            }
            if(resourceManager.getResource().getType() == ResourceTypeEnum.mp4)
                startTimer(fileContainer, List.of(resourceManager.getResource()), resourceManager);
            setComments();
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

    /**
     * Configures and initializes the media player UI components for the provided media node.
     * <p>
     * This method performs the following actions:
     * <ul>
     *     <li>Extracts the {@link MediaPlayer} from the given {@link MediaView} node.</li>
     *     <li>Initializes the progress slider, time labels, and their event listeners.</li>
     *     <li>Creates and styles the video or audio control layout based on the resource type.</li>
     *     <li>Updates the UI to display the media player and enables media controls.</li>
     *     <li>Attaches a listener to update time labels as the media plays.</li>
     * </ul>
     *
     * @param resourceNode the {@link Node} containing the media player to be configured
     */
    public void setupMediaPlayer(Node resourceNode) {
        mediaPlayer = ((MediaView) resourceNode).getMediaPlayer();

        initSlider();
        initLabels();
        setupSliderListeners();

        HBox videoControls = new HBox(10, lblTimer, sliderProgress, lblDuration);
        videoControls.setAlignment(Pos.CENTER);
        videoControls.setPadding(new Insets(10));
        applyStyles();

        VBox videoLayout = new VBox();
        if (resourceManager.getResource().getType().equals(ResourceTypeEnum.mp3)) {
            Label label = new Label(Strings.AUDIO_FILE);
            label.setWrapText(true);
            label.getStyleClass().add("audio-label");

            VBox audioLayout = new VBox(10, label, videoControls);
            audioLayout.setAlignment(Pos.CENTER);
            audioLayout.setPadding(new Insets(10));
            videoLayout.getChildren().add(audioLayout);
            fileContainer.setStyle("-fx-background-color: #000000");
            lblTimer.setStyle("-fx-text-fill: #fae3b4");
            lblDuration.setStyle("-fx-text-fill: #fae3b4");
        }
        else{
            videoLayout = new VBox(resourceNode, videoControls);
        }
        videoLayout.setAlignment(Pos.CENTER);

        fileContainer.getChildren().add(videoLayout);

        mediaPlayer.currentTimeProperty().addListener((_, _, newTime) -> updateTimeLabels(newTime));
        mediaPlayer.setOnReady(() -> sliderProgress.setDisable(false));

        videoToolBox.setVisible(true);
        handlePlayPause();
    }

    /**
     * Loads and displays all comments associated with the current resource.
     * <p>
     * For each comment retrieved from the database, the corresponding user is also fetched.
     * If the user exists, the comment is added to the user interface.
     * </p>
     */
    private void setComments() {
        if (resourceManager.getResource() != null) {
            for (Comment comment : DatabaseManager.getDAOProvider().getCommentDAO().getAllCommentByResource(resourceManager.getResource().getId())) {
                User user = DatabaseManager.getDAOProvider().getUserDAO().getById(comment.getUserID());
                if(user != null)
                    addCommentOnView(comment, user);
            }
        }
    }

    /**
     * Creates and returns a {@link VBox} containing detailed metadata information about the current resource.
     * <p>
     * The displayed details include:
     * <ul>
     *     <li>Track title</li>
     *     <li>Authors associated with the track</li>
     *     <li>Genres linked to the track</li>
     *     <li>Musical instruments involved</li>
     *     <li>File format and size</li>
     *     <li>For multimedia resources, duration and registration date (added once media is ready)</li>
     *     <li>Name of the user who uploaded the resource</li>
     * </ul>
     * </p>
     *
     * @return a {@code VBox} node containing the metadata rows to be displayed in the UI
     */
    private VBox setDetailsInfo() {
        VBox box = new VBox();
        box.setAlignment(Pos.CENTER_LEFT);
        box.setSpacing(5);

        track = DatabaseManager.getDAOProvider().getTrackDAO().getTrackByResourceId(resourceManager.getResource().getId());
        String title = track.getTitle();
        box.getChildren().add(createMetadataRow(Strings.TRACKS, title));

        String authors = DatabaseManager.getDAOProvider().getAuthorDAO().getAllAuthorsByTrackId(track.getId()).stream()
                .map(Author::getAuthorshipName)
                .collect(Collectors.joining(", "));
        box.getChildren().add(createMetadataRow(Strings.AUTHORS, authors));

        String genres = DatabaseManager.getDAOProvider().getGenreDAO().getAllGenresByTrackId(track.getId()).stream()
                .map(Genre::getName)
                .collect(Collectors.joining(", "));
        box.getChildren().add(createMetadataRow(Strings.GENRES, genres));

        String instruments = DatabaseManager.getDAOProvider().getMusicalInstrumentDAO().getAllInstrumentByTrackId(track.getId()).stream()
                .map(MusicalInstrument::getName)
                .collect(Collectors.joining(", "));
        box.getChildren().add(createMetadataRow(Strings.INSTRUMENTS, instruments));

        box.getChildren().add(createMetadataRow(Strings.FILE_FORMAT, resourceManager.getResource().getType().toString()));
        box.getChildren().add(createMetadataRow(Strings.RESOURCE_SIZE, humanReadableByteCount(resourceManager.getResource().getData().length)));

        if(resourceManager.getResource() instanceof MultimediaResource multimediaResource) {
            mediaPlayer.setOnReady(() -> {
                metadataBox.getChildren().add(createMetadataRow(Strings.DURATION, formatDuration(mediaPlayer.getTotalDuration())));
                metadataBox.getChildren().add(createMetadataRow(Strings.REGISTERED_DATA, multimediaResource.getResourceDate().toString()));
            });
        }

        User user = DatabaseManager.getDAOProvider().getUserDAO().getById(resourceManager.getResource().getUserID());
        box.getChildren().add(createMetadataRow(Strings.UPLOADED, user.getName() + " " + user.getSurname()));

        return box;
    }

    /**
     * Creates an {@link HBox} representing a single row of metadata with a title and its corresponding value.
     * <p>
     * The row contains two labels:
     * <ul>
     *     <li>A title label styled with "metadata-label"</li>
     *     <li>A value label styled with "metadata-value". If the value is null, displays "N/A"</li>
     * </ul>
     * The row is aligned to the left with a spacing of 5 pixels between the labels.
     * </p>
     *
     * @param title the title or name of the metadata field
     * @param value the value associated with the metadata field; may be null
     * @return an {@code HBox} containing the formatted metadata title and value labels
     */
    private HBox createMetadataRow(String title, String value) {
        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("metadata-label");
        Label lblValue = new Label(value != null ? value : "N/A");
        lblValue.getStyleClass().add("metadata-value");

        HBox row = new HBox(5, lblTitle, lblValue);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    /**
     * Converts a byte count into a human-readable string using binary prefixes (KB, MB, GB, etc.).
     * <p>
     * For example, 1536 bytes will be converted to "1.5 KB".
     * If the byte count is less than 1024, it returns the value in bytes with "B" suffix.
     * </p>
     *
     * @param bytes the number of bytes to convert
     * @return a human-readable string representation of the byte count with appropriate unit
     */
    private String humanReadableByteCount(long bytes) {
        int unit = 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = ("KMGTPE").charAt(exp-1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    /**
     * Initializes the progress slider used to display and control media playback progress.
     * <p>
     * The slider is created with a range from 0 to 100 and initial value 0.
     * Its preferred width is set to match the width of the file container.
     * The slider is initially disabled and set to always grow horizontally within its container.
     * </p>
     */
    private void initSlider() {
        sliderProgress = new Slider(0, 100, 0);
        sliderProgress.setPrefWidth(fileContainer.getPrefWidth());
        sliderProgress.setDisable(true);
        HBox.setHgrow(sliderProgress, Priority.ALWAYS);
    }

    /**
     * Initializes the timer labels used to display the current playback time and total duration.
     * <p>
     * Both labels are initialized with "00:00" and their minimum widths are set to their preferred sizes
     * to ensure proper layout.
     * </p>
     */
    private void initLabels() {
        lblTimer = new Label("00:00");
        lblDuration = new Label("00:00");
        lblTimer.setMinWidth(Label.USE_PREF_SIZE);
        lblDuration.setMinWidth(Label.USE_PREF_SIZE);
    }

    /**
     * Applies CSS style classes to the media controls.
     * <p>
     * Adds "media-slider" style class to the slider,
     * and "media-time-label" style class to the timer and duration labels.
     * </p>
     */
    private void applyStyles() {
        sliderProgress.getStyleClass().add("media-slider");
        lblTimer.getStyleClass().add("media-time-label");
        lblDuration.getStyleClass().add("media-time-label");
    }

    /**
     * Sets up listeners for the slider to handle user interaction.
     * <p>
     * - When the slider's value is no longer changing (dragging finished), it triggers seeking to the new position.
     * - When the slider's value changes, it updates the slider's visual style accordingly.
     * </p>
     */
    private void setupSliderListeners() {
        sliderProgress.valueChangingProperty().addListener((_, _, isChanging) -> {
            if (!isChanging) {
                seekToSliderPosition();
            }
        });

        sliderProgress.valueProperty().addListener((_, _, newVal) -> updateSliderStyle(newVal.doubleValue()));
    }

    /**
     * Updates the playback time labels and slider progress based on the current media time.
     * <p>
     * Sets the elapsed time label and remaining time label (with a preceding "-").
     * Updates the slider's value to reflect the playback progress unless the slider is currently being dragged.
     * </p>
     *
     * @param newTime the current playback time of the media
     */
    private void updateTimeLabels(javafx.util.Duration newTime) {
        lblTimer.setText(formatDuration(newTime));
        javafx.util.Duration total = mediaPlayer.getTotalDuration();
        javafx.util.Duration remaining = total.subtract(newTime);
        lblDuration.setText("-" + formatDuration(remaining));

        if (!sliderProgress.isValueChanging() && total.toSeconds() > 0) {
            sliderProgress.setValue((newTime.toSeconds() / total.toSeconds()) * 100);
        }
    }

    /**
     * Seeks the media player to the position corresponding to the current slider value.
     * <p>
     * Calculates the seek time as a percentage of the total media duration.
     * </p>
     */
    private void seekToSliderPosition() {
        double total = mediaPlayer.getTotalDuration().toSeconds();
        if (total > 0) {
            double seekTime = (sliderProgress.getValue() / 100) * total;
            mediaPlayer.seek(javafx.util.Duration.seconds(seekTime));
        }
    }

    /**
     * Seeks the media player to a specific time and updates the slider position accordingly.
     * <p>
     * If the media player and time are valid, the player is seeked and slider value is updated to reflect the new position.
     * </p>
     *
     * @param time the target playback time to seek to
     */
    private void seekTo(Duration time) {
        if (mediaPlayer != null && time != null) {
            mediaPlayer.seek(time);
            double total = mediaPlayer.getTotalDuration().toSeconds();
            if (total > 0) {
                double percent = (time.toSeconds() / total) * 100;
                sliderProgress.setValue(percent);
            }
        }
    }

    /**
     * Updates the visual style of the slider track to indicate playback progress.
     * <p>
     * Uses a linear gradient background to fill the slider track proportionally to the given progress percentage.
     * </p>
     *
     * @param progress the playback progress percentage (0.0 - 100.0)
     */
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

    /**
     * Formats a JavaFX Duration into a "mm:ss" string.
     *
     * @param duration the duration to format
     * @return a string formatted as "minutes:seconds" with zero padding
     */
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
            MediaPlayer.Status status = mediaPlayer.getStatus();
            if (status != MediaPlayer.Status.UNKNOWN && status != MediaPlayer.Status.DISPOSED) {
                mediaPlayer.stop();
            }
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
    }

    /**
     * Starts or resumes the media playback.
     * If an error occurs during playback, an alert is shown and the media player is disposed.
     */
    @FXML
    public void handlePlayPause() {
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
                Node node = videoToolBox.getChildren().getFirst();

                if (node instanceof Button button && button.getGraphic() instanceof FontIcon icon) {
                    icon.setIconLiteral("mdi2p-play");
                    isPlaying = false;
                }
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
     * If the media player is currently muted, it will be disabled and vice versa.
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
            else if(parentController instanceof DiscoverController discoverController){
                if(discoverController.parentController instanceof AuthenticatedUserDashboardController authController)
                    ViewManager.setMainContent(Frames.DISCOVER_VIEW_PATH, authController.mainContent, authController);
                else if(discoverController.parentController instanceof AdminDashboardController adminController){
                    ViewManager.setMainContent(Frames.DISCOVER_VIEW_PATH, adminController.mainContent, adminController);
                }
            }
            disposeMediaPlayer();
        } catch (Exception e) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
            System.err.println(e.getMessage());
            disposeMediaPlayer();
        }
    }

    /**
     * Handles the action to display the media player in a full-screen view.
     * <p>
     * Creates a new {@link Stage} with a {@link MediaView} that uses the current {@link MediaPlayer}.
     * The media view is resized to fit the primary screen while preserving its aspect ratio.
     * The full-screen stage closes when the ESC key is pressed.
     * </p>
     * <p>
     * If the media player is not initialized, the method returns without action.
     * In case of exceptions, an error alert is shown and the error message is logged.
     * </p>
     */
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

            fullStage.setOnCloseRequest(_ -> fullStage = null);

            fullStage.show();

        } catch (Exception e) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.MEDIA_ERROR, Alert.AlertType.ERROR);
            System.err.println(Strings.MEDIA_ERROR + e.getMessage());
        }
    }

    /**
     * Handles the action of sending a new comment.
     * <p>
     * Retrieves the text from the comment input field and, if it is not empty,
     * creates a new {@link Comment} object with the current timestamp, current user ID,
     * and the current resource ID. The comment is then inserted into the database.
     * After insertion, a new {@link Comment} with the generated ID is created and added to the view.
     * Finally, the comment input field is cleared.
     * </p>
     */
    @FXML
    private void handleSendComment() {
        String commentText = commentField.getText();
        Comment c;
        if (commentText != null && !commentText.trim().isEmpty()) {
            c = new Comment(commentText, new Timestamp(System.currentTimeMillis()), ViewManager.getSessionUser().getId(), resourceManager.getResource().getId());
            int id = DatabaseManager.getDAOProvider().getCommentDAO().insert(c);
            c = new Comment(id, c.getDescription(), c.getStartTrackInterval(), c.getEndTrackInterval(), c.getCreationDate(), c.getUserID(), resourceManager.getResource().getId());
            addCommentOnView(c, ViewManager.getSessionUser());
            commentField.clear();
        }
    }

    /**
     * Adds a comment node representing the given comment and user to the comments container in the UI.
     *
     * @param comment the {@link Comment} to be displayed
     * @param user the {@link User} who posted the comment
     */
    private void addCommentOnView(Comment comment, User user) {
        VBox commentNode = createCommentNode(comment, user, 0);
        commentVBox.getChildren().add(commentNode);
    }

    /**
     * Creates a graphical node (VBox) representing a comment with all its visual and functional components,
     * including the author, comment text, creation date, user role, time intervals, reply and delete buttons,
     * and nested display of any replies.
     * The created node includes:
     * <ul>
     *   <li>The full name of the comment's author.</li>
     *   <li>A text area with the comment's content.</li>
     *   <li>Labels showing the creation date and the user role (Administrator, Author/Interpreter, Regular User).</li>
     *   <li>Buttons to reply to the comment and, if authorized, to delete it.</li>
     *   <li>Clickable time interval labels for media navigation, if applicable.</li>
     *   <li>A nested section to show replies to the comment, with expand/collapse functionality.</li>
     * </ul>
     *
     * The method also handles the logic for adding new replies, recursively loading existing replies from the database,
     * and event handling for user interactions with the buttons.
     *
     * @param comment the comment object to be represented in the node
     * @param user the user who authored the comment, used to display name and role
     * @param indentLevel the indentation level for the node, used to format nested replies visually
     * @return a VBox containing the complete structure of the comment including any nested replies
     */
    private VBox createCommentNode(Comment comment, User user, int indentLevel) {
        Label nameLabel = new Label(user.getName() + " " + user.getSurname());
        nameLabel.setWrapText(true);
        nameLabel.getStyleClass().add("comment-author");

        FontIcon replyIcon = new FontIcon("mdi2r-reply");
        replyIcon.setIconSize(18);
        Button replyButton = new Button();
        replyButton.setGraphic(replyIcon);
        replyButton.getStyleClass().add("comment-icon-button");

        FontIcon deleteIcon = new FontIcon("mdi2d-delete");
        deleteIcon.setIconSize(18);
        Button deleteButton = new Button();
        deleteButton.setGraphic(deleteIcon);
        deleteButton.getStyleClass().add("delete-comment");

        HBox buttonsBox = (ViewManager.getSessionUser() instanceof Administrator || resourceManager.getResource().getUserID() == ViewManager.getSessionUser().getId() || Objects.equals(comment.getUserID(), ViewManager.getSessionUser().getId()))
                ? new HBox(5, replyButton, deleteButton)
                : new HBox(5, replyButton);
        buttonsBox.setAlignment(Pos.CENTER_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBox = new HBox(nameLabel, spacer, buttonsBox);
        topBox.setAlignment(Pos.CENTER_LEFT);
        topBox.setSpacing(5);

        Label commentLabel = new Label(comment.getDescription());
        commentLabel.setWrapText(true);
        commentLabel.setPadding(new Insets(10, 0, 10, 0));
        commentLabel.getStyleClass().add("comment-text");

        Label dateLabel = new Label(getFormattedRequestDate(comment.getCreationDate()));
        FontIcon clockIcon = new FontIcon("mdi2c-clock-outline");
        clockIcon.setIconSize(14);
        dateLabel.setGraphic(clockIcon);
        dateLabel.getStyleClass().add("comment-date");

        VBox repliesBox = new VBox();
        repliesBox.setPadding(new Insets(10, 0, 0, 0));
        repliesBox.setVisible(false);
        repliesBox.setManaged(false);
        repliesBox.setVisible(false);
        repliesBox.setManaged(false);

        Label repliesLabel = new Label(Strings.REPLIES);
        repliesLabel.setVisible(false);
        repliesLabel.getStyleClass().add("replies-toggle-closed");
        repliesLabel.setCursor(Cursor.HAND);
        repliesLabel.setOnMouseClicked(_ -> {
            boolean isVisible = repliesBox.isVisible();
            repliesBox.setVisible(!isVisible);
            repliesBox.setManaged(!isVisible);
            repliesLabel.setText(isVisible ? Strings.REPLIES : Strings.HIDE_REPLIES);
            repliesLabel.getStyleClass().removeAll("replies-toggle-open", "replies-toggle-closed");
            repliesLabel.getStyleClass().add(isVisible ? "replies-toggle-closed" : "replies-toggle-open");
        });

        Region spacer3 = new Region();
        HBox.setHgrow(spacer3, Priority.ALWAYS);
        HBox replies_date = new HBox(repliesLabel, spacer3, dateLabel);
        VBox commentBox = new VBox();
        commentBox.setSpacing(5);
        commentBox.setPadding(new Insets(10));

        Label roleLB;

        if(user instanceof Administrator){
            commentBox.getStyleClass().add("comment-box-admin");
            roleLB = new Label(Strings.ADMIN);
            roleLB.getStyleClass().add("comment-author-admin");

        }
        else if(resourceManager.getResource().isAuthor()){
            commentBox.getStyleClass().add("comment-box-author-interpreter");
            roleLB = new Label(Strings.AUTHOR_INTERPRETER);
            roleLB.getStyleClass().add("comment-author-interpreter");
        }
        else {
            commentBox.getStyleClass().add("comment-box");
            roleLB = new Label(Strings.USER);
            roleLB.getStyleClass().add("comment-author-user");
        }

        commentBox.setMaxWidth(280);
        HBox role =  new HBox(roleLB);
        role.setAlignment(Pos.CENTER);
        if(comment.getStartTrackInterval() != 0 && comment.getEndTrackInterval() != comment.getStartTrackInterval()){
            Label start = new Label(formatDuration(new Duration(comment.getStartTrackInterval()*1000)));
            Label dash = new Label("- ");
            Label end = new Label(formatDuration(new Duration(comment.getEndTrackInterval()*1000)));
            HBox intervals = new HBox();

            start.setOnMouseClicked(_ -> seekTo(new Duration(comment.getStartTrackInterval() * 1000)));
            if(comment.getEndTrackInterval() != 0){
                intervals.getChildren().addAll(start, dash, end);
                end.setOnMouseClicked(_ -> seekTo(new Duration(comment.getEndTrackInterval() * 1000)));
            }
            else{
                intervals.getChildren().addAll(start);
            }
            intervals.setAlignment(Pos.CENTER);
            start.getStyleClass().add("segment-text");
            dash.getStyleClass().add("comment-text");
            end.getStyleClass().add("segment-text");
            commentBox.getChildren().addAll(role, topBox, commentLabel, replies_date, intervals);
        }
        else
            commentBox.getChildren().addAll(role, topBox, commentLabel, replies_date);
        VBox indentedBox = new VBox(commentBox, repliesBox);
        indentedBox.setPadding(new Insets(0, 0, 0, indentLevel == 0 ? 0 : 15));

        VBox container = new VBox(indentedBox);
        container.setPadding(new Insets(5, 0, 5, 0));

        replyButton.setOnAction(_ -> {
            Optional<String> result = ViewManager.showReplyDialog();
            result.ifPresent(responseText -> {
                if(responseText.trim().isEmpty()){
                    ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERR_REPLY, Strings.FIELD_EMPTY, Alert.AlertType.ERROR);
                }
                else{
                    Timestamp now = new Timestamp(System.currentTimeMillis());
                    Comment reply = new Comment(
                            responseText,
                            0,
                            0,
                            now,
                            ViewManager.getSessionUser().getId(),
                            resourceManager.getResource().getId()
                    );

                    int replyID = DatabaseManager.getDAOProvider().getCommentDAO().insert(reply);
                    DatabaseManager.getDAOProvider().getCommentDAO().insertReply(comment.getID(), replyID);

                    Comment newReply = new Comment(
                            replyID,
                            reply.getDescription(),
                            reply.getStartTrackInterval(),
                            reply.getEndTrackInterval(),
                            reply.getCreationDate(),
                            reply.getUserID(),
                            resourceManager.getResource().getId()
                    );

                    VBox replyNode = createCommentNode(
                            newReply,
                            SessionManager.getInstance().getUser(),
                            1
                    );

                    repliesBox.getChildren().add(replyNode);
                    repliesBox.setVisible(true);
                    repliesBox.setManaged(true);
                    repliesLabel.setText(Strings.HIDE_REPLIES);
                    repliesLabel.getStyleClass().removeAll("replies-toggle-closed");
                    repliesLabel.getStyleClass().add("replies-toggle-open");
                }
            });
        });

        List<Comment> replies = DatabaseManager.getDAOProvider().getCommentDAO().getAllReplies(comment.getID());
        if (replies != null && !replies.isEmpty()) {
            repliesLabel.setVisible(true);
            for (Comment reply : replies) {
                VBox replyNode = createCommentNode(
                        reply,
                        getUser(reply.getUserID()),
                        1
                );
                repliesBox.getChildren().add(replyNode);
            }
        }

        deleteButton.setOnAction(_ -> {
            boolean response = ViewManager.setAndGetConfirmAlert(Strings.CONFIRM_DELETION, Strings.CONFIRM_DELETION, Strings.ARE_YOU_SURE);
            if(response){
                ((VBox) container.getParent()).getChildren().remove(container);
                DatabaseManager.getDAOProvider().getCommentDAO().deleteById(comment.getID());
            }
        });

        return container;
    }

    /**
     * Retrieves a User object from the database based on the given user ID.
     *
     * @param userId the unique identifier of the user to fetch
     * @return the User object corresponding to the given ID, or null if not found
     */
    private User getUser(int userId) {
        return DatabaseManager.getDAOProvider().getUserDAO().getById(userId);
    }

    /**
     * Opens a dialog to add a time-segment-specific comment on the media track.
     * The user inputs a start time, an optional end time, and a comment description.
     * This method validates the input times against the media duration, creates a Comment,
     * saves it to the database, and updates the view with the new comment.
     * If the time format is invalid or the times are logically inconsistent (e.g., start time after end time,
     * times outside media duration), an error alert is shown.
     */
    @FXML
    private void addSegmentComment() {
        ViewManager.showSegmentCommentDialog().ifPresent(data -> {
            String start = data[0];
            String end = data[1];
            String comment = data[2];

//            if(resourceManager.resource.getType().equals(ResourceTypeEnum.pdf)){
//                // FOR FUTURE IMPLEMENTATIONS - COMMENT INTO PDF FILES
//            }
//            else {
                try {
                    int startInt = parseToSeconds(start);
                    int endInt = parseToSeconds(end);
                    Duration startDuration = new Duration(startInt * 1000);
                    Duration mediaDuration = mediaPlayer.getTotalDuration();
                    Duration endDuration;

                    if(!end.equals("0")){
                        if (startDuration.greaterThan(mediaDuration) || startDuration.lessThan(Duration.ZERO))
                            throw new TrackTuneException(Strings.ERROR_START_TIME_GREATER_DURATION);

                        endDuration = new Duration(endInt * 1000);
                        if (endDuration.greaterThan(mediaDuration) || endDuration.lessThanOrEqualTo(Duration.ZERO))
                            throw new TrackTuneException(Strings.ERROR_END_TIME_GREATER_DURATION);

                        if (endDuration.lessThan(startDuration))
                            throw new TrackTuneException(Strings.ERROR_START_TIME_GREATER_END_TIME);
                    }
                    else{
                        if (startDuration.greaterThan(mediaDuration) || startDuration.lessThan(Duration.ZERO))
                            throw new TrackTuneException(Strings.ERROR_START_TIME_GREATER_DURATION);
                    }

                    Comment c = new Comment(
                            comment,
                            startInt,
                            endInt,
                            Timestamp.from(Instant.now()),
                            SessionManager.getInstance().getUser().getId(),
                            resourceManager.getResource().getId()
                    );

                    int id = DatabaseManager.getDAOProvider().getCommentDAO().insert(c);
                    c = new Comment(id, c.getDescription(), c.getStartTrackInterval(), c.getEndTrackInterval(), c.getCreationDate(), c.getUserID(), resourceManager.getResource().getId());
                    addCommentOnView(c, ViewManager.getSessionUser());
                } catch (DateTimeParseException ex) {
                    ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.INVALID_TIME_FORMAT + ex.getParsedString(), Alert.AlertType.ERROR);
                }
                catch (TrackTuneException e){
                    ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, e.getMessage(), Alert.AlertType.ERROR);
                }
            //}
        });
    }

    /**
     * Parses a time string formatted as "HH:mm:ss", "mm:ss", or "ss" into total seconds.
     *
     * @param timeString the time string to parse
     * @return the total number of seconds represented by the time string
     * @throws DateTimeParseException if the time string format is invalid or contains non-numeric values
     */
    private int parseToSeconds(String timeString) throws DateTimeParseException {
        String[] parts = timeString.split(":");

        try {
            int hours = 0;
            int minutes = 0;
            int seconds;

            if (parts.length == 3) {
                hours = Integer.parseInt(parts[0]);
                minutes = Integer.parseInt(parts[1]);
                seconds = Integer.parseInt(parts[2]);
            } else if (parts.length == 2) {
                minutes = Integer.parseInt(parts[0]);
                seconds = Integer.parseInt(parts[1]);
            } else if (parts.length == 1) {
                seconds = Integer.parseInt(parts[0]);
            } else {
                throw new DateTimeParseException(Strings.INVALID_TIME_FORMAT, timeString, 0);
            }

            return hours * 3600 + minutes * 60 + seconds;

        } catch (NumberFormatException e) {
            throw new DateTimeParseException(Strings.INVALID_TIME_FORMAT, timeString, 0);
        }
    }

    /**
     * Handles the download action of the currently selected resource.
     * Opens a file save dialog allowing the user to choose the destination and filename.
     * The default filename is set to the track title with the resource file extension.
     * If a file is selected, writes the resource data to the file.
     * Displays a success alert if the download completes successfully,
     * or an error alert if an IOException occurs during file saving.
     */
    @FXML
    private void handleDownload() {
        Resource r = resourceManager.getResource();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Downloading File");
        fileChooser.setInitialFileName(track.getTitle()+"."+r.getType().toString());
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(r.getType().toString().toUpperCase(), "*."+r.getType().toString());
        fileChooser.getExtensionFilters().add(extFilter);

        File selectedFile = fileChooser.showSaveDialog(Main.root);
        if (selectedFile != null) {
            try (FileOutputStream fos = new FileOutputStream(selectedFile)) {
                fos.write(r.getData());
                ViewManager.setAndShowAlert(Strings.SUCCESS, Strings.SUCCESS, Strings.FILE_DOWNLOADED, Alert.AlertType.INFORMATION);
            } catch (IOException e) {
                System.out.println("Error saving file: " + e.getMessage());
                ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }
}


