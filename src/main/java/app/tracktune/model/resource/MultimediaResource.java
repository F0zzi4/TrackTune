package app.tracktune.model.resource;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Represents a multimedia resource associated with a track and user.
 * Extends the base {@link Resource} class by adding location and resource date.
 */
public class MultimediaResource extends Resource {
    private final String location;
    private final Date resourceDate;

    /**
     * Constructs a MultimediaResource with a specified ID.
     *
     * @param id            the unique identifier of the resource, can be null for new entities
     * @param type          the type of the resource (see {@link ResourceTypeEnum})
     * @param data          the binary data of the resource
     * @param creationDate  the timestamp when the resource was created
     * @param isMultimedia  flag indicating if the resource is multimedia
     * @param location      the location/path of the multimedia resource
     * @param resourceDate  the date associated with the resource (e.g., creation or capture date)
     * @param isAuthor      flag indicating if the resource is related to an author
     * @param trackID       the ID of the track this resource is linked to
     * @param userID        the ID of the user who owns or created the resource
     */
    public MultimediaResource(
            Integer id,
            ResourceTypeEnum type,
            byte[] data,
            Timestamp creationDate,
            boolean isMultimedia,
            String location,
            Date resourceDate,
            boolean isAuthor,
            int trackID,
            int userID
    ) {
        super(id, type, data, creationDate, isMultimedia, isAuthor, trackID, userID);
        this.location = location;
        this.resourceDate = resourceDate;
    }

    /**
     * Constructs a new MultimediaResource without an ID (for insertion).
     *
     * @param type          the type of the resource (see {@link ResourceTypeEnum})
     * @param data          the binary data of the resource
     * @param creationDate  the timestamp when the resource was created
     * @param isMultimedia  flag indicating if the resource is multimedia
     * @param location      the location/path of the multimedia resource
     * @param resourceDate  the date associated with the resource (e.g., creation or capture date)
     * @param isAuthor      flag indicating if the resource is related to an author
     * @param trackID       the ID of the track this resource is linked to
     * @param userID        the ID of the user who owns or created the resource
     */
    public MultimediaResource(
            ResourceTypeEnum type,
            byte[] data,
            Timestamp creationDate,
            boolean isMultimedia,
            String location,
            Date resourceDate,
            boolean isAuthor,
            int trackID,
            int userID
    ) {
        super(null, type, data, creationDate, isMultimedia, isAuthor, trackID, userID);
        this.location = location;
        this.resourceDate = resourceDate;
    }

    /**
     * Returns the location or path of this multimedia resource.
     *
     * @return the resource location as a String
     */
    public String getLocation() {
        return location;
    }

    /**
     * Returns the date associated with this resource (e.g., the date of creation or capture).
     *
     * @return the resource date as {@link java.sql.Date}
     */
    public Date getResourceDate() {
        return resourceDate;
    }
}
