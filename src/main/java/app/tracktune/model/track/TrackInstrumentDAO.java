package app.tracktune.model.track;

import app.tracktune.Main;
import app.tracktune.exceptions.SQLiteException;
import app.tracktune.interfaces.DAO;
import app.tracktune.model.DatabaseManager;
import app.tracktune.utils.Strings;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class TrackInstrumentDAO implements DAO<TrackInstrument> {
    private final DatabaseManager dbManager;

    // FIELDS
    private static final String ID = "ID";
    private static final String TRACK_ID = "trackID";
    private static final String INSTRUMENT_ID = "instrumentID";

    // CRUD STATEMENTS
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

    private static final String GET_TRACK_INSTRUMENT_BY_INSTRUMENT_ID_STMT = """
        SELECT *
        FROM TracksInstruments
        WHERE instrumentID = ?
    """;

    public TrackInstrumentDAO() {
        dbManager = Main.dbManager;
    }

    @Override
    public Integer insert(TrackInstrument ti) {
        boolean success = dbManager.executeUpdate(INSERT_TRACK_INSTRUMENT_STMT,
                ti.getTrackId(),
                ti.getInstrumentId()
        );

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }
        return dbManager.getLastInsertId();
    }

    @Override
    public void updateById(TrackInstrument ti, int id) {
        boolean success = dbManager.executeUpdate(UPDATE_TRACK_INSTRUMENT_STMT,
                ti.getTrackId(),
                ti.getInstrumentId(),
                id
        );

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }
    }

    @Override
    public void deleteById(int id) {
        boolean success = dbManager.executeUpdate(DELETE_TRACK_INSTRUMENT_STMT, id);

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }
    }

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

    public List<TrackInstrument> getByInstrumentId(int instrumentId) {
        List<TrackInstrument> list = new ArrayList<>();

        boolean success = dbManager.executeQuery(GET_TRACK_INSTRUMENT_BY_INSTRUMENT_ID_STMT,
                rs -> {
                    while (rs.next()) {
                        list.add(mapResultSetToEntity(rs));
                    }
                    return true;
                }, instrumentId);

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }

        return list;
    }

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

    private TrackInstrument mapResultSetToEntity(ResultSet rs) throws SQLException {
        int id = rs.getInt(ID);
        int trackId = rs.getInt(TRACK_ID);
        int instrumentId = rs.getInt(INSTRUMENT_ID);
        return new TrackInstrument(id, trackId, instrumentId);
    }
}
