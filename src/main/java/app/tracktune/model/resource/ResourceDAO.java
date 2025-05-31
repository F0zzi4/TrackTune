package app.tracktune.model.resource;

import app.tracktune.Main;
import app.tracktune.exceptions.SQLiteException;
import app.tracktune.interfaces.DAO;
import app.tracktune.model.DatabaseManager;
import app.tracktune.utils.Strings;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ResourceDAO implements DAO<Resource> {

    private final DatabaseManager dbManager;

    // FIELDS
    private static final String ID = "ID";
    private static final String TYPE = "type";
    private static final String DATA = "data";
    private static final String CREATION_DATE = "creationDate";
    private static final String IS_MULTIMEDIA = "isMultimedia";
    private static final String DURATION = "duration";
    private static final String LOCATION = "location";
    private static final String RESOURCE_DATE = "resourceDate";
    private static final String IS_AUTHOR = "isAuthor";
    private static final String TRACK_ID = "trackID";
    private static final String USER_ID = "userID";

    // SQL STATEMENTS
    private static final String INSERT_RESOURCE_STMT = """
        INSERT INTO Resources (type, data, creationDate, isMultimedia, duration, location, resourceDate, isAuthor, trackID, userID)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;

    private static final String UPDATE_RESOURCE_STMT = """
        UPDATE Resources
        SET type = ?, data = ?,
        creationDate = ?,
        isMultimedia = ?,
        duration = ?,
        location = ?,
        resourceDate = ?,
        isAuthor = ?,
        trackID = ?,
        userID = ?
        WHERE ID = ?
    """;

    private static final String DELETE_RESOURCE_STMT = """
        DELETE FROM Resources
        WHERE ID = ?
    """;

    private static final String GET_ALL_RESOURCES_STMT = """
        SELECT * FROM Resources
    """;

    private static final String GET_ALL_RESOURCES_BY_USERID_STMT = """
        SELECT R.*
        FROM Resources R
        JOIN Tracks T ON R.trackID = T.ID
        WHERE T.userID = ?;
    """;

    private static final String GET_ALL_RESOURCES_BY_TRACK_ID_STMT = """
        SELECT * FROM Resources
        WHERE trackID = ?
    """;

    private static final String GET_RESOURCE_BY_ID_STMT = """
        SELECT * FROM Resources
        WHERE ID = ?
    """;

    private static final String GET_RESOURCE_COMMENTS_BY_USER_ID_STMT = """
        SELECT DISTINCT r.*
        FROM Resources r
        JOIN Comments c ON r.ID = c.resourceID
        WHERE c.userID = ?
        ORDER BY C.creationDate DESC
        LIMIT 5
    """;

    public ResourceDAO() {
        dbManager = Main.dbManager;
    }

    public ResourceDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public Integer insert(Resource resource) {
        boolean success;

        if (resource instanceof MultimediaResource multimedia) {
            success = dbManager.executeUpdate(
                    INSERT_RESOURCE_STMT,
                    multimedia.getType().getValue(),
                    multimedia.getData(),
                    multimedia.getCreationDate(),
                    multimedia.isMultimedia(),
                    multimedia.getDuration(),
                    multimedia.getLocation(),
                    multimedia.getResourceDate(),
                    multimedia.isAuthor(),
                    multimedia.getTrackID(),
                    multimedia.getUserID()
            );
        } else {
            success = dbManager.executeUpdate(
                    INSERT_RESOURCE_STMT,
                    resource.getType().getValue(),
                    resource.getData(),
                    resource.getCreationDate(),
                    resource.isMultimedia(),
                    null,
                    null,
                    null,
                    resource.isAuthor(),
                    resource.getTrackID(),
                    resource.getUserID()
            );
        }

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }

        return dbManager.getLastInsertId();
    }

    @Override
    public void updateById(Resource resource, int trackID) {
        boolean success;

        if (resource instanceof MultimediaResource multimedia) {
            success = dbManager.executeUpdate(
                    UPDATE_RESOURCE_STMT,
                    multimedia.getType().getValue(),
                    multimedia.getData(),
                    multimedia.getCreationDate(),
                    multimedia.isMultimedia(),
                    multimedia.getDuration(),
                    multimedia.getLocation(),
                    multimedia.getResourceDate(),
                    multimedia.isAuthor(),
                    multimedia.getTrackID(),
                    multimedia.getId(),
                    trackID
            );
        } else {
            success = dbManager.executeUpdate(
                    UPDATE_RESOURCE_STMT,
                    resource.getType().getValue(),
                    resource.getData(),
                    resource.getCreationDate(),
                    resource.isMultimedia(),
                    null,
                    null,
                    null,
                    resource.isAuthor(),
                    resource.getTrackID(),
                    resource.getUserID(),
                    trackID
            );
        }

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }
    }

    @Override
    public void deleteById(int id) {
        boolean success = dbManager.executeUpdate(DELETE_RESOURCE_STMT, id);
        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }
    }

    @Override
    public Resource getById(int id) {
        AtomicReference<Resource> result = new AtomicReference<>();

        dbManager.executeQuery(GET_RESOURCE_BY_ID_STMT,
                rs -> {
                    if (rs.next()) {
                        result.set(mapResultSetToEntity(rs));
                        return true;
                    }
                    return false;
                }, id);

        return result.get();
    }

    @Override
    public List<Resource> getAll() {
        List<Resource> resources = new ArrayList<>();

        dbManager.executeQuery(
                GET_ALL_RESOURCES_STMT,
                rs -> {
                    while (rs.next()) {
                        resources.add(mapResultSetToEntity(rs));
                    }
                    return null;
                }
        );

        return resources;
    }

    public List<Resource> getAllByUserID(int userId) {
        List<Resource> resources = new ArrayList<>();

        dbManager.executeQuery(
                GET_ALL_RESOURCES_BY_USERID_STMT,
                rs -> {
                    while (rs.next()) {
                        resources.add(mapResultSetToEntity(rs));
                    }
                    return null;
                }, userId
        );

        return resources;
    }

    public List<Resource> getAllByTrackID(int userId) {
        List<Resource> resources = new ArrayList<>();

        dbManager.executeQuery(
                GET_ALL_RESOURCES_BY_TRACK_ID_STMT,
                rs -> {
                    while (rs.next()) {
                        resources.add(mapResultSetToEntity(rs));
                    }
                    return null;
                }, userId
        );

        return resources;
    }

    public List<Resource> getAllCommentedResourcesByUserID(int userId) {
        List<Resource> resources = new ArrayList<>();

        dbManager.executeQuery(
                GET_RESOURCE_COMMENTS_BY_USER_ID_STMT,
                rs -> {
                    while (rs.next()) {
                        resources.add(mapResultSetToEntity(rs));
                    }
                    return null;
                }, userId
        );

        return resources;
    }

    public static Resource mapResultSetToEntity(ResultSet rs) throws SQLException {
        int id = rs.getInt(ID);
        ResourceTypeEnum type = ResourceTypeEnum.fromInt(rs.getInt(TYPE));
        byte[] data = rs.getBytes(DATA);
        Timestamp creationDate = rs.getTimestamp(CREATION_DATE);
        boolean isMultimedia = rs.getInt(IS_MULTIMEDIA) == 1;
        boolean isAuthor = rs.getInt(IS_AUTHOR) == 1;
        int trackID = rs.getInt(TRACK_ID);
        int userID = rs.getInt(USER_ID);

        if (isMultimedia) {
            Time duration = rs.getTime(DURATION);
            String location = rs.getString(LOCATION);
            Date resourceDate = rs.getDate(RESOURCE_DATE);
            return new MultimediaResource(id, type, data, creationDate, true, duration, location, resourceDate, isAuthor, trackID, userID);
        } else {
            return new Resource(id, type, data, creationDate, false, isAuthor, trackID, userID);
        }
    }
}