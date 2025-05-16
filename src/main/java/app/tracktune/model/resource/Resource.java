package app.tracktune.model.resource;

import java.sql.Blob;
import java.sql.Timestamp;

public class Resource {
    private final ResourceTypeEnum type;
    private final Blob data;
    private final Timestamp creationDate;
    private final boolean isMultimedia;
    private final int trackID;

    public Resource(ResourceTypeEnum type, Blob data, Timestamp creationDate, boolean isMultimedia, int trackID) {
        this.type = type;
        this.data = data;
        this.creationDate = creationDate;
        this.isMultimedia = isMultimedia;
        this.trackID = trackID;
    }

    public ResourceTypeEnum getType() {
        return type;
    }

    public Blob getData() {
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
}
