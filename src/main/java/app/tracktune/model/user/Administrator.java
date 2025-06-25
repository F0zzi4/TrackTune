package app.tracktune.model.user;

import java.sql.Timestamp;

/**
 * Represents an administrator user of the TrackTune system.
 * Inherits from {@link AuthenticatedUser} and has all its attributes and behaviors.
 *
 * <p>Administrator accounts typically have elevated privileges compared to regular users.</p>
 */
public class Administrator extends AuthenticatedUser {

    /**
     * Constructs an {@code Administrator} with a specified ID and full user details.
     *
     * @param id            the unique ID of the administrator
     * @param username      the administrator's username
     * @param password      the administrator's password
     * @param name          the administrator's first name
     * @param surname       the administrator's surname
     * @param status        the current account status
     * @param creationDate  the date and time the account was created
     */
    public Administrator(Integer id, String username, String password, String name, String surname, UserStatusEnum status, Timestamp creationDate) {
        super(id, username, password, name, surname, status, creationDate);
    }

    /**
     * Constructs a new {@code Administrator} without an ID (for creation before persistence).
     *
     * @param username      the administrator's username
     * @param password      the administrator's password
     * @param name          the administrator's first name
     * @param surname       the administrator's surname
     * @param status        the current account status
     * @param creationDate  the date and time the account was created
     */
    public Administrator(String username, String password, String name, String surname, UserStatusEnum status, Timestamp creationDate) {
        super(username, password, name, surname, status, creationDate);
    }
}
