package app.tracktune.model.user;

import java.sql.Timestamp;

public class Administrator extends AuthenticatedUser {
    public Administrator(Integer id, String username, String password, String name, String surname, UserStatusEnum status, Timestamp creationDate) {
        super(id, username, password, name, surname, status, creationDate);
    }

    public Administrator(String username, String password, String name, String surname, UserStatusEnum status, Timestamp creationDate) {
        super(username, password, name, surname, status, creationDate);
    }
}
