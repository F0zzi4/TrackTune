package app.tracktune.model.track;

import app.tracktune.model.genre.Genre;

import java.sql.Timestamp;
import java.util.Set;

public class Track {
    private final Integer id;
    private final String title;
    private final Timestamp creationDate;
    private final int userID;

    private Set<Genre> genres;

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

    @Override
    public String toString() {
        return title;
    }
}
