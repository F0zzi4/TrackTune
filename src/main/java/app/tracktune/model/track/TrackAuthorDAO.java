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

public class TrackAuthorDAO implements DAO<TrackAuthor> {
    private final DatabaseManager dbManager;

    // FIELDS
    private static final String ID = "ID";
    private static final String RESOURCE_ID = "trackID";
    private static final String AUTHOR_ID = "authorID";

    // CRUD STATEMENTS
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

    public TrackAuthorDAO() {
        dbManager = Main.dbManager;
    }

    public TrackAuthorDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

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

    @Override
    public void deleteById(int id) {
        boolean success = dbManager.executeUpdate(DELETE_TRACK_AUTHOR_STMT, id);

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }
    }

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

    private TrackAuthor mapResultSetToEntity(ResultSet rs) throws SQLException {
        int id = rs.getInt(ID);
        int resourceId = rs.getInt(RESOURCE_ID);
        int authorId = rs.getInt(AUTHOR_ID);
        return new TrackAuthor(id, resourceId, authorId);
    }
}