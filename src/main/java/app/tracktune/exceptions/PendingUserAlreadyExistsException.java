package app.tracktune.exceptions;

public class PendingUserAlreadyExistsException extends TrackTuneException {
    public PendingUserAlreadyExistsException(String message) {
        super(message);
    }
}
