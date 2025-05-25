package app.tracktune.model.comments;

import java.sql.Time;
import java.sql.Timestamp;

public class Comment {
    private final int ID;
    private final String description;
    private final Time startTrackInterval;
    private final Time endTrackInterval;
    private final Timestamp creationDate;
    private final int userID;
    private final int trackID;

    public Comment(int ID, String description, Time startTrackInterval, Time endTrackInterval, Timestamp creationDate, int userID, int trackID){
        this.ID = ID;
        this.description = description;
        this.startTrackInterval = startTrackInterval;
        this.endTrackInterval = endTrackInterval;
        this.creationDate = creationDate;
        this.userID = userID;
        this.trackID = trackID;
    }

    public int getID() {
        return ID;
    }

    public String getDescription() {
        return description;
    }

    public Time getStartTrackInterval() {
        return startTrackInterval;
    }

    public Time getEndTrackInterval() {
        return endTrackInterval;
    }

    public Timestamp getCreationDate() {
        return creationDate;
    }

    public int getUserID() {
        return userID;
    }

    public int getTrackID() {
        return trackID;
    }
}
