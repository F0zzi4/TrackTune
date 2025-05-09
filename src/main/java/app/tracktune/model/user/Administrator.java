package app.tracktune.model.user;

import java.sql.Timestamp;

public class Administrator extends AuthenticatedUser {
    public Administrator(String username, String password, String name, String surname, UserStatusEnum status, Timestamp creationDate) {
        super(username, password, name, surname, status, creationDate);
    }
}
