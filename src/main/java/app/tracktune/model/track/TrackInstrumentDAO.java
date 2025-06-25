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
 * Data Access Object (DAO) implementation for {@link TrackInstrument}.
 * Handles all database operations for the TracksInstruments table.
 */
public class TrackInstrumentDAO implements DAO<TrackInstrument> {
    private final DatabaseManager dbManager;

    // Database column names
    private static final String ID = "ID";
    private static final String TRACK_ID = "trackID";
    private static final String INSTRUMENT_ID = "instrumentID";

    // SQL Statements
    private static final String INSERT_TRACK_INSTRUMENT_STMT = """
        INSERT INTO TracksInstruments (trackID, instrumentID)
        VALUES (?, ?)
    """;

    private static final String UPDATE_TRACK_INSTRUMENT_STMT = """
        UPDATE TracksInstruments
        SET trackID = ?, instrumentID = ?
        WHERE ID = ?
    """;

    private static final String DELETE_TRACK_INSTRUMENT_STMT = """
        DELETE FROM TracksInstruments
        WHERE ID = ?
    """;

    private static final String GET_ALL_TRACK_INSTRUMENTS_STMT = """
        SELECT *
        FROM TracksInstruments
    """;

    private static final String GET_TRACK_INSTRUMENT_BY_ID_STMT = """
        SELECT *
        FROM TracksInstruments
        WHERE ID = ?
    """;

    private static final String GET_TRACK_INSTRUMENT_BY_TRACK_ID_STMT = """
        SELECT *
        FROM TracksInstruments
        WHERE trackID = ?
    """;

    private static final String GET_TRACK_INSTRUMENT_BY_TRACK_AND_INSTRUMENT_ID = """
        SELECT *
        FROM TracksInstruments
        WHERE trackID = ? AND instrumentID = ?
    """;

    /**
     * Constructs a {@code TrackInstrumentDAO} using the default {@link DatabaseManager}.
     */
    public TrackInstrumentDAO() {
        dbManager = Main.dbManager;
    }

    /**
     * Constructs a {@code TrackInstrumentDAO} with a custom {@link DatabaseManager}.
     *
     * @param dbManager the database manager to use
     */
    public TrackInstrumentDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Inserts a new {@link TrackInstrument} record into the database.
     *
     * @param ti the TrackInstrument to insert
     * @return the generated ID of the inserted record
     */
    @Override
    public Integer insert(TrackInstrument ti) {
        boolean success = dbManager.executeUpdate(INSERT_TRACK_INSTRUMENT_STMT,
                ti.getTrackId(),
                ti.getInstrumentId());

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }
        return dbManager.getLastInsertId();
    }

    /**
     * Updates an existing {@link TrackInstrument} by ID.
     *
     * @param ti  the updated TrackInstrument data
     * @param id  the ID of the record to update
     */
    @Override
    public void updateById(TrackInstrument ti, int id) {
        boolean success = dbManager.executeUpdate(UPDATE_TRACK_INSTRUMENT_STMT,
                ti.getTrackId(),
                ti.getInstrumentId(),
                id);

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }
    }

    /**
     * Deletes a {@link TrackInstrument} record by its ID.
     *
     * @param id the ID of the record to delete
     */
    @Override
    public void deleteById(int id) {
        boolean success = dbManager.executeUpdate(DELETE_TRACK_INSTRUMENT_STMT, id);

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }
    }

    /**
     * Retrieves a {@link TrackInstrument} record by ID.
     *
     * @param id the ID of the desired record
     * @return the corresponding TrackInstrument, or {@code null} if not found
     */
    @Override
    public TrackInstrument getById(int id) {
        AtomicReference<TrackInstrument> result = new AtomicReference<>();

        boolean success = dbManager.executeQuery(GET_TRACK_INSTRUMENT_BY_ID_STMT,
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
     * Retrieves all {@link TrackInstrument} associations for a given track.
     *
     * @param trackId the ID of the track
     * @return a list of TrackInstrument objects associated with the track
     */
    public List<TrackInstrument> getByTrackId(int trackId) {
        List<TrackInstrument> list = new ArrayList<>();

        boolean success = dbManager.executeQuery(GET_TRACK_INSTRUMENT_BY_TRACK_ID_STMT,
                rs -> {
                    while (rs.next()) {
                        list.add(mapResultSetToEntity(rs));
                    }
                    return true;
                }, trackId);

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }

        return list;
    }

    /**
     * Retrieves a {@link TrackInstrument} by track ID and instrument ID.
     *
     * @param trackId      the ID of the track
     * @param instrumentId the ID of the instrument
     * @return the corresponding TrackInstrument, or {@code null} if not found
     */
    public TrackInstrument getByTrackIdAndInstrumentId(int trackId, int instrumentId) {
        AtomicReference<TrackInstrument> result = new AtomicReference<>();

        dbManager.executeQuery(GET_TRACK_INSTRUMENT_BY_TRACK_AND_INSTRUMENT_ID,
                rs -> {
                    if (rs.next()) {
                        result.set(mapResultSetToEntity(rs));
                        return true;
                    }
                    return false;
                }, trackId, instrumentId);

        return result.get();
    }

    /**
     * Retrieves all {@link TrackInstrument} records from the database.
     *
     * @return a list of all TrackInstrument associations
     */
    @Override
    public List<TrackInstrument> getAll() {
        List<TrackInstrument> list = new ArrayList<>();

        dbManager.executeQuery(GET_ALL_TRACK_INSTRUMENTS_STMT,
                rs -> {
                    while (rs.next()) {
                        list.add(mapResultSetToEntity(rs));
                    }
                    return null;
                });

        return list;
    }

    /**
     * Maps a {@link ResultSet} row to a {@link TrackInstrument} object.
     *
     * @param rs the result set to map
     * @return the corresponding TrackInstrument object
     * @throws SQLException if a database access error occurs
     */
    private TrackInstrument mapResultSetToEntity(ResultSet rs) throws SQLException {
        int id = rs.getInt(ID);
        int trackId = rs.getInt(TRACK_ID);
        int instrumentId = rs.getInt(INSTRUMENT_ID);
        return new TrackInstrument(id, trackId, instrumentId);
    }
}
