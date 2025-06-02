package app.tracktune.model.user;

import java.sql.Timestamp;

public class PendingUser extends User {
    private final Timestamp requestDate;
    private final AuthRequestStatusEnum status;

    /**
     * Constructor for creating a pending user object
     * @param id The user's unique ID
     * @param username The user's username
     * @param password The user's password
     * @param name The user's name
     * @param surname The user's surname
     * @param requestDate Request timestamp
     * @param status Request status
     */
    public PendingUser(Integer id, String username, String password, String name, String surname, Timestamp requestDate, AuthRequestStatusEnum status) {
        super(id, username, password, name, surname);
        this.requestDate = requestDate;
        this.status = status;
    }

    public PendingUser(String username, String password, String name, String surname, Timestamp requestDate, AuthRequestStatusEnum status) {
        super(username, password, name, surname);
        this.requestDate = requestDate;
        this.status = status;
    }

    /**
     * Get the account request timestamp
     * @return Request timestamp
     */
    public Timestamp getRequestDate() {
        return requestDate;
    }

    /**
     * Get the status of the request
     * @return Status of the request
     */
    public AuthRequestStatusEnum getStatus() {
        return status;
    }
}