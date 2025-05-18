package app.tracktune.model.track;

public class TrackAuthor {
    private final Integer id;
    private final int trackId;
    private final int authorId;

    public TrackAuthor(int id, int trackId, int authorId) {
        this.id = id;
        this.trackId = trackId;
        this.authorId = authorId;
    }

    public TrackAuthor(int trackId, int authorId) {
        this.id = null;
        this.trackId = trackId;
        this.authorId = authorId;
    }

    public Integer getId() {
        return id;
    }

    public int getTrackId() {
        return trackId;
    }

    public int getAuthorId() {
        return authorId;
    }
}
