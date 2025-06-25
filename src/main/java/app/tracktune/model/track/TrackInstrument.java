package app.tracktune.model.track;

/**
 * Represents an association between a track and an instrument.
 * This class models the many-to-many relationship between tracks and instruments.
 */
public class TrackInstrument {
    private final Integer id;
    private final int trackId;
    private final int instrumentId;

    /**
     * Constructs a {@code TrackInstrument} with a known ID.
     *
     * @param id           the ID of the association
     * @param trackId      the ID of the track
     * @param instrumentId the ID of the instrument
     */
    public TrackInstrument(Integer id, int trackId, int instrumentId) {
        this.id = id;
        this.trackId = trackId;
        this.instrumentId = instrumentId;
    }

    /**
     * Constructs a {@code TrackInstrument} without specifying the ID.
     * This constructor is typically used before inserting a new record into the database.
     *
     * @param trackId      the ID of the track
     * @param instrumentId the ID of the instrument
     */
    public TrackInstrument(int trackId, int instrumentId) {
        this.id = null;
        this.trackId = trackId;
        this.instrumentId = instrumentId;
    }

    /**
     * Returns the ID of the track-instrument association.
     *
     * @return the ID, or {@code null} if not yet persisted
     */
    public Integer getId() {
        return id;
    }

    /**
     * Returns the track ID.
     *
     * @return the ID of the associated track
     */
    public int getTrackId() {
        return trackId;
    }

    /**
     * Returns the instrument ID.
     *
     * @return the ID of the associated instrument
     */
    public int getInstrumentId() {
        return instrumentId;
    }
}
