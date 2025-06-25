package app.tracktune.exceptions;

/**
 * Exception thrown to indicate an error related to SQLite database operations.
 * This exception signals issues such as query failures, connection problems, or constraint violations.
 */
public class SQLiteException extends TrackTuneException {

    /**
     * Constructs a new SQLiteException with the specified detail message.
     *
     * @param message the detail message describing the cause of the exception
     */
    public SQLiteException(String message) {
        super(message);
    }
}
