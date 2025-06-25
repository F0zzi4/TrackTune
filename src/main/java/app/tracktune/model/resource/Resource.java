package app.tracktune.model.resource;

import java.sql.Timestamp;

/**
 * Represents a general resource entity associated with a track and a user.
 * This class stores metadata and binary data for a resource, which can be multimedia or other types.
 */
public class Resource {
    private final Integer id;
    private final ResourceTypeEnum type;
    private final byte[] data;
    private final Timestamp creationDate;
    private final boolean isMultimedia;
    private final boolean isAuthor;
    private final int trackID;
    private final int userID;

    /**
     * Constructs a Resource with the specified fields including an explicit ID.
     *
     * @param id            the unique identifier of the resource, may be null if not assigned yet
     * @param type          the type of the resource (see {@link ResourceTypeEnum})
     * @param data          the binary content of the resource
     * @param creationDate  the timestamp when the resource was created
     * @param isMultimedia  true if the resource is multimedia, false otherwise
     * @param isAuthor      true if the resource is related to an author, false otherwise
     * @param trackID       the identifier of the track this resource is linked to
     * @param userID        the identifier of the user who owns or created the resource
     */
    public Resource(Integer id, ResourceTypeEnum type, byte[] data, Timestamp creationDate, boolean isMultimedia, boolean isAuthor, int trackID, int  userID) {
        this.id = id;
        this.type = type;
        this.data = data;
        this.creationDate = creationDate;
        this.isMultimedia = isMultimedia;
        this.isAuthor = isAuthor;
        this.trackID = trackID;
        this.userID = userID;
    }

    /**
     * Constructs a Resource without specifying an ID (e.g., for new resources before database insertion).
     *
     * @param type          the type of the resource (see {@link ResourceTypeEnum})
     * @param data          the binary content of the resource
     * @param creationDate  the timestamp when the resource was created
     * @param isMultimedia  true if the resource is multimedia, false otherwise
     * @param isAuthor      true if the resource is related to an author, false otherwise
     * @param trackID       the identifier of the track this resource is linked to
     * @param userID        the identifier of the user who owns or created the resource
     */
    public Resource(ResourceTypeEnum type, byte[] data, Timestamp creationDate, boolean isMultimedia, boolean isAuthor, int trackID, int userID) {
        this(null, type, data, creationDate, isMultimedia, isAuthor, trackID, userID);
    }

    /**
     * Returns the unique identifier of the resource.
     *
     * @return the resource ID, or null if not yet assigned
     */
    public Integer getId() {
        return id;
    }

    /**
     * Returns the type of the resource.
     *
     * @return the resource type enum value
     */
    public ResourceTypeEnum getType() {
        return type;
    }

    /**
     * Returns the binary data of the resource.
     *
     * @return a byte array containing the resource data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Returns the timestamp when the resource was created.
     *
     * @return the creation date as a {@link Timestamp}
     */
    public Timestamp getCreationDate() {
        return creationDate;
    }

    /**
     * Checks if this resource is multimedia.
     *
     * @return true if the resource is multimedia, false otherwise
     */
    public boolean isMultimedia() {
        return isMultimedia;
    }

    /**
     * Checks if this resource is related to an author.
     *
     * @return true if the resource is an author resource, false otherwise
     */
    public boolean isAuthor() {
        return isAuthor;
    }

    /**
     * Returns the ID of the track associated with this resource.
     *
     * @return the track ID
     */
    public int getTrackID() {
        return trackID;
    }

    /**
     * Returns the ID of the user who owns or created this resource.
     *
     * @return the user ID
     */
    public int getUserID() {
        return userID;
    }
}
