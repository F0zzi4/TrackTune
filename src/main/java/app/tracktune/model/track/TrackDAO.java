package app.tracktune.model.track;

import app.tracktune.Main;
import app.tracktune.exceptions.SQLiteException;
import app.tracktune.interfaces.DAO;
import app.tracktune.utils.DatabaseManager;
import app.tracktune.utils.Strings;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class TrackDAO implements DAO<Track> {
    private final DatabaseManager dbManager;

    // FIELDS
    private static final String ID = "ID";
    private static final String TITLE = "title";
    private static final String CREATION_DATE = "creationDate";
    private static final String USER_ID = "userID";

    // CRUD STATEMENTS
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
        DELETE FROM Tracks
        WHERE ID = ?
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
    JOIN TracksGenres ta ON ta.trackID = t.ID
    WHERE ta.genreID = ?;
    """;

    private static final String GET_ALL_TRACK_BY_INSTRUMENT_ID_STMT = """
        SELECT t.*
        FROM Tracks t
        JOIN TracksInstruments ta ON ta.trackID = t.ID
        WHERE ta.instrumentID = ?;
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


    public TrackDAO() {
        dbManager = Main.dbManager;
    }

    public TrackDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

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

    @Override
    public void deleteById(int id) {
        boolean success = dbManager.executeUpdate(DELETE_TRACK_STMT, id);

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }
    }

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

    private Track mapResultSetToEntity(ResultSet rs) throws SQLException {
        Integer id = rs.getInt(ID);
        String title = rs.getString(TITLE);
        Timestamp creationDate = rs.getTimestamp(CREATION_DATE);
        int userID = rs.getInt(USER_ID);
        return new Track(id, title, creationDate, userID);
    }
}
