package app.tracktune.utils;

import app.tracktune.exceptions.MediaNotSupportedException;
import app.tracktune.exceptions.TrackTuneException;
import app.tracktune.model.resource.AudioVideoFileEnum;
import app.tracktune.model.resource.ImageFileEnum;
import app.tracktune.model.resource.Resource;
import app.tracktune.view.ViewManager;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;

public final class ResourceConverter {
    public final Resource resource;

    public ResourceConverter(Resource resource){
        this.resource = resource;
    }

    public Node createMediaNode(double width, double height) throws TrackTuneException {
        String extension = resource.getType().toString();

        if(ImageFileEnum.isSupported(extension)){
            // Images
            ByteArrayInputStream bis = new ByteArrayInputStream(resource.getData());
            Image image = new Image(bis, width, height, true, true);
            ImageView imgView = new ImageView(image);
            imgView.setFitWidth(width);
            imgView.setFitHeight(height);
            imgView.setPreserveRatio(true);
            return imgView;
        }else if(AudioVideoFileEnum.isSupported(extension)){
            // Audio/Videos
            MediaView mediaView = createMediaPlayer(resource.getData(), extension);
            mediaView.setFitWidth(width);
            mediaView.setFitHeight(height);
            return mediaView;
        }else{
            throw new MediaNotSupportedException(Strings.MEDIA_NOT_SUPPORTED);
        }
    }

    /**
     * Initializes the media player with a video file located at a specific path.
     * The video will be displayed inside the fileContainer.
     * If the video cannot be loaded or played, an error alert is shown.
     */
    private MediaView createMediaPlayer(byte[] videoBytes, String extension) {
        MediaView mediaView = null;
        try {
            File tempFile = File.createTempFile("temp", extension);
            tempFile.deleteOnExit();

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(videoBytes);
            }

            Media media = new Media(tempFile.toURI().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(media);

            mediaView = new MediaView(mediaPlayer);
            mediaView.setPreserveRatio(true);
        } catch (Exception e) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.MEDIA_ERROR, Alert.AlertType.ERROR);
            System.err.println(Strings.MEDIA_ERROR + e.getMessage());
        }
        return mediaView;
    }

}
