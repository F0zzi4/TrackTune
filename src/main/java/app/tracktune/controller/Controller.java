package app.tracktune.controller;

import app.tracktune.controller.common.ResourceFileController;
import app.tracktune.model.resource.Resource;
import app.tracktune.utils.ResourceManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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

public class Controller {
    public Controller parentController;
    private Timeline timer;
    private int counter = 0;
    private int readies = 0;
    // CONSTANTS
    protected static final int previewWidth = 140;
    protected static final int previewHeight = 120;

    public void setParentController(Controller parentController){
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

    protected void startTimer(Node container, List<Resource> resources, ResourceManager resourceManager) {
        timer = new Timeline(new KeyFrame(Duration.seconds(1), _ -> {
            if(container instanceof VBox resourcesContainer){
                for(Node resourceBox : resourcesContainer.getChildren()){
                    if(resourceBox instanceof HBox hbox){
                        if(hbox.getChildren().getFirst() instanceof MediaView mediaView){
                            if(mediaView.getMediaPlayer().getStatus() == MediaPlayer.Status.READY){
                                readies++;
                            }
                            else{
                                resourceManager.setResource(resources.get(counter));
                                Node node = resourceManager.createMediaNode(previewWidth, previewHeight, true);
                                hbox.getChildren().set(0, node);
                            }
                        }
                    }
                    counter++;
                }
                if(readies == resources.size()){
                    stopTimer();
                }
                readies = 0;
                counter = 0;
            }else if(container instanceof StackPane stackPane){
                VBox vbox = (VBox) stackPane.getChildren().getFirst();
                MediaView mediaView = (MediaView) vbox.getChildren().getFirst();
                if(mediaView.getMediaPlayer().getStatus() == MediaPlayer.Status.READY || mediaView.getMediaPlayer().getStatus() == MediaPlayer.Status.PLAYING){
                    if(this instanceof ResourceFileController controller){
                        controller.setupMediaPlayer(mediaView);
                    }
                    stopTimer();
                }else{
                    resourceManager.setResource(resources.get(counter));
                    Node node = resourceManager.createMediaNode(stackPane.getPrefWidth(), stackPane.getPrefHeight(), false);
                    vbox.getChildren().set(counter, node);
                }
            }
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    protected void stopTimer() {
        if (timer != null) {
            timer.stop();
        }
    }

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
