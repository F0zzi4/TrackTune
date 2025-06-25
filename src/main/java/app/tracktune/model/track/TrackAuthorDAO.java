package app.tracktune.model.track;

import app.tracktune.Main;
import app.tracktune.exceptions.SQLiteException;
import app.tracktune.interfaces.DAO;
import app.tracktune.utils.DatabaseManager;
import app.tracktune.utils.Strings;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Data Access Object (DAO) for managing associations between Tracks and Authors.
 * <p>
 * This class provides CRUD operations for the {@link TrackAuthor} entity,
 * which represents a many-to-many relationship between tracks and authors in the database.
 * It communicates with the "TracksAuthors" table.
 */
public class TrackAuthorDAO implements DAO<TrackAuthor> {
    private final DatabaseManager dbManager;

    // Column names
    private static final String ID = "ID";
    private static final String RESOURCE_ID = "trackID";
    private static final String AUTHOR_ID = "authorID";

    // SQL statements
    private static final String INSERT_TRACK_AUTHOR_STMT = """
        INSERT INTO TracksAuthors (trackID, authorID)
        VALUES (?, ?)
    """;

    private static final String UPDATE_TRACK_AUTHOR_STMT = """
        UPDATE TracksAuthors
        SET trackID = ?,
        authorID = ?
        WHERE ID = ?
    """;

    private static final String DELETE_TRACK_AUTHOR_STMT = """
        DELETE FROM TracksAuthors
        WHERE ID = ?
    """;

    private static final String GET_ALL_TRACK_AUTHORS_STMT = """
        SELECT *
        FROM TracksAuthors
    """;

    private static final String GET_TRACK_AUTHOR_BY_ID_STMT = """
        SELECT *
        FROM TracksAuthors
        WHERE ID = ?
    """;

    private static final String GET_TRACK_AUTHOR_BY_TRACK_ID_STMT = """
        SELECT *
        FROM TracksAuthors
        WHERE trackID = ?
    """;

    private static final String GET_TRACK_AUTHOR_BY_TRACK_AND_AUTHOR_ID = """
        SELECT *
        FROM TracksAuthors
        WHERE trackID = ? AND authorID = ?
    """;

    /**
     * Default constructor using the application's main database manager.
     */
    public TrackAuthorDAO() {
        dbManager = Main.dbManager;
    }

    /**
     * Constructor allowing the use of a custom {@link DatabaseManager}.
     * @param dbManager the database manager to use
     */
    public TrackAuthorDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Inserts a new TrackAuthor association into the database.
     *
     * @param trackAuthor the TrackAuthor to insert
     * @return the generated ID of the inserted record
     * @throws SQLiteException if the operation fails
     */
    @Override
    public Integer insert(TrackAuthor trackAuthor) {
        boolean success = dbManager.executeUpdate(INSERT_TRACK_AUTHOR_STMT,
                trackAuthor.getTrackId(),
                trackAuthor.getAuthorId()
        );

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }
        return dbManager.getLastInsertId();
    }

    /**
     * Updates an existing TrackAuthor association by its ID.
     *
     * @param trackAuthor the updated TrackAuthor
     * @param id the ID of the TrackAuthor to update
     * @throws SQLiteException if the operation fails
     */
    @Override
    public void updateById(TrackAuthor trackAuthor, int id) {
        boolean success = dbManager.executeUpdate(UPDATE_TRACK_AUTHOR_STMT,
                trackAuthor.getTrackId(),
                trackAuthor.getAuthorId(),
                id
        );

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }
    }

    /**
     * Deletes a TrackAuthor association by its ID.
     *
     * @param id the ID of the TrackAuthor to delete
     * @throws SQLiteException if the operation fails
     */
    @Override
    public void deleteById(int id) {
        boolean success = dbManager.executeUpdate(DELETE_TRACK_AUTHOR_STMT, id);

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }
    }

    /**
     * Retrieves a TrackAuthor by its unique ID.
     *
     * @param id the ID to look up
     * @return the matching TrackAuthor, or {@code null} if not found
     * @throws SQLiteException if the operation fails
     */
    @Override
    public TrackAuthor getById(int id) {
        AtomicReference<TrackAuthor> result = new AtomicReference<>();

        boolean success = dbManager.executeQuery(GET_TRACK_AUTHOR_BY_ID_STMT,
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
     * Retrieves all TrackAuthor associations for a specific track.
     *
     * @param trackId the ID of the track
     * @return a list of associated TrackAuthor records
     * @throws SQLiteException if the operation fails
     */
    public List<TrackAuthor> getByTrackId(int trackId) {
        List<TrackAuthor> trackAuthors = new ArrayList<>();

        boolean success = dbManager.executeQuery(GET_TRACK_AUTHOR_BY_TRACK_ID_STMT,
                rs -> {
                    while (rs.next()) {
                        trackAuthors.add(mapResultSetToEntity(rs));
                    }
                    return true;
                }, trackId);

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }

        return trackAuthors;
    }

    /**
     * Retrieves a TrackAuthor record that matches both a track ID and an author ID.
     *
     * @param trackId the ID of the track
     * @param authorId the ID of the author
     * @return the matching TrackAuthor, or {@code null} if not found
     */
    public TrackAuthor getByTrackIdAndAuthorId(int trackId, int authorId) {
        AtomicReference<TrackAuthor> result = new AtomicReference<>();

        dbManager.executeQuery(GET_TRACK_AUTHOR_BY_TRACK_AND_AUTHOR_ID,
                rs -> {
                    if (rs.next()) {
                        result.set(mapResultSetToEntity(rs));
                        return true;
                    }
                    return false;
                }, trackId, authorId);

        return result.get();
    }

    /**
     * Retrieves all TrackAuthor associations in the database.
     *
     * @return a list of all TrackAuthor records
     */
    @Override
    public List<TrackAuthor> getAll() {
        List<TrackAuthor> trackAuthors = new ArrayList<>();

        dbManager.executeQuery(GET_ALL_TRACK_AUTHORS_STMT,
                rs -> {
                    while (rs.next()) {
                        trackAuthors.add(mapResultSetToEntity(rs));
                    }
                    return null;
                });

        return trackAuthors;
    }

    /**
     * Maps a ResultSet row to a TrackAuthor entity.
     *
     * @param rs the result set containing the data
     * @return a TrackAuthor instance
     * @throws SQLException if a column is missing or cannot be read
     */
    private TrackAuthor mapResultSetToEntity(ResultSet rs) throws SQLException {
        int id = rs.getInt(ID);
        int resourceId = rs.getInt(RESOURCE_ID);
        int authorId = rs.getInt(AUTHOR_ID);
        return new TrackAuthor(id, resourceId, authorId);
    }
}
