package app.tracktune.exceptions;

/**
 * Exception thrown when a requested entity cannot be found in the system.
 * This indicates that the searched entity does not exist or is missing.
 */
public class EntityNotFoundException extends TrackTuneException {

    /**
     * Constructs a new EntityNotFoundException with the specified detail message.
     *
     * @param message the detail message explaining the cause of the exception
     */
    public EntityNotFoundException(String message) {
        super(message);
    }
}
