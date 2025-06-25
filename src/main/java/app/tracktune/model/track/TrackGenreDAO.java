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
 * Data Access Object (DAO) for managing {@link TrackGenre} entities.
 * Handles all CRUD operations for the {@code TracksGenres} association table in the database,
 * which represents the many-to-many relationship between tracks and genres.
 */
public class TrackGenreDAO implements DAO<TrackGenre> {
    private final DatabaseManager dbManager;

    // Database column names
    private static final String ID = "ID";
    private static final String TRACK_ID = "trackID";
    private static final String GENRE_ID = "genreID";

    // SQL statements
    private static final String INSERT_TRACK_GENRE_STMT = """
        INSERT INTO TracksGenres (trackID, genreID)
        VALUES (?, ?)
    """;

    private static final String UPDATE_TRACK_GENRE_STMT = """
        UPDATE TracksGenres
        SET trackID = ?,
        genreID = ?
        WHERE ID = ?
    """;

    private static final String DELETE_TRACK_GENRE_STMT = """
        DELETE FROM TracksGenres
        WHERE ID = ?
    """;

    private static final String GET_ALL_TRACK_GENRE_STMT = """
        SELECT *
        FROM TracksGenres
    """;

    private static final String GET_TRACK_GENRE_BY_ID_STMT = """
        SELECT *
        FROM TracksGenres
        WHERE ID = ?
    """;

    private static final String GET_TRACK_GENRE_BY_TRACK_ID_STMT = """
        SELECT *
        FROM TracksGenres
        WHERE trackID = ?
    """;

    private static final String GET_TRACK_GENRE_BY_TRACK_AND_GENRE_ID = """
        SELECT *
        FROM TracksGenres
        WHERE trackID = ? AND genreID = ?
    """;

    /**
     * Default constructor. Initializes the DAO using the application's main {@link DatabaseManager}.
     */
    public TrackGenreDAO() {
        dbManager = Main.dbManager;
    }

    /**
     * Constructs the DAO with a specific {@link DatabaseManager} instance.
     *
     * @param dbManager the database manager to be used
     */
    public TrackGenreDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Inserts a new {@link TrackGenre} record into the database.
     *
     * @param trackGenre the entity to insert
     * @return the generated ID of the new record
     * @throws SQLiteException if the operation fails
     */
    @Override
    public Integer insert(TrackGenre trackGenre) {
        boolean success = dbManager.executeUpdate(INSERT_TRACK_GENRE_STMT,
                trackGenre.getTrackId(),
                trackGenre.getGenreId()
        );

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }
        return dbManager.getLastInsertId();
    }

    /**
     * Updates an existing {@link TrackGenre} record by ID.
     *
     * @param trackGenre the updated entity
     * @param id         the ID of the record to update
     * @throws SQLiteException if the operation fails
     */
    @Override
    public void updateById(TrackGenre trackGenre, int id) {
        boolean success = dbManager.executeUpdate(UPDATE_TRACK_GENRE_STMT,
                trackGenre.getTrackId(),
                trackGenre.getGenreId(),
                id
        );

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }
    }

    /**
     * Deletes a {@link TrackGenre} record by ID.
     *
     * @param id the ID of the record to delete
     * @throws SQLiteException if the operation fails
     */
    @Override
    public void deleteById(int id) {
        boolean success = dbManager.executeUpdate(DELETE_TRACK_GENRE_STMT, id);

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }
    }

    /**
     * Retrieves a {@link TrackGenre} record by ID.
     *
     * @param id the ID of the record to retrieve
     * @return the corresponding {@code TrackGenre}, or {@code null} if not found
     * @throws SQLiteException if the operation fails
     */
    @Override
    public TrackGenre getById(int id) {
        AtomicReference<TrackGenre> result = new AtomicReference<>();

        boolean success = dbManager.executeQuery(GET_TRACK_GENRE_BY_ID_STMT,
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
     * Retrieves all {@link TrackGenre} associations for a given track.
     *
     * @param trackId the ID of the track
     * @return a list of {@code TrackGenre} records
     * @throws SQLiteException if the operation fails
     */
    public List<TrackGenre> getByTrackId(int trackId) {
        List<TrackGenre> trackGenres = new ArrayList<>();

        boolean success = dbManager.executeQuery(GET_TRACK_GENRE_BY_TRACK_ID_STMT,
                rs -> {
                    while (rs.next()) {
                        trackGenres.add(mapResultSetToEntity(rs));
                    }
                    return true;
                }, trackId);

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }

        return trackGenres;
    }

    /**
     * Retrieves a {@link TrackGenre} record by track ID and genre ID.
     *
     * @param trackId the ID of the track
     * @param genreId the ID of the genre
     * @return the matching {@code TrackGenre}, or {@code null} if not found
     */
    public TrackGenre getByTrackIdAndGenreId(int trackId, int genreId) {
        AtomicReference<TrackGenre> result = new AtomicReference<>();

        dbManager.executeQuery(GET_TRACK_GENRE_BY_TRACK_AND_GENRE_ID,
                rs -> {
                    if (rs.next()) {
                        result.set(mapResultSetToEntity(rs));
                        return true;
                    }
                    return false;
                }, trackId, genreId);

        return result.get();
    }

    /**
     * Retrieves all {@link TrackGenre} records from the database.
     *
     * @return a list of all {@code TrackGenre} associations
     */
    @Override
    public List<TrackGenre> getAll() {
        List<TrackGenre> trackGenres = new ArrayList<>();

        dbManager.executeQuery(GET_ALL_TRACK_GENRE_STMT,
                rs -> {
                    while (rs.next()) {
                        trackGenres.add(mapResultSetToEntity(rs));
                    }
                    return null;
                });

        return trackGenres;
    }

    /**
     * Maps a {@link ResultSet} row to a {@link TrackGenre} entity.
     *
     * @param rs the result set
     * @return the corresponding {@code TrackGenre}
     * @throws SQLException if an error occurs while reading the result set
     */
    private TrackGenre mapResultSetToEntity(ResultSet rs) throws SQLException {
        int id = rs.getInt(ID);
        int trackId = rs.getInt(TRACK_ID);
        int genreId = rs.getInt(GENRE_ID);
        return new TrackGenre(id, trackId, genreId);
    }
}
