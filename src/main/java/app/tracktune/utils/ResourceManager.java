package app.tracktune.utils;

import app.tracktune.exceptions.MediaNotSupportedException;
import app.tracktune.exceptions.TrackTuneException;
import app.tracktune.model.resource.AudioVideoFileEnum;
import app.tracktune.model.resource.ImageFileEnum;
import app.tracktune.model.resource.Resource;
import app.tracktune.model.resource.ResourceTypeEnum;
import app.tracktune.view.ViewManager;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.Time;

public final class ResourceManager {
    public final Resource resource;

    public ResourceManager(Resource resource){
        this.resource = resource;
    }

    public Node createMediaNode(double width, double height, boolean isPreview) throws TrackTuneException {
        ResourceTypeEnum type = resource.getType();
        String extension = type.toString();

        if (type.equals(ResourceTypeEnum.link)) {
            Label linkLabel = new Label(extension.toUpperCase());
            linkLabel.setPrefWidth(width);
            linkLabel.setPrefHeight(height / 2);
            linkLabel.getStyleClass().add("resource-link-label");
            linkLabel.getStyleClass().add("fx-font-style: underline;");
            return linkLabel;
        } else if (ImageFileEnum.isSupported(extension)) {
            if (type.equals(ResourceTypeEnum.pdf)) {
                // Pdf images
                try {
                    InputStream input = new ByteArrayInputStream(resource.getData());
                    PDDocument document = PDDocument.load(input);
                    PDFRenderer pdfRenderer = new PDFRenderer(document);

                    BufferedImage bim = pdfRenderer.renderImageWithDPI(0, 150);
                    Image fxImage = SwingFXUtils.toFXImage(bim, null);
                    ImageView imageView = new ImageView(fxImage);
                    imageView.setFitWidth(width);
                    imageView.setPreserveRatio(true);

                    // If it's preview, returns the first page
                    if (isPreview) {
                        document.close();
                        return imageView;
                    } else {
                        VBox pdfPagesBox = new VBox(10);
                        pdfPagesBox.setPrefWidth(width);
                        pdfPagesBox.getChildren().add(imageView);

                        int pageCount = document.getNumberOfPages();
                        for (int page = 1; page < pageCount; page++) {
                            bim = pdfRenderer.renderImageWithDPI(page, 150);
                            fxImage = SwingFXUtils.toFXImage(bim, null);
                            ImageView extraPage = new ImageView(fxImage);
                            extraPage.setFitWidth(width);
                            extraPage.setPreserveRatio(true);
                            pdfPagesBox.getChildren().add(extraPage);
                        }

                        ScrollPane scrollPane = new ScrollPane(pdfPagesBox);
                        scrollPane.setPrefWidth(width);
                        scrollPane.setPrefHeight(height);
                        scrollPane.setFitToWidth(true);
                        scrollPane.getStyleClass().add("scroll-pane");
                        document.close();
                        return scrollPane;
                    }

                } catch (IOException e) {
                    throw new TrackTuneException(Strings.MEDIA_ERROR);
                }
            } else {
                // Normal images
                ByteArrayInputStream bis = new ByteArrayInputStream(resource.getData());
                Image image = new Image(bis, width, height, true, true);
                ImageView imgView = new ImageView(image);
                imgView.setFitWidth(width);
                imgView.setFitHeight(height);
                imgView.setPreserveRatio(true);
                return imgView;
            }
        } else if (AudioVideoFileEnum.isSupported(extension)) {
            // Audio/Videos
            if(type.equals(ResourceTypeEnum.mp3) && isPreview) {
                Label linkLabel = new Label(".MP3");
                linkLabel.setPrefWidth(width);
                linkLabel.setPrefHeight(height / 2);
                linkLabel.getStyleClass().add("resource-link-label");
                return linkLabel;
            }else{
                MediaView mediaView = createMediaPlayer(resource.getData(), extension);
                mediaView.setFitWidth(width);
                mediaView.setFitHeight(height);
                return mediaView;
            }
        } else {
            throw new MediaNotSupportedException(Strings.MEDIA_NOT_SUPPORTED);
        }
    }

    public static Time calcMediaDuration(byte[] data, String extension){
        MediaView mediaView = ResourceManager.createMediaPlayer(data, extension);
        Duration duration = mediaView.getMediaPlayer().getTotalDuration();
        long millis = Math.round(duration.toMillis());
        return new Time(millis);
    }

    /**
     * Initializes the media player with a video file located at a specific path.
     * The video will be displayed inside the fileContainer.
     * If the video cannot be loaded or played, an error alert is shown.
     */
    private static MediaView createMediaPlayer(byte[] videoBytes, String extension) {
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

    public Resource getResource() {
        return resource;
    }
}
