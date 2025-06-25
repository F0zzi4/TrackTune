package app.tracktune.exceptions;

/**
 * Exception thrown when an attempt is made to create or add an author
 * that already exists in the system.
 * Extends TrackTuneException as a specific domain exception.
 */
public class AuthorAlreadyExistsException extends TrackTuneException {

    /**
     * Constructs a new AuthorAlreadyExistsException with the specified detail message.
     *
     * @param message the detail message explaining the cause of the exception
     */
    public AuthorAlreadyExistsException(String message) {
        super(message);
    }
}
