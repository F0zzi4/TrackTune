package app.tracktune.model;

import app.tracktune.model.author.AuthorDAO;
import app.tracktune.model.comments.CommentDAO;
import app.tracktune.model.genre.GenreDAO;
import app.tracktune.model.musicalInstrument.MusicalInstrumentDAO;
import app.tracktune.model.resource.ResourceDAO;
import app.tracktune.model.track.TrackAuthorDAO;
import app.tracktune.model.track.TrackDAO;
import app.tracktune.model.track.TrackGenreDAO;
import app.tracktune.model.track.TrackInstrumentDAO;
import app.tracktune.model.user.PendingUserDAO;
import app.tracktune.model.user.UserDAO;

public class DAOProvider {
    private final AuthorDAO authorDAO;
    private final CommentDAO commentDAO;
    private final GenreDAO genreDAO;
    private final MusicalInstrumentDAO musicalInstrumentDAO;
    private final ResourceDAO resourceDAO;
    private final TrackDAO trackDAO;
    private final TrackAuthorDAO  trackAuthorDAO;
    private final TrackGenreDAO trackGenreDAO;
    private final TrackInstrumentDAO trackInstrumentDAO;
    private final PendingUserDAO pendingUserDAO;
    private final UserDAO userDAO;


    public DAOProvider() {
        DatabaseManager db = DatabaseManager.getInstance();
        this.authorDAO = new AuthorDAO(db);
        this.commentDAO = new CommentDAO(db);
        this.genreDAO = new GenreDAO(db);
        this.musicalInstrumentDAO = new MusicalInstrumentDAO(db);
        this.resourceDAO = new ResourceDAO(db);
        this.trackDAO = new TrackDAO(db);
        this.trackAuthorDAO = new TrackAuthorDAO(db);
        this.trackGenreDAO = new TrackGenreDAO(db);
        this.trackInstrumentDAO = new TrackInstrumentDAO(db);
        this.pendingUserDAO = new PendingUserDAO(db);
        this.userDAO = new UserDAO(db);
    }

    public AuthorDAO getAuthorDAO() {
        return authorDAO;
    }

    public TrackAuthorDAO getTrackAuthorDAO() {
        return trackAuthorDAO;
    }

    public CommentDAO getCommentDAO() {
        return commentDAO;
    }

    public GenreDAO getGenreDAO() {
        return genreDAO;
    }

    public MusicalInstrumentDAO getMusicalInstrumentDAO() {
        return musicalInstrumentDAO;
    }

    public ResourceDAO getResourceDAO() {
        return resourceDAO;
    }

    public TrackDAO getTrackDAO() {
        return trackDAO;
    }

    public TrackGenreDAO getTrackGenreDAO() {
        return trackGenreDAO;
    }

    public TrackInstrumentDAO getTrackInstrumentDAO() {
        return trackInstrumentDAO;
    }

    public PendingUserDAO getPendingUserDAO() {
        return pendingUserDAO;
    }

    public UserDAO getUserDAO() {
        return userDAO;
    }
}

