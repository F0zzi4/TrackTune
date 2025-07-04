package app.tracktune.model.musicalInstrument;

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
 * Data Access Object (DAO) for {@link MusicalInstrument} entities.
 * Provides CRUD operations and queries related to musical instruments in the database.
 */
public class MusicalInstrumentDAO implements DAO<MusicalInstrument> {

    private final DatabaseManager dbManager;

    // Database column names
    private static final String ID = "ID";
    private static final String NAME = "name";
    private static final String DESCRIPTION = "description";

    // SQL Statements
    private static final String INSERT_MUSICAL_INSTRUMENT_STMT = """
        INSERT INTO MusicalInstruments (name, description)
        VALUES (?, ?)
    """;

    private static final String UPDATE_MUSICAL_INSTRUMENT_STMT = """
        UPDATE MusicalInstruments
        SET name = ?, description = ?
        WHERE ID = ?
    """;

    private static final String DELETE_MUSICAL_INSTRUMENT_STMT = """
        DELETE FROM MusicalInstruments
        WHERE ID = ?
    """;

    private static final String GET_ALL_MUSICAL_INSTRUMENTS_STMT = """
        SELECT *
        FROM MusicalInstruments
    """;

    private static final String GET_MUSICAL_INSTRUMENT_BY_ID_STMT = """
        SELECT *
        FROM MusicalInstruments
        WHERE ID = ?
    """;

    private static final String GET_MUSICAL_INSTRUMENT_BY_TRACK_ID_STMT = """
        SELECT m.*
        FROM MusicalInstruments m
             JOIN TracksInstruments ti ON ti.instrumentID = m.ID
        WHERE ti.trackID = ?
    """;

    /**
     * Constructs a MusicalInstrumentDAO using the default {@link DatabaseManager} instance.
     * Typically used in the main application context.
     */
    public MusicalInstrumentDAO() {
        this.dbManager = Main.dbManager;
    }

    /**
     * Constructs a MusicalInstrumentDAO with a provided {@link DatabaseManager}.
     * Useful for testing or using different database configurations.
     *
     * @param dbManager the database manager to use for queries and updates
     */
    public MusicalInstrumentDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Inserts a new musical instrument into the database.
     *
     * @param instrument the musical instrument to insert
     * @return the generated ID of the inserted instrument
     * @throws SQLiteException if the insertion fails
     */
    @Override
    public Integer insert(MusicalInstrument instrument) {
        boolean success = dbManager.executeUpdate(
                INSERT_MUSICAL_INSTRUMENT_STMT,
                instrument.getName(),
                instrument.getDescription()
        );

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }
        return dbManager.getLastInsertId();
    }

    /**
     * Updates an existing musical instrument identified by ID.
     *
     * @param instrument the instrument data to update
     * @param id the ID of the instrument to update
     * @throws SQLiteException if the update fails
     */
    @Override
    public void updateById(MusicalInstrument instrument, int id) {
        boolean success = dbManager.executeUpdate(
                UPDATE_MUSICAL_INSTRUMENT_STMT,
                instrument.getName(),
                instrument.getDescription(),
                id
        );

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }
    }

    /**
     * Deletes the musical instrument with the specified ID from the database.
     *
     * @param id the ID of the instrument to delete
     * @throws SQLiteException if the deletion fails
     */
    @Override
    public void deleteById(int id) {
        boolean success = dbManager.executeUpdate(DELETE_MUSICAL_INSTRUMENT_STMT, id);

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }
    }

    /**
     * Retrieves a musical instrument by its ID.
     *
     * @param id the ID of the instrument to retrieve
     * @return the {@link MusicalInstrument} with the specified ID, or null if not found
     * @throws SQLiteException if the query fails
     */
    @Override
    public MusicalInstrument getById(int id) {
        AtomicReference<MusicalInstrument> result = new AtomicReference<>();

        boolean success = dbManager.executeQuery(GET_MUSICAL_INSTRUMENT_BY_ID_STMT,
                rs -> {
                    if (rs.next()) {
                        result.set(mapResultSetToEntity(rs));
                        return true;
                    }
                    return false;
                }, id);

        if(!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }

        return result.get();
    }

    /**
     * Retrieves all musical instruments associated with a specific track.
     *
     * @param id the ID of the track
     * @return a list of {@link MusicalInstrument} objects linked to the track
     */
    public List<MusicalInstrument> getAllInstrumentByTrackId(int id) {
        List<MusicalInstrument> instruments = new ArrayList<>();

        dbManager.executeQuery(GET_MUSICAL_INSTRUMENT_BY_TRACK_ID_STMT,
                rs -> {
                    while (rs.next()) {
                        MusicalInstrument instrument = mapResultSetToEntity(rs);
                        instruments.add(instrument);
                    }
                    return null;
                }, id);

        return instruments;
    }

    /**
     * Retrieves all musical instruments from the database.
     *
     * @return a list of all {@link MusicalInstrument} objects
     */
    @Override
    public List<MusicalInstrument> getAll() {
        List<MusicalInstrument> instruments = new ArrayList<>();

        dbManager.executeQuery(GET_ALL_MUSICAL_INSTRUMENTS_STMT,
                rs -> {
                    while (rs.next()) {
                        MusicalInstrument instrument = mapResultSetToEntity(rs);
                        instruments.add(instrument);
                    }
                    return null;
                });

        return instruments;
    }

    /**
     * Maps the current row of the given {@link ResultSet} to a {@link MusicalInstrument} entity.
     *
     * @param rs the result set pointing to the current row
     * @return a MusicalInstrument entity mapped from the result set
     * @throws SQLException if a database access error occurs
     */
    private MusicalInstrument mapResultSetToEntity(ResultSet rs) throws SQLException {
        int id = rs.getInt(ID);
        String name = rs.getString(NAME);
        String description = rs.getString(DESCRIPTION);
        return new MusicalInstrument(id, name, description);
    }
}
