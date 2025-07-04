package app.tracktune.controller;

import app.tracktune.controller.common.ResourceFileController;
import app.tracktune.model.resource.Resource;
import app.tracktune.utils.ResourceManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Abstract base controller class providing common functionality for all controllers.
 * <p>
 * Maintains a reference to a parent controller, manages a timeline timer for periodic actions,
 * and defines constants for resource preview dimensions.
 * <p>
 * Subclasses should extend this class to inherit shared behavior and properties.
 */
public abstract class Controller {
    /**
     * Reference to the parent controller.
     * Allows child controllers to communicate or delegate actions to their parent.
     */
    public Controller parentController;

    /**
     * Timeline used for scheduling repeated or delayed actions.
     */
    private Timeline timer;

    /**
     * Counter variable, usage defined by subclasses or timer events.
     */
    private int counter = 0;

    /**
     * Tracks how many times a ready event has occurred.
     */
    private int readies = 0;

    /**
     * Width for preview images or media nodes.
     */
    protected static final int previewWidth = 140;

    /**
     * Height for preview images or media nodes.
     */
    protected static final int previewHeight = 120;

    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    /**
     * Sets the parent controller for this controller.
     *
     * @param parentController the parent controller to associate with this controller
     */
    public void setParentController(Controller parentController) {
        this.parentController = parentController;
    }

    /**
     * Get the formatted request date, showing only up to minutes.
     * @param date date to convert into dd MMM yyyy, HH:mm format
     * @return Formatted request date
     */
    protected static String getFormattedRequestDate(Timestamp date) {
        if (date == null) return "";
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy, HH:mm");
        return formatter.format(new Date(date.getTime()));
    }

    /**
     * Converts a string to title case, capitalizing the first letter of each word.
     *
     * @param input the input string
     * @return the formatted string in title case
     */
    public static String toTitleCase(String input) {
        if (input == null || input.isEmpty()) return input;

        String[] words = input.toLowerCase().split(" ");
        StringBuilder titleCase = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                titleCase.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }

        return titleCase.toString().trim();
    }

    /**
     * Starts a timer that periodically checks the readiness of media resources within the provided container.
     * <p>
     * This method supports two types of containers:
     * <ul>
     *     <li><b>VBox</b>: Iterates through each child (expected to be HBoxes containing MediaViews) and replaces
     *     non-ready media nodes using the provided {@link ResourceManager}.</li>
     *     <li><b>StackPane</b>: Checks the first MediaView inside the VBox. If the media is ready or playing,
     *     and the controller is an instance of {@link ResourceFileController}, it initializes and starts playback.</li>
     * </ul>
     * The timer stops automatically when all media resources are ready.
     *
     * @param container       the JavaFX Node container that holds the media nodes (VBox or StackPane)
     * @param resources       the list of media resources to manage
     * @param resourceManager the manager responsible for creating and updating media nodes
     */
    protected void startTimer(Node container, List<Resource> resources, ResourceManager resourceManager) {
        timer = new Timeline(new KeyFrame(Duration.seconds(1), _ -> {
            if (isRunning.get()) {
                return; // Skip if already running
            }

            isRunning.set(true);

            Platform.runLater(() -> {
                try {
                    if (container instanceof VBox resourcesContainer) {
                        for (Node resourceBox : resourcesContainer.getChildren()) {
                            if (resourceBox instanceof HBox hbox) {
                                if (hbox.getChildren().getFirst() instanceof MediaView mediaView) {
                                    if (mediaView.getMediaPlayer().getStatus() == MediaPlayer.Status.READY) {
                                        readies++;
                                    } else {
                                        Node node = resourceManager.createMediaNode(resources.get(counter), previewWidth, previewHeight, true);
                                        hbox.getChildren().set(0, node);
                                    }
                                }
                            }
                            counter++;
                        }
                        if (readies == resources.size()) {
                            stopTimer();
                        }
                        readies = 0;
                        counter = 0;
                    } else if (container instanceof StackPane stackPane) {
                        VBox vbox = (VBox) stackPane.getChildren().getFirst();
                        MediaView mediaView = (MediaView) vbox.getChildren().getFirst();
                        if (mediaView.getMediaPlayer().getStatus() == MediaPlayer.Status.READY
                                || mediaView.getMediaPlayer().getStatus() == MediaPlayer.Status.PLAYING) {
                            if (this instanceof ResourceFileController controller
                                    && mediaView.getMediaPlayer().getStatus() == MediaPlayer.Status.READY) {
                                controller.setupMediaPlayer(mediaView);
                                controller.handlePlayPause();
                            }
                            stopTimer();
                        } else {
                            Node node = resourceManager.createMediaNode(resources.get(counter), stackPane.getPrefWidth(), stackPane.getPrefHeight(), false);
                            vbox.getChildren().set(counter, node);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    isRunning.set(false);
                }
            });
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    /**
     * Stops the currently running timer, if it exists.
     * <p>
     * This method safely checks whether the timer has been initialized before attempting to stop it.
     */
    protected void stopTimer() {
        if (timer != null) {
            timer.stop();
        }
    }

    /**
     * Disposes all media players contained within the given VBox and stops the timer.
     * <p>
     * This method iterates over each child of the provided VBox, expecting HBoxes that contain
     * {@link MediaView} nodes. For each media player found, it stops playback and releases associated resources.
     * The timer is also stopped to prevent further updates.
     *
     * @param resourcesContainer the VBox containing the media nodes to be disposed
     */
    protected void dispose(VBox resourcesContainer) {
        stopTimer();

        for (Node node : resourcesContainer.getChildren()) {
            if (node instanceof HBox hbox && hbox.getChildren().getFirst() instanceof MediaView mediaView) {
                MediaPlayer player = mediaView.getMediaPlayer();
                if (player != null) {
                    player.stop();
                    player.dispose();
                }
            }
        }
    }
}
