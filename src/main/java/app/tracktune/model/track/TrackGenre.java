package app.tracktune.model.track;

/**
 * Represents the association between a {@code Track} and a {@code Genre}.
 * This class is typically used to model a many-to-many relationship in the database,
 * where each track can have multiple genres and each genre can belong to multiple tracks.
 */
public class TrackGenre {
    private final Integer id;
    private final int trackId;
    private final int genreId;

    /**
     * Constructs a {@code TrackGenre} with a specified ID.
     * Typically used when loading existing records from the database.
     *
     * @param id       the ID of the association (primary key)
     * @param trackId  the ID of the track
     * @param genreId  the ID of the genre
     */
    public TrackGenre(int id, int trackId, int genreId) {
        this.id = id;
        this.trackId = trackId;
        this.genreId = genreId;
    }

    /**
     * Constructs a {@code TrackGenre} without an ID.
     * Typically used when creating a new association to be inserted into the database.
     *
     * @param trackId  the ID of the track
     * @param genreId  the ID of the genre
     */
    public TrackGenre(int trackId, int genreId) {
        this.id = null;
        this.trackId = trackId;
        this.genreId = genreId;
    }

    /**
     * Returns the ID of this association, or {@code null} if it has not been persisted yet.
     *
     * @return the ID of the association
     */
    public Integer getId() {
        return id;
    }

    /**
     * Returns the ID of the associated track.
     *
     * @return the track ID
     */
    public int getTrackId() {
        return trackId;
    }

    /**
     * Returns the ID of the associated genre.
     *
     * @return the genre ID
     */
    public int getGenreId() {
        return genreId;
    }
}
