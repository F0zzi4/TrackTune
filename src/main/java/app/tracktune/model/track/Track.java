package app.tracktune.model.track;

import java.sql.Timestamp;

/**
 * Represents a musical track entity with basic metadata.
 * <p>
 * Each Track has a unique identifier (nullable for new tracks before insertion),
 * a title, creation timestamp, and an associated user ID who owns the track.
 */
public class Track {
    private final Integer id;
    private final String title;
    private final Timestamp creationDate;
    private final int userID;

    /**
     * Constructs a Track with an explicit ID.
     *
     * @param id the unique identifier of the track (can be null if not yet persisted)
     * @param title the title of the track
     * @param creationDate the creation timestamp of the track
     * @param userID the ID of the user owning the track
     */
    public Track(Integer id, String title, Timestamp creationDate, int userID) {
        this.id = id;
        this.title = title;
        this.creationDate = creationDate;
        this.userID = userID;
    }

    /**
     * Constructs a Track without an ID, used for new tracks before being saved.
     *
     * @param title the title of the track
     * @param creationDate the creation timestamp of the track
     * @param userID the ID of the user owning the track
     */
    public Track(String title, Timestamp creationDate, int userID) {
        this(null, title, creationDate, userID);
    }

    /**
     * Returns the unique identifier of the track.
     *
     * @return the track ID, or null if not persisted yet
     */
    public Integer getId() {
        return id;
    }

    /**
     * Returns the title of the track.
     *
     * @return the track title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the creation timestamp of the track.
     *
     * @return the creation date
     */
    public Timestamp getCreationDate() {
        return creationDate;
    }

    /**
     * Returns the user ID of the track owner.
     *
     * @return the user ID
     */
    public int getUserID() {
        return userID;
    }

    /**
     * Returns a string representation of the Track.
     *
     * @return the track title as string
     */
    @Override
    public String toString() {
        return title;
    }
}
