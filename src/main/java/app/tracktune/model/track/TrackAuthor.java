package app.tracktune.model.track;

/**
 * Represents an association between a track and an author.
 * <p>
 * This class models the many-to-many relationship in the database
 * where each track can have multiple authors and each author can contribute to multiple tracks.
 */
public class TrackAuthor {
    private final Integer id;
    private final int trackId;
    private final int authorId;

    /**
     * Constructs a TrackAuthor with an existing ID (e.g., when loading from the database).
     *
     * @param id       the unique ID of the association
     * @param trackId  the ID of the associated track
     * @param authorId the ID of the associated author
     */
    public TrackAuthor(int id, int trackId, int authorId) {
        this.id = id;
        this.trackId = trackId;
        this.authorId = authorId;
    }

    /**
     * Constructs a TrackAuthor without an ID (e.g., when creating a new association).
     *
     * @param trackId  the ID of the associated track
     * @param authorId the ID of the associated author
     */
    public TrackAuthor(int trackId, int authorId) {
        this.id = null;
        this.trackId = trackId;
        this.authorId = authorId;
    }

    /**
     * Returns the ID of the TrackAuthor association.
     *
     * @return the ID or {@code null} if not yet stored in the database
     */
    public Integer getId() {
        return id;
    }

    /**
     * Returns the ID of the track in this association.
     *
     * @return the track ID
     */
    public int getTrackId() {
        return trackId;
    }

    /**
     * Returns the ID of the author in this association.
     *
     * @return the author ID
     */
    public int getAuthorId() {
        return authorId;
    }
}
