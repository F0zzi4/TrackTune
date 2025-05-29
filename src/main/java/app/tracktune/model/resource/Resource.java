package app.tracktune.model.resource;

import java.sql.Timestamp;

public class Resource {
    private final Integer id;
    private final ResourceTypeEnum type;
    private final byte[] data;
    private final Timestamp creationDate;
    private final boolean isMultimedia;
    private final int trackID;
    private final int userID;

    public Resource(Integer id, ResourceTypeEnum type, byte[] data, Timestamp creationDate, boolean isMultimedia, int trackID, int  userID) {
        this.id = id;
        this.type = type;
        this.data = data;
        this.creationDate = creationDate;
        this.isMultimedia = isMultimedia;
        this.trackID = trackID;
        this.userID = userID;
    }

    public Resource(ResourceTypeEnum type, byte[] data, Timestamp creationDate, boolean isMultimedia, int trackID, int userID) {
        this(null, type, data, creationDate, isMultimedia, trackID, userID);
    }

    public Integer getId() {
        return id;
    }

    public ResourceTypeEnum getType() {
        return type;
    }

    public byte[] getData() {
        return data;
    }

    public Timestamp getCreationDate() {
        return creationDate;
    }

    public boolean isMultimedia() {
        return isMultimedia;
    }

    public int getTrackID() {
        return trackID;
    }

    public int getUserID() {return userID; }
}