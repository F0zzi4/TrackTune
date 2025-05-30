package app.tracktune.model.comments;
import java.sql.Timestamp;

public class Comment {
    private final Integer ID;
    private final String description;
    private final int startTrackInterval;
    private final int endTrackInterval;
    private final Timestamp creationDate;
    private final int userID;
    private final int trackID;

    public Comment(Integer ID, String description, int startTrackInterval, int endTrackInterval, Timestamp creationDate, int userID, int trackID){
        this.ID = ID;
        this.description = description;
        this.startTrackInterval = startTrackInterval;
        this.endTrackInterval = endTrackInterval;
        this.creationDate = creationDate;
        this.userID = userID;
        this.trackID = trackID;
    }

    public Comment(String description, Timestamp creationDate, int userID, int trackID){
        this.ID = null;
        this.description = description;
        this.startTrackInterval = 0;
        this.endTrackInterval = 0;
        this.creationDate = creationDate;
        this.userID = userID;
        this.trackID = trackID;
    }

    public Comment(String description, int startTrackInterval, int endTrackInterval, Timestamp creationDate, int userID, int trackID){
        this.ID = null;
        this.description = description;
        this.startTrackInterval = startTrackInterval;
        this.endTrackInterval = endTrackInterval;
        this.creationDate = creationDate;
        this.userID = userID;
        this.trackID = trackID;
    }

    public Integer getID() {
        return ID;
    }

    public String getDescription() {
        return description;
    }

    public int getStartTrackInterval() {
        return startTrackInterval;
    }

    public int getEndTrackInterval() {
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
