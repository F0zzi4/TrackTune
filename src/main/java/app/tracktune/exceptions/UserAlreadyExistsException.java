package app.tracktune.exceptions;

/**
 * Exception thrown when an attempt is made to create or add a user that already exists.
 * This typically indicates a violation of uniqueness constraints on user identifiers such as username.
 */
public class UserAlreadyExistsException extends TrackTuneException {

    /**
     * Constructs a new UserAlreadyExistsException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
