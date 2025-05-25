package app.tracktune.model.resource;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

public class MultimediaResource extends Resource {
    private final Time duration;
    private final String location;
    private final Date resourceDate;

    public MultimediaResource(
            Integer id,
            ResourceTypeEnum type,
            byte[] data,
            Timestamp creationDate,
            boolean isMultimedia,
            Time duration,
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
            Time duration,
            String location,
            Date resourceDate,
            int trackID
    ) {
        super(null, type, data, creationDate, isMultimedia, trackID);
        this.duration = duration;
        this.location = location;
        this.resourceDate = resourceDate;
    }

    public Time getDuration() {
        return duration;
    }

    public String getLocation() {
        return location;
    }

    public Date getResourceDate() {
        return resourceDate;
    }
}