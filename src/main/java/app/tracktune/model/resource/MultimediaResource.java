package app.tracktune.model.resource;

import java.sql.Blob;
import java.sql.Date;
import java.sql.Timestamp;

public class MultimediaResource extends Resource {
    private final int duration;
    private final String location;
    private final Date resourceDate;

    public MultimediaResource(
            Integer id,
            ResourceTypeEnum type,
            byte[] data,
            Timestamp creationDate,
            boolean isMultimedia,
            int duration,
            String location,
            Date resourceDate,
            int trackID
    ) {
        super(id, type, data, creationDate, isMultimedia, trackID);
        this.duration = duration;
        this.location = location;
        this.resourceDate = resourceDate;
    }

    public MultimediaResource(
            ResourceTypeEnum type,
            byte[] data,
            Timestamp creationDate,
            boolean isMultimedia,
            int duration,
            String location,
            Date resourceDate,
            int trackID
    ) {
        super(null, type, data, creationDate, isMultimedia, trackID);
        this.duration = duration;
        this.location = location;
        this.resourceDate = resourceDate;
    }

    public int getDuration() {
        return duration;
    }

    public String getLocation() {
        return location;
    }

    public Date getResourceDate() {
        return resourceDate;
    }
}