package app.tracktune.model.user;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class PendingUser extends User {
    private final Timestamp requestDate;
    private final AuthRequestStatusEnum status;

    /**
     * Constructor for creating a pending user object
     * @param username The user's username
     * @param password The user's password
     * @param name The user's name
     * @param surname The user's surname
     * @param requestDate Request timestamp
     */
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

    /**
     * Get the formatted request date, showing only up to minutes.
     * @return Formatted request date
     */
    public String getFormattedRequestDate() {
        if (requestDate == null) return "";
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy, HH:mm");
        return formatter.format(new Date(requestDate.getTime()));
    }
}
