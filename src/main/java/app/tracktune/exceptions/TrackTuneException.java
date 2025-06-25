package app.tracktune.exceptions;

/**
 * Base unchecked exception class for all custom exceptions in the TrackTune application.
 * Extends RuntimeException to allow exceptions to be thrown without mandatory catch blocks.
 */
public class TrackTuneException extends RuntimeException {

    /**
     * Constructs a new TrackTuneException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public TrackTuneException(String message) {
        super(message);
    }
}
