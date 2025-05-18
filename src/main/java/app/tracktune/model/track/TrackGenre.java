package app.tracktune.model.track;

public class TrackGenre {
    private final Integer id;
    private final int trackId;
    private final int genreId;

    public TrackGenre(int id, int trackId, int genreId) {
        this.id = id;
        this.trackId = trackId;
        this.genreId = genreId;
    }

    public TrackGenre(int trackId, int genreId) {
        this.id = null;
        this.trackId = trackId;
        this.genreId = genreId;
    }

    public Integer getId() {
        return id;
    }

    public int getTrackId() {
        return trackId;
    }

    public int getGenreId() {
        return genreId;
    }
}
