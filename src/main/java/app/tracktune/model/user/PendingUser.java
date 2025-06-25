package app.tracktune.model.user;

import java.sql.Timestamp;

/**
 * Represents a user whose account registration or authentication request is still pending.
 * <p>
 * Contains information about the request date and the current status of the request.
 * </p>
 */
public class PendingUser extends User {
    /**
     * Timestamp when the request was made.
     */
    private final Timestamp requestDate;

    /**
     * Current status of the authentication or registration request.
     */
    private final AuthRequestStatusEnum status;

    /**
     * Constructs a PendingUser with a known ID.
     *
     * @param id          The user's unique identifier
     * @param username    The user's username
     * @param password    The user's password
     * @param name        The user's first name
     * @param surname     The user's surname
     * @param requestDate The timestamp when the request was submitted
     * @param status      The current status of the request
     */
    public PendingUser(Integer id, String username, String password, String name, String surname, Timestamp requestDate, AuthRequestStatusEnum status) {
        super(id, username, password, name, surname);
        this.requestDate = requestDate;
        this.status = status;
    }

    /**
     * Constructs a PendingUser without specifying an ID.
     * Useful when creating a new pending user before the database assigns an ID.
     *
     * @param username    The user's username
     * @param password    The user's password
     * @param name        The user's first name
     * @param surname     The user's surname
     * @param requestDate The timestamp when the request was submitted
     * @param status      The current status of the request
     */
    public PendingUser(String username, String password, String name, String surname, Timestamp requestDate, AuthRequestStatusEnum status) {
        super(username, password, name, surname);
        this.requestDate = requestDate;
        this.status = status;
    }

    /**
     * Returns the timestamp when the account request was made.
     *
     * @return the request date timestamp
     */
    public Timestamp getRequestDate() {
        return requestDate;
    }

    /**
     * Returns the current status of the pending request.
     *
     * @return the authentication request status
     */
    public AuthRequestStatusEnum getStatus() {
        return status;
    }
}
