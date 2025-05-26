package app.tracktune.controller.common;

import app.tracktune.controller.Controller;
import app.tracktune.controller.admin.AdminDashboardController;
import app.tracktune.controller.authenticatedUser.AuthenticatedUserDashboardController;
import app.tracktune.controller.authentication.SessionManager;
import app.tracktune.exceptions.TrackTuneException;
import app.tracktune.model.author.Author;
import app.tracktune.model.author.AuthorDAO;
import app.tracktune.model.genre.Genre;
import app.tracktune.model.genre.GenreDAO;
import app.tracktune.model.musicalInstrument.MusicalInstrument;
import app.tracktune.model.musicalInstrument.MusicalInstrumentDAO;
import app.tracktune.model.resource.MultimediaResource;
import app.tracktune.model.resource.Resource;
import app.tracktune.model.resource.ResourceDAO;
import app.tracktune.model.resource.ResourceTypeEnum;
import app.tracktune.model.track.Track;
import app.tracktune.model.track.TrackDAO;
import app.tracktune.utils.Frames;
import app.tracktune.utils.ResourceManager;
import app.tracktune.utils.Strings;
import app.tracktune.view.ViewManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;


public class ResourceFileController extends Controller implements Initializable {
    @FXML private StackPane fileContainer;
    @FXML private HBox videoToolBox;
    @FXML private Label lblTitle;
    @FXML private VBox metadataBox;
    @FXML private TextField commentField;
    @FXML private VBox commentVBox;


    private final ResourceManager resourceManager;
    private MediaPlayer mediaPlayer;
    private Node resourceNode;
    private Track track;

    //CONSTANTS
    private final int defaultSkipTime = 10;
    private boolean isPlaying = false;
    private Slider sliderProgress;
    private Label lblTimer;
    private Label lblDuration;
    private Stage fullStage = null;


    public ResourceFileController(Resource resource) {
        resourceManager = new ResourceManager(resource);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            resourceNode = resourceManager.createMediaNode(fileContainer.getPrefWidth(), fileContainer.getPrefHeight());
            boolean isMultimedia = resourceNode instanceof MediaView;

            if (!isMultimedia) {
                int defaultGapTitle = 10;
                lblTitle.setLayoutY(lblTitle.getLayoutY() + defaultGapTitle);
                int defaultGapContainerToolBox = 30;
                fileContainer.setLayoutY(fileContainer.getLayoutY() + defaultGapContainerToolBox);
                fileContainer.getChildren().add(resourceNode);
                videoToolBox.setVisible(false);
                metadataBox.getChildren().add(setMetadata());
                metadataBox.setAlignment(Pos.CENTER);
                return;
            }

            setUpMediaPlayer();
            metadataBox.getChildren().add(setMetadata());
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

    private void setUpMediaPlayer() {
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

    private VBox setMetadata() {
        VBox box = new VBox();
        box.setAlignment(Pos.CENTER);
        box.setSpacing(5);
        TrackDAO trackDAO = new TrackDAO();
        AuthorDAO authorDAO = new AuthorDAO();
        GenreDAO genreDAO = new GenreDAO();
        MusicalInstrumentDAO instrumentDAO = new MusicalInstrumentDAO();

        track = trackDAO.getTrackByResourceId(resourceManager.resource.getId());
        String title = track.getTitle();
        box.getChildren().add(createMetadataRow("Title:", title));

        String authors = authorDAO.getAllAuthorsByTrackId(track.getId()).stream()
                .map(Author::getAuthorshipName)
                .collect(Collectors.joining(", "));
        box.getChildren().add(createMetadataRow("Author:", authors));

        String genres = genreDAO.getAllGenresByTrackId(track.getId()).stream()
                .map(Genre::getName)
                .collect(Collectors.joining(", "));
        box.getChildren().add(createMetadataRow("Genre:", genres));

        String instruments = instrumentDAO.getAllInstrumentByTrackId(track.getId()).stream()
                .map(MusicalInstrument::getName)
                .collect(Collectors.joining(", "));
        box.getChildren().add(createMetadataRow("Instruments:", instruments));

        box.getChildren().add(createMetadataRow("File format:", resourceManager.resource.getType().toString()));
        box.getChildren().add(createMetadataRow("Resource Size:", humanReadableByteCount(resourceManager.resource.getData().length)));

        if(resourceManager.resource instanceof MultimediaResource multimediaResource) {
            mediaPlayer.setOnReady(() -> {
                metadataBox.getChildren().add(createMetadataRow("Duration:", formatDuration(mediaPlayer.getTotalDuration())));
                metadataBox.getChildren().add(createMetadataRow("Registered Data:", multimediaResource.getResourceDate().toString()));
            });
        }

        return box;
    }


    /**
     * Crea una riga di metadato con Label titolo e Label valore, colorate diversamente
     */
    private HBox createMetadataRow(String title, String value) {
        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("metadata-label");
        Label lblValue = new Label(value != null ? value : "N/A");
        lblValue.getStyleClass().add("metadata-value");

        HBox row = new HBox(5, lblTitle, lblValue);
        row.setAlignment(Pos.CENTER);
        return row;
    }

    private String humanReadableByteCount(long bytes) {
        int unit = 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = ("KMGTPE").charAt(exp-1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
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

    @FXML
    private void handleSendComment() {
        String commentText = commentField.getText();
        if (commentText != null && !commentText.trim().isEmpty()) {
            addComment(commentText);
            commentField.clear();
        }
    }

    private void addComment(String commentText) {
        // LABEL: Nome Cognome
        Label nameLabel = new Label(SessionManager.getInstance().getUser().getName() + " " + SessionManager.getInstance().getUser().getSurname());
        nameLabel.getStyleClass().add("comment-author");

        // Bottoni icona: rispondi
        FontIcon replyIcon = new FontIcon("mdi2r-reply");
        replyIcon.setIconSize(18);
        Button replyButton = new Button();
        replyButton.setGraphic(replyIcon);
        replyButton.getStyleClass().add("comment-icon-button");
        replyButton.setOnAction(e -> {
            System.out.println("Rispondi a: ");

            //TODO - ..
        });

        // Bottone icona: elimina
        FontIcon deleteIcon = new FontIcon("mdi2d-delete");
        deleteIcon.setIconSize(18);
        Button deleteButton = new Button();
        deleteButton.setGraphic(deleteIcon);
        deleteButton.getStyleClass().add("delete-comment");
        HBox buttonsBox;

        // TOP: Nome + bottoni
        HBox topBox = new HBox();
        topBox.setAlignment(Pos.CENTER_LEFT);
        topBox.setSpacing(5);

        buttonsBox = new HBox(5, replyButton, deleteButton);
        buttonsBox.setAlignment(Pos.CENTER_RIGHT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        topBox.getChildren().addAll(nameLabel, spacer, buttonsBox);

        HBox aboveBox = new HBox();
        // CENTER: commento
        Label commentLabel = new Label(commentText);
        commentLabel.setWrapText(true);
        commentLabel.getStyleClass().add("comment-text");
        commentLabel.setAlignment(Pos.CENTER_LEFT);

        // BOTTOM: data
        FontIcon clockIcon = new FontIcon("mdi2c-clock-outline");
        clockIcon.setIconSize(14);
        Label dateLabel = new Label("data ...");
        dateLabel.setGraphic(clockIcon);
        dateLabel.setContentDisplay(ContentDisplay.LEFT);
        dateLabel.getStyleClass().add("comment-date");
        dateLabel.setAlignment(Pos.CENTER_RIGHT);
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        aboveBox.getChildren().addAll(commentLabel, spacer2, dateLabel);

        // VBOX principale del commento
        VBox commentBox = new VBox(topBox, aboveBox);
        commentBox.setSpacing(5);
        commentBox.setPadding(new Insets(10));
        commentBox.setStyle("-fx-background-color: #f9f9f9; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #cccccc;");
        commentBox.setMaxWidth(280);

        // Contenitore principale (opzionale per padding esterno)
        VBox container = new VBox(commentBox);
        container.setPadding(new Insets(5, 0, 5, 0));

        deleteButton.setOnAction(e -> {
            commentVBox.getChildren().remove(container);
        });

        commentVBox.getChildren().add(container);
    }

}


