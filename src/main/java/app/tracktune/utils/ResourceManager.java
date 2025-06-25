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
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.*;

/**
 * Singleton class responsible for managing the current {@link Resource} instance
 * and providing media-related operations for it.
 */
public final class ResourceManager {

    /** Singleton instance of ResourceManager */
    private static ResourceManager instance;

    /** The current resource being managed */
    private Resource resource;

    /**
     * Create the instance of the resource manager following singleton pattern
     */
    private ResourceManager() {}

    /**
     * Retrieves the singleton instance of ResourceManager.
     *
     * @return the ResourceManager instance
     */
    public static ResourceManager getInstance() {
        return instance;
    }

    /**
     * Initializes the resource manager safely
     */
    public static void initialize() {
        if (instance == null) {
            instance = new ResourceManager();
        }
    }

    /**
     * Creates a JavaFX Node representing the media content of the current resource.
     * <p>
     * The Node returned depends on the resource type:
     * <ul>
     *   <li>For {@link ResourceTypeEnum#link}, returns a styled Label with the link type.</li>
     *   <li>For supported image types (including PDF), returns an ImageView or a ScrollPane
     *       containing PDF pages as images. PDF previews show only the first page.</li>
     *   <li>For supported audio/video types, returns a MediaView player or a label preview for MP3.</li>
     *   <li>Throws an exception if the media type is unsupported.</li>
     * </ul>
     *
     * @param width     the desired width of the media node
     * @param height    the desired height of the media node
     * @param isPreview true if a preview version should be created (e.g., only first PDF page)
     * @return a JavaFX Node that visually represents the resource media content
     * @throws TrackTuneException if an I/O error occurs or media type is not supported
     */
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

    /**
     * Sets the current Resource.
     *
     * @param resource the Resource to set
     */
    public void setResource(Resource resource) {
        this.resource = resource;
    }

    /**
     * Gets the current Resource.
     *
     * @return the current Resource
     */
    public Resource getResource() {
        return resource;
    }
}
