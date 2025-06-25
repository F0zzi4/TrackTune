package app.tracktune.exceptions;

/**
 * Exception thrown when an unsupported media format or type is encountered.
 * Indicates that the media cannot be processed due to incompatibility.
 */
public class MediaNotSupportedException extends TrackTuneException {

    /**
     * Constructs a new MediaNotSupportedException with the specified detail message.
     *
     * @param message the detail message explaining the cause of the exception
     */
    public MediaNotSupportedException(String message) {
        super(message);
    }
}
