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

public class TrackGenreDAO implements DAO<TrackGenre> {
    private final DatabaseManager dbManager;

    // FIELDS
    private static final String ID = "ID";
    private static final String TRACK_ID = "trackID";
    private static final String GENRE_ID = "genreID";

    // CRUD STATEMENTS
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

    private static final String GET_TRACK_GENRE_BY_GENRE_ID_STMT = """
        SELECT *
        FROM TracksGenres
        WHERE genreID = ?
    """;

    private static final String GET_TRACK_GENRE_BY_TRACK_AND_GENRE_ID = """
        SELECT *
        FROM TracksGenres
        WHERE trackID = ? AND genreID = ?
    """;

    public TrackGenreDAO() {
        dbManager = Main.dbManager;
    }

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

    @Override
    public void deleteById(int id) {
        boolean success = dbManager.executeUpdate(DELETE_TRACK_GENRE_STMT, id);

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }
    }

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

    public List<TrackGenre> getByTrackId(int resourceId) {
        List<TrackGenre> trackGenres = new ArrayList<>();

        boolean success = dbManager.executeQuery(GET_TRACK_GENRE_BY_TRACK_ID_STMT,
                rs -> {
                    while (rs.next()) {
                        trackGenres.add(mapResultSetToEntity(rs));
                    }
                    return true;
                }, resourceId);

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }

        return trackGenres;
    }

    public List<TrackGenre> getByGenreId(int genreId) {
        List<TrackGenre> trackGenres = new ArrayList<>();

        boolean success = dbManager.executeQuery(GET_TRACK_GENRE_BY_GENRE_ID_STMT,
                rs -> {
                    while (rs.next()) {
                        trackGenres.add(mapResultSetToEntity(rs));
                    }
                    return true;
                }, genreId);

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }

        return trackGenres;
    }

    public TrackGenre getByTrackIdAndGenreId(int trackId, int genreId) {
        AtomicReference<TrackGenre> result = new AtomicReference<>();

        boolean success = dbManager.executeQuery(GET_TRACK_GENRE_BY_TRACK_AND_GENRE_ID,
                rs -> {
                    if (rs.next()) {
                        result.set(mapResultSetToEntity(rs));
                        return true;
                    }
                    return false;
                }, trackId, genreId);


        return result.get();
    }

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

    private TrackGenre mapResultSetToEntity(ResultSet rs) throws SQLException {
        int id = rs.getInt(ID);
        int resourceId = rs.getInt(TRACK_ID);
        int genreId = rs.getInt(GENRE_ID);
        return new TrackGenre(id, resourceId, genreId);
    }
}
