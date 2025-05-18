package app.tracktune.model.track;

public class TrackInstrument {
    private final Integer id;
    private final int trackId;
    private final int instrumentId;

    public TrackInstrument(Integer id, int trackId, int instrumentId) {
        this.id = id;
        this.trackId = trackId;
        this.instrumentId = instrumentId;
    }

    public TrackInstrument(int trackId, int instrumentId) {
        this.id = null;
        this.trackId = trackId;
        this.instrumentId = instrumentId;
    }

    public Integer getId() {
        return id;
    }

    public int getTrackId() {
        return trackId;
    }

    public int getInstrumentId() {
        return instrumentId;
    }
}
