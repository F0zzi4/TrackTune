package app.tracktune.exceptions;

/**
 * Exception thrown when a user is not found in the system or database.
 * This typically occurs when attempting to retrieve or operate on a user that does not exist.
 */
public class UserNotFoundException extends TrackTuneException {

    /**
     * Constructs a new UserNotFoundException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public UserNotFoundException(String message) {
        super(message);
    }
}
