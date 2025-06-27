package app.tracktune.model.track;

import app.tracktune.Main;
import app.tracktune.exceptions.SQLiteException;
import app.tracktune.interfaces.DAO;
import app.tracktune.utils.DatabaseManager;
import app.tracktune.utils.Strings;
import app.tracktune.view.ViewManager;
import javafx.scene.control.Alert;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Data Access Object (DAO) for the {@link Track} entity.
 * This class handles all database operations related to tracks,
 * including CRUD operations and advanced queries involving related entities
 * such as authors, instruments, genres, and resources.
 */
public class TrackDAO implements DAO<Track> {
    private final DatabaseManager dbManager;

    // SQL column names
    private static final String ID = "ID";
    private static final String TITLE = "title";
    private static final String CREATION_DATE = "creationDate";
    private static final String USER_ID = "userID";

    // SQL statements
    private static final String INSERT_TRACK_STMT = """
        INSERT INTO Tracks (title, creationDate, userID)
        VALUES (?, ?, ?)
    """;

    private static final String UPDATE_TRACK_STMT = """
        UPDATE Tracks
        SET title = ?,
        creationDate = ?,
        userId = ?
        WHERE ID = ?
    """;

    private static final String DELETE_TRACK_STMT = """
        DELETE FROM Tracks WHERE ID = ?
    """;

    private static final String GET_ALL_TRACKS_STMT = """
        SELECT *
        FROM Tracks
    """;

    private static final String GET_TRACK_BY_ID_STMT = """
        SELECT *
        FROM Tracks
        WHERE ID = ?
    """;

    private static final String GET_ALL_TRACK_BY_AUTHOR_ID_STMT = """
        SELECT t.*
        FROM Tracks t
        JOIN TracksAuthors ta ON ta.trackID = t.ID
        WHERE ta.authorID = ?;
    """;

    private static final String GET_ALL_TRACK_BY_GENRE_ID_STMT = """
        SELECT t.*
        FROM Tracks t
        JOIN TracksGenres tg ON tg.trackID = t.ID
        WHERE tg.genreID = ?;
    """;

    private static final String GET_ALL_TRACK_BY_INSTRUMENT_ID_STMT = """
        SELECT t.*
        FROM Tracks t
        JOIN TracksInstruments ti ON ti.trackID = t.ID
        WHERE ti.instrumentID = ?;
    """;

    private static final String GET_TRACK_BY_RESOURCE_ID_STMT = """
        SELECT t.*
        FROM Tracks t
        JOIN Resources r ON r.trackID = t.ID
        WHERE r.ID = ?
    """;

    private static final String GET_TRACK_BY_TITLE = """
        SELECT *
        FROM Tracks
        WHERE title = ?
    """;

    /**
     * Default constructor using the global {@link Main#dbManager}.
     */
    public TrackDAO() {
        dbManager = Main.dbManager;
    }

    /**
     * Constructor with a specific {@link DatabaseManager}.
     *
     * @param dbManager the database manager instance to use
     */
    public TrackDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Inserts a new track into the database.
     *
     * @param track the track to insert
     * @return the generated ID of the new track
     */
    @Override
    public Integer insert(Track track) {
        boolean success = dbManager.executeUpdate(INSERT_TRACK_STMT,
                track.getTitle(),
                track.getCreationDate(),
                track.getUserID()
        );

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }
        return dbManager.getLastInsertId();
    }

    /**
     * Updates a track in the database by its ID.
     *
     * @param track the updated track object
     * @param id    the ID of the track to update
     */
    @Override
    public void updateById(Track track, int id) {
        boolean success = dbManager.executeUpdate(UPDATE_TRACK_STMT,
                track.getTitle(),
                track.getCreationDate(),
                track.getUserID(),
                id
        );

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }
    }

    /**
     * Deletes a track from the database by its ID.
     *
     * @param id the ID of the track to delete
     */
    @Override
    public void deleteById(int id) {
        try {
            dbManager.executeUpdate(DELETE_TRACK_STMT, id);
        } catch (SQLiteException ex) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.DELETE, Strings.ERR_DELETE_TRACK, Alert.AlertType.ERROR);
            System.err.println(ex.getMessage());
        }
    }

    /**
     * Retrieves a track by its ID.
     *
     * @param id the ID of the track
     * @return the track with the given ID, or null if not found
     */
    @Override
    public Track getById(int id) {
        AtomicReference<Track> result = new AtomicReference<>();

        boolean success = dbManager.executeQuery(GET_TRACK_BY_ID_STMT,
                rs -> {
                    if (rs.next()) {
                        result.set(mapResultSetToEntity(rs));
                        return true;
                    }
                    return false;
                }, id);

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }

        return result.get();
    }

    /**
     * Retrieves a track by its title.
     *
     * @param title the title of the track
     * @return the track with the specified title, or null if not found
     */
    public Track getByTitle(String title) {
        AtomicReference<Track> result = new AtomicReference<>();

        boolean success = dbManager.executeQuery(
                GET_TRACK_BY_TITLE,
                rs -> {
                    if (rs.next()) {
                        result.set(mapResultSetToEntity(rs));
                        return true;
                    }
                    return false;
                },
                title
        );

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }

        return result.get();
    }

    /**
     * Retrieves all tracks associated with a given author ID.
     *
     * @param id the author ID
     * @return a list of tracks by the author
     */
    public List<Track> getAllByAuthorId(int id) {
        List<Track> tracks = new ArrayList<>();

        dbManager.executeQuery(GET_ALL_TRACK_BY_AUTHOR_ID_STMT,
                rs -> {
                    while (rs.next()) {
                        tracks.add(mapResultSetToEntity(rs));
                    }
                    return null;
                }, id);

        return tracks;
    }

    public List<Track> getAllByGenreId(int id) {
        List<Track> tracks = new ArrayList<>();

        dbManager.executeQuery(GET_ALL_TRACK_BY_GENRE_ID_STMT,
                rs -> {
                    while (rs.next()) {
                        tracks.add(mapResultSetToEntity(rs));
                    }
                    return null;
                }, id);

        return tracks;
    }

    /**
     * Retrieves all tracks with a specific ID (usually returns at most one).
     *
     * @param id the track ID
     * @return a list containing the matching track(s)
     */
    public List<Track> getAllByTrackId(int id) {
        List<Track> tracks = new ArrayList<>();

        dbManager.executeQuery(GET_TRACK_BY_ID_STMT,
                rs -> {
                    while (rs.next()) {
                        tracks.add(mapResultSetToEntity(rs));
                    }
                    return null;
                }, id);

        return tracks;
    }

    /**
     * Retrieves all tracks associated with a given instrument ID.
     *
     * @param id the instrument ID
     * @return a list of tracks using the instrument
     */
    public List<Track> getAllByInstrumentId(int id) {
        List<Track> tracks = new ArrayList<>();

        dbManager.executeQuery(GET_ALL_TRACK_BY_INSTRUMENT_ID_STMT,
                rs -> {
                    while (rs.next()) {
                        tracks.add(mapResultSetToEntity(rs));
                    }
                    return null;
                }, id);

        return tracks;
    }

    /**
     * Retrieves the track associated with a given resource ID.
     *
     * @param id the resource ID
     * @return the track linked to the resource, or null if not found
     */
    public Track getTrackByResourceId(int id) {
        AtomicReference<Track> track = new AtomicReference<>();

        dbManager.executeQuery(GET_TRACK_BY_RESOURCE_ID_STMT,
                rs -> {
                    if (rs.next()) {
                        track.set(mapResultSetToEntity(rs));
                    }
                    return null;
                }, id);

        return track.get();
    }

    /**
     * Retrieves all tracks from the database.
     *
     * @return a list of all tracks
     */
    @Override
    public List<Track> getAll() {
        List<Track> tracks = new ArrayList<>();

        dbManager.executeQuery(GET_ALL_TRACKS_STMT,
                rs -> {
                    while (rs.next()) {
                        tracks.add(mapResultSetToEntity(rs));
                    }
                    return null;
                });

        return tracks;
    }

    /**
     * Converts a {@link ResultSet} row into a {@link Track} object.
     *
     * @param rs the result set
     * @return a new Track instance populated with data from the result set
     * @throws SQLException if a database access error occurs
     */
    private Track mapResultSetToEntity(ResultSet rs) throws SQLException {
        Integer id = rs.getInt(ID);
        String title = rs.getString(TITLE);
        Timestamp creationDate = rs.getTimestamp(CREATION_DATE);
        int userID = rs.getInt(USER_ID);
        return new Track(id, title, creationDate, userID);
    }
}
