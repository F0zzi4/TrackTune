package app.tracktune.controller.common;

import app.tracktune.Main;
import app.tracktune.controller.Controller;
import app.tracktune.controller.admin.AdminDashboardController;
import app.tracktune.controller.authenticatedUser.AuthenticatedUserDashboardController;
import app.tracktune.controller.authentication.SessionManager;
import app.tracktune.exceptions.TrackTuneException;
import app.tracktune.model.DatabaseManager;
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
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import java.net.URL;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

public class ResourceFileController extends Controller implements Initializable {
    @FXML private StackPane fileContainer;
    @FXML private HBox videoToolBox;
    @FXML private Label lblTitle;
    @FXML private VBox metadataBox;
    @FXML private TextField commentField;
    @FXML private VBox commentVBox;
    @FXML private Button segmentButton;

    private final ResourceManager resourceManager;
    private MediaPlayer mediaPlayer;
    private Node resourceNode;

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
            Platform.runLater(() -> Main.root.setOnCloseRequest(event -> disposeMediaPlayer()));
            resourceNode = resourceManager.createMediaNode(fileContainer.getPrefWidth(), fileContainer.getPrefHeight(), false);
            boolean isMultimedia = resourceNode instanceof MediaView;

            if (!isMultimedia) {
                int defaultGapTitle = 10;
                lblTitle.setLayoutY(lblTitle.getLayoutY() + defaultGapTitle);
                int defaultGapContainerToolBox = 30;
                fileContainer.setLayoutY(fileContainer.getLayoutY() + defaultGapContainerToolBox);
                fileContainer.getChildren().add(resourceNode);
                videoToolBox.setVisible(false);
                metadataBox.getChildren().add(setDetailsInfo());
                metadataBox.setAlignment(Pos.CENTER);
                setComments();
                if(resourceManager.resource.getType().equals(ResourceTypeEnum.pdf))
                    segmentButton.setVisible(true);
            }
            else{
                segmentButton.setVisible(true);
                setupMediaPlayer();
                metadataBox.getChildren().add(setDetailsInfo());
                setComments();
            }
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

    private void setupMediaPlayer() {
        mediaPlayer = ((MediaView) resourceNode).getMediaPlayer();

        initSlider();
        initLabels();

        setupSliderListeners();


        HBox videoControls = new HBox(10, lblTimer, sliderProgress, lblDuration);
        videoControls.setAlignment(Pos.CENTER);
        videoControls.setPadding(new Insets(10));
        applyStyles();

        VBox videoLayout = new VBox();
        if (resourceManager.resource.getType().equals(ResourceTypeEnum.mp3)) {
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

        mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> updateTimeLabels(newTime));
        mediaPlayer.setOnReady(() -> sliderProgress.setDisable(false));

        videoToolBox.setVisible(true);
        handlePlayPause();
    }

    private void setComments() {
        if (resourceManager.getResource() != null) {
            for (Comment comment : DatabaseManager.getDAOProvider().getCommentDAO().getAllCommentByResource(resourceManager.getResource().getId())) {
                User user = DatabaseManager.getDAOProvider().getUserDAO().getById(comment.getUserID());
                if(user != null)
                    addCommentOnView(comment, user);
            }
        }
    }

    private VBox setDetailsInfo() {
        VBox box = new VBox();
        box.setAlignment(Pos.CENTER_LEFT);
        box.setSpacing(5);

        Track track = DatabaseManager.getDAOProvider().getTrackDAO().getTrackByResourceId(resourceManager.resource.getId());
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

        box.getChildren().add(createMetadataRow(Strings.FILE_FORMAT, resourceManager.resource.getType().toString()));
        box.getChildren().add(createMetadataRow(Strings.RESOURCE_SIZE, humanReadableByteCount(resourceManager.resource.getData().length)));

        if(resourceManager.resource instanceof MultimediaResource multimediaResource) {
            mediaPlayer.setOnReady(() -> {
                metadataBox.getChildren().add(createMetadataRow(Strings.DURATION, formatDuration(mediaPlayer.getTotalDuration())));
                metadataBox.getChildren().add(createMetadataRow(Strings.REGISTERED_DATA, multimediaResource.getResourceDate().toString()));
            });
        }

        User user = DatabaseManager.getDAOProvider().getUserDAO().getById(resourceManager.resource.getUserID());
        box.getChildren().add(createMetadataRow(Strings.UPLOADED, user.getName() + " " + user.getSurname()));

        return box;
    }

    private HBox createMetadataRow(String title, String value) {
        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("metadata-label");
        Label lblValue = new Label(value != null ? value : "N/A");
        lblValue.getStyleClass().add("metadata-value");

        HBox row = new HBox(5, lblTitle, lblValue);
        row.setAlignment(Pos.CENTER_LEFT);
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

    private void seekTo(Duration time) {
        if (mediaPlayer != null && time != null) {
            mediaPlayer.seek(time); // 'time' è un javafx.util.Duration
            double total = mediaPlayer.getTotalDuration().toSeconds();
            if (total > 0) {
                double percent = (time.toSeconds() / total) * 100;
                sliderProgress.setValue(percent); // aggiorna anche lo slider
            }
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
        Comment c;
        if (commentText != null && !commentText.trim().isEmpty()) {
            c = new Comment(commentText,new Timestamp(System.currentTimeMillis()), ViewManager.getSessionUser().getId(), resourceManager.getResource().getId());
            int id = DatabaseManager.getDAOProvider().getCommentDAO().insert(c);
            c = new Comment(id, c.getDescription(), c.getStartTrackInterval(), c.getEndTrackInterval(), c.getCreationDate(), c.getUserID(), resourceManager.getResource().getId());
            addCommentOnView(c, ViewManager.getSessionUser());
            commentField.clear();
        }
    }

    private void addCommentOnView(Comment comment, User user) {
        VBox commentNode = createCommentNode(comment, user, 0);
        commentVBox.getChildren().add(commentNode);
    }

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

        HBox buttonsBox = (ViewManager.getSessionUser() instanceof Administrator || resourceManager.resource.getUserID() == ViewManager.getSessionUser().getId() || Objects.equals(comment.getUserID(), ViewManager.getSessionUser().getId()))
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
        repliesLabel.setOnMouseClicked(event -> {
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
        else if(resourceManager.resource.isAuthor()){
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
        if(comment.getEndTrackInterval() != 0 && comment.getEndTrackInterval() != comment.getStartTrackInterval()){
            Label start = new Label(formatDuration(new Duration(comment.getStartTrackInterval()*1000)));
            Label dash = new Label("- ");
            Label end = new Label(formatDuration(new Duration(comment.getEndTrackInterval()*1000)));
            HBox intervals = new HBox();

            start.setOnMouseClicked(event -> {
                seekTo(new Duration(comment.getStartTrackInterval() * 1000));
            });
            if(comment.getEndTrackInterval() != 0){
                intervals.getChildren().addAll(start, dash, end);
                end.setOnMouseClicked(event -> {
                    seekTo(new Duration(comment.getEndTrackInterval() * 1000));
                });

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

        replyButton.setOnAction(e -> {
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

        deleteButton.setOnAction(e -> {
            boolean response = ViewManager.setAndGetConfirmAlert(Strings.CONFIRM_DELETION, Strings.CONFIRM_DELETION, Strings.ARE_YOU_SURE);
            if(response){
                ((VBox) container.getParent()).getChildren().remove(container);
                DatabaseManager.getDAOProvider().getCommentDAO().deleteById(comment.getID());
            }
        });

        return container;
    }

    private User getUser(int userId) {
        return DatabaseManager.getDAOProvider().getUserDAO().getById(userId);
    }

    @FXML
    private void addSegmentComment() {
        ViewManager.showSegmentCommentDialog().ifPresent(data -> {
            String start = data[0];
            String end = data[1];
            String comment = data[2];

            if(resourceManager.resource.getType().equals(ResourceTypeEnum.pdf)){
                //TODO - pdf management
            }
            else {
                try {
                    int startDuration = parseToSeconds(start);
                    int endDuration = 0;

                    if(new Duration(startDuration * 1000).greaterThan(mediaPlayer.getTotalDuration()) || startDuration < 0)
                        throw new TrackTuneException(Strings.ERROR_START_TIME_GREATER_DURATION);
                    if(new Duration(endDuration * 1000).greaterThan(mediaPlayer.getTotalDuration()) || endDuration < 0)
                        throw new TrackTuneException(Strings.ERROR_END_TIME_GREATER_DURATION);

                    if(!end.isEmpty()){
                        endDuration = parseToSeconds(end);
                    }
                    Comment c = new Comment(
                            comment,
                            startDuration,
                            endDuration,
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
            }
        });
    }

    private int parseToSeconds(String timeString) throws DateTimeParseException {
        String[] parts = timeString.split(":");

        try {
            int hours = 0;
            int minutes = 0;
            int seconds = 0;

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

}


