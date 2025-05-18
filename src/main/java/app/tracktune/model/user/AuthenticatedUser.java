package app.tracktune.model.user;

import java.sql.Timestamp;

public class AuthenticatedUser extends User {
    private final UserStatusEnum status;
    private final Timestamp creationDate;

    public AuthenticatedUser(Integer id, String username, String password, String name, String surname, UserStatusEnum status, Timestamp creationDate) {
        super(id, username, password, name, surname);
        this.creationDate = creationDate;
        this.status = status;
    }

    public AuthenticatedUser(String username, String password, String name, String surname, UserStatusEnum status, Timestamp creationDate) {
        super(username, password, name, surname);
        this.creationDate = creationDate;
        this.status = status;
    }

    /**
     * Get the creation timestamp
     * @return Creation timestamp
     */
    public Timestamp getCreationDate() {
        return creationDate;
    }

    /**
     * Get the user status
     * @return User status
     */
    public UserStatusEnum getStatus() {
        return status;
    }
}
