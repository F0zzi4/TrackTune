package app.tracktune.exceptions;

/**
 * Exception thrown to indicate a potential SQL injection attempt.
 * This exception is used to signal that input data might compromise database security.
 */
public class SQLInjectionException extends TrackTuneException {

    /**
     * Constructs a new SQLInjectionException with the specified detail message.
     *
     * @param message the detail message describing the cause of the exception
     */
    public SQLInjectionException(String message) {
        super(message);
    }
}
