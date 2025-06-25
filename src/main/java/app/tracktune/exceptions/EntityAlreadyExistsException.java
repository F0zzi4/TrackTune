package app.tracktune.exceptions;

/**
 * Exception thrown when an attempt is made to create or add an entity
 * that already exists in the system.
 * This is a generic exception for duplicate entity scenarios.
 */
public class EntityAlreadyExistsException extends TrackTuneException {

    /**
     * Constructs a new EntityAlreadyExistsException with the specified detail message.
     *
     * @param message the detail message explaining the cause of the exception
     */
    public EntityAlreadyExistsException(String message) {
        super(message);
    }
}
