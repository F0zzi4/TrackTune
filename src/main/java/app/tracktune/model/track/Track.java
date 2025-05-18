package app.tracktune.model.track;

import java.sql.Timestamp;

public class Track {
    private final Integer id;
    private final String title;
    private final Timestamp creationDate;
    private final int userID;

    public Track(Integer id, String title, Timestamp creationDate, int userID) {
        this.id = id;
        this.title = title;
        this.creationDate = creationDate;
        this.userID = userID;
    }

    public Track(String title, Timestamp creationDate, int userID) {
        this.id = null;
        this.title = title;
        this.creationDate = creationDate;
        this.userID = userID;
    }

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Timestamp getCreationDate() {
        return creationDate;
    }

    public int getUserID() {
        return userID;
    }
}
