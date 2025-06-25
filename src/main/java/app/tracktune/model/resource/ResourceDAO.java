package app.tracktune.model.resource;

import app.tracktune.Main;
import app.tracktune.exceptions.SQLiteException;
import app.tracktune.interfaces.DAO;
import app.tracktune.utils.DatabaseManager;
import app.tracktune.utils.Strings;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Data Access Object (DAO) implementation for the {@link Resource} entity.
 * Provides methods to perform CRUD operations on the Resources table in the database.
 */
public class ResourceDAO implements DAO<Resource> {

    private final DatabaseManager dbManager;

    // Database table field names
    private static final String ID = "ID";
    private static final String TYPE = "type";
    private static final String DATA = "data";
    private static final String CREATION_DATE = "creationDate";
    private static final String IS_MULTIMEDIA = "isMultimedia";
    private static final String LOCATION = "location";
    private static final String RESOURCE_DATE = "resourceDate";
    private static final String IS_AUTHOR = "isAuthor";
    private static final String TRACK_ID = "trackID";
    private static final String USER_ID = "userID";

    // SQL statements for CRUD operations
    private static final String INSERT_RESOURCE_STMT = """
        INSERT INTO Resources (type, data, creationDate, isMultimedia, location, resourceDate, isAuthor, trackID, userID)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;

    private static final String UPDATE_RESOURCE_STMT = """
        UPDATE Resources
        SET type = ?, data = ?,
        creationDate = ?,
        isMultimedia = ?,
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
        WHERE R.userID = ?;
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

    /**
     * Constructs a new ResourceDAO using the default {@link DatabaseManager} instance
     * from the main application.
     */
    public ResourceDAO() {
        dbManager = Main.dbManager;
    }

    /**
     * Constructs a new ResourceDAO with a specified {@link DatabaseManager}.
     *
     * @param dbManager the DatabaseManager to use for database operations
     */
    public ResourceDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Inserts a new resource into the database.
     * Supports both general resources and multimedia resources.
     *
     * @param resource the resource to insert
     * @return the ID of the newly inserted resource
     * @throws SQLiteException if the insertion fails
     */
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

    /**
     * Updates an existing resource identified by its ID (trackID here seems like a misnomer, should be resource ID).
     *
     * @param resource the resource with updated data
     * @param trackID  the ID of the resource to update (note: parameter name is misleading, expected resource ID)
     * @throws SQLiteException if the update fails
     */
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
                    multimedia.getLocation(),
                    multimedia.getResourceDate(),
                    multimedia.isAuthor(),
                    multimedia.getTrackID(),
                    multimedia.getUserID(),
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

    /**
     * Deletes a resource by its ID.
     *
     * @param id the ID of the resource to delete
     * @throws SQLiteException if the deletion fails
     */
    @Override
    public void deleteById(int id) {
        boolean success = dbManager.executeUpdate(DELETE_RESOURCE_STMT, id);
        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }
    }

    /**
     * Retrieves a resource by its ID.
     *
     * @param id the ID of the resource
     * @return the Resource object, or null if not found
     */
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

    /**
     * Retrieves all resources.
     *
     * @return a list of all Resource objects in the database
     */
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

    /**
     * Retrieves all resources belonging to a specific user.
     *
     * @param userId the user ID
     * @return a list of Resource objects owned by the user
     */
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

    /**
     * Retrieves all resources linked to a specific track.
     *
     * @param trackId the track ID
     * @return a list of Resource objects linked to the track
     */
    public List<Resource> getAllByTrackID(int trackId) {
        List<Resource> resources = new ArrayList<>();

        dbManager.executeQuery(
                GET_ALL_RESOURCES_BY_TRACK_ID_STMT,
                rs -> {
                    while (rs.next()) {
                        resources.add(mapResultSetToEntity(rs));
                    }
                    return null;
                }, trackId
        );

        return resources;
    }

    /**
     * Retrieves the last 5 distinct resources commented on by a specific user,
     * ordered by comment creation date descending.
     *
     * @param userId the user ID who made the comments
     * @return a list of commented Resource objects
     */
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

    /**
     * Maps a {@link ResultSet} row to a {@link Resource} or {@link MultimediaResource} entity.
     *
     * @param rs the ResultSet positioned at the current row
     * @return the mapped Resource object
     * @throws SQLException if a database access error occurs
     */
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
            String location = rs.getString(LOCATION);
            Date resourceDate = rs.getDate(RESOURCE_DATE);
            return new MultimediaResource(id, type, data, creationDate, true, location, resourceDate, isAuthor, trackID, userID);
        } else {
            return new Resource(id, type, data, creationDate, false, isAuthor, trackID, userID);
        }
    }
}
