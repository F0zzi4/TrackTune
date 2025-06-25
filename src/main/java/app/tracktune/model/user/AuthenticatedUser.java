package app.tracktune.model.user;

import java.sql.Timestamp;

/**
 * Represents an authenticated user in the system.
 * This class extends {@link User} and includes additional information
 * such as account status and the creation timestamp.
 *
 * <p>This class serves as a base for user types that require authentication,
 * such as administrators or moderators.</p>
 */
public class AuthenticatedUser extends User {

    /**
     * The current status of the user (e.g., ACTIVE, REMOVED).
     */
    private UserStatusEnum status;

    /**
     * The timestamp when the user account was created.
     */
    private final Timestamp creationDate;

    /**
     * Constructs an {@code AuthenticatedUser} with all fields including ID.
     *
     * @param id            the unique ID of the user
     * @param username      the user's login username
     * @param password      the user's password (hashed or raw)
     * @param name          the user's first name
     * @param surname       the user's surname
     * @param status        the account status (e.g., ACTIVE, REMOVED)
     * @param creationDate  the date and time the account was created
     */
    public AuthenticatedUser(Integer id, String username, String password, String name, String surname, UserStatusEnum status, Timestamp creationDate) {
        super(id, username, password, name, surname);
        this.creationDate = creationDate;
        this.status = status;
    }

    /**
     * Constructs an {@code AuthenticatedUser} without an ID (for new accounts).
     *
     * @param username      the user's login username
     * @param password      the user's password
     * @param name          the user's first name
     * @param surname       the user's surname
     * @param status        the account status
     * @param creationDate  the account creation timestamp
     */
    public AuthenticatedUser(String username, String password, String name, String surname, UserStatusEnum status, Timestamp creationDate) {
        super(username, password, name, surname);
        this.creationDate = creationDate;
        this.status = status;
    }

    /**
     * Returns the timestamp when the user account was created.
     *
     * @return the creation timestamp
     */
    public Timestamp getCreationDate() {
        return creationDate;
    }

    /**
     * Returns the current status of the user.
     *
     * @return the user's account status
     */
    public UserStatusEnum getStatus() {
        return status;
    }

    /**
     * Sets the status of the user.
     *
     * @param status the new user status
     */
    public void setStatus(UserStatusEnum status) {
        this.status = status;
    }
}
