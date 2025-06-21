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

public class MusicalInstrumentDAO implements DAO<MusicalInstrument> {

    private final DatabaseManager dbManager;

    // DB fields
    private static final String ID = "ID";
    private static final String NAME = "name";
    private static final String DESCRIPTION = "description";

    // SQL statements
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

    public MusicalInstrumentDAO() {
        this.dbManager = Main.dbManager;
    }

    public MusicalInstrumentDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

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

    @Override
    public void deleteById(int id) {
        boolean success = dbManager.executeUpdate(DELETE_MUSICAL_INSTRUMENT_STMT, id);

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }
    }

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

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }

        return result.get();
    }


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

    private MusicalInstrument mapResultSetToEntity(ResultSet rs) throws SQLException {
        int id = rs.getInt(ID);
        String name = rs.getString(NAME);
        String description = rs.getString(DESCRIPTION);
        return new MusicalInstrument(id, name, description);
    }
}