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
import app.tracktune.utils.DatabaseManager;

/**
 * Provides access to all DAO (Data Access Object) instances used in the application.
 * Each DAO is instantiated with a shared DatabaseManager instance.
 * <p>
 * This class acts as a centralized factory for DAO objects, promoting
 * easy management and consistency across data access layers.
 */
public class DAOProvider {

    private final AuthorDAO authorDAO;
    private final CommentDAO commentDAO;
    private final GenreDAO genreDAO;
    private final MusicalInstrumentDAO musicalInstrumentDAO;
    private final ResourceDAO resourceDAO;
    private final TrackDAO trackDAO;
    private final TrackAuthorDAO trackAuthorDAO;
    private final TrackGenreDAO trackGenreDAO;
    private final TrackInstrumentDAO trackInstrumentDAO;
    private final PendingUserDAO pendingUserDAO;
    private final UserDAO userDAO;

    /**
     * Constructs a DAOProvider and initializes all DAO instances
     * using a singleton DatabaseManager.
     */
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

    /**
     * Gets the AuthorDAO instance.
     * @return the AuthorDAO
     */
    public AuthorDAO getAuthorDAO() {
        return authorDAO;
    }

    /**
     * Gets the TrackAuthorDAO instance.
     * @return the TrackAuthorDAO
     */
    public TrackAuthorDAO getTrackAuthorDAO() {
        return trackAuthorDAO;
    }

    /**
     * Gets the CommentDAO instance.
     * @return the CommentDAO
     */
    public CommentDAO getCommentDAO() {
        return commentDAO;
    }

    /**
     * Gets the GenreDAO instance.
     * @return the GenreDAO
     */
    public GenreDAO getGenreDAO() {
        return genreDAO;
    }

    /**
     * Gets the MusicalInstrumentDAO instance.
     * @return the MusicalInstrumentDAO
     */
    public MusicalInstrumentDAO getMusicalInstrumentDAO() {
        return musicalInstrumentDAO;
    }

    /**
     * Gets the ResourceDAO instance.
     * @return the ResourceDAO
     */
    public ResourceDAO getResourceDAO() {
        return resourceDAO;
    }

    /**
     * Gets the TrackDAO instance.
     * @return the TrackDAO
     */
    public TrackDAO getTrackDAO() {
        return trackDAO;
    }

    /**
     * Gets the TrackGenreDAO instance.
     * @return the TrackGenreDAO
     */
    public TrackGenreDAO getTrackGenreDAO() {
        return trackGenreDAO;
    }

    /**
     * Gets the TrackInstrumentDAO instance.
     * @return the TrackInstrumentDAO
     */
    public TrackInstrumentDAO getTrackInstrumentDAO() {
        return trackInstrumentDAO;
    }

    /**
     * Gets the PendingUserDAO instance.
     * @return the PendingUserDAO
     */
    public PendingUserDAO getPendingUserDAO() {
        return pendingUserDAO;
    }

    /**
     * Gets the UserDAO instance.
     * @return the UserDAO
     */
    public UserDAO getUserDAO() {
        return userDAO;
    }
}
