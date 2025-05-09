package app.tracktune.exceptions;

public class UserAlreadyExistsException extends TrackTuneException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
