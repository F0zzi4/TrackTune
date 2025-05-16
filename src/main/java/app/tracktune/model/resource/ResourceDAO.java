package app.tracktune.model.resource;

import app.tracktune.Main;
import app.tracktune.exceptions.EntityAlreadyExistsException;
import app.tracktune.interfaces.DAO;
import app.tracktune.model.DatabaseManager;
import app.tracktune.utils.Strings;

import java.sql.Blob;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

public class ResourceDAO implements DAO<Resource> {
    private final Set<Resource> cache = new HashSet<>();
    private final DatabaseManager dbManager;
    // FIELDS
    private static final String TYPE = "type";
    private static final String DATA = "data";
    private static final String CREATION_DATE = "creationDate";
    private static final String IS_MULTIMEDIA = "isMultimedia";
    private static final String DURATION = "duration";
    private static final String LOCATION = "location";
    private static final String RESOURCE_DATE = "resourceDate";
    private static final String TRACK_ID = "trackID";

    // CRUD STATEMENTS
    private static final String INSERT_RESOURCE_STMT = """
        INSERT INTO Resources (type, data, creationDate, isMultimedia, duration, location, resourceDate, trackID)
        VALUES (?, ?, ?, ?, ?, ?)
    """;

    private static final String UPDATE_RESOURCE_STMT = """
        UPDATE Resources
        SET type = ?,
        data = ?,
        creationDate = ?,
        isMultimedia = ?,
        duration = ?,
        location = ?,
        resourceDate = ?
        WHERE creationDate = ?
    """;

    private static final String DELETE_RESOURCE_STMT = """
        DELETE FROM Resources
        WHERE creationDate = ?
    """;

    private static final String GET_ALL_RESOURCES_STMT = """
            SELECT *
            FROM Resources
    """;

    public ResourceDAO() {
        dbManager = Main.dbManager;
        refreshCache();
    }

    @Override
    public void refreshCache() {
        cache.clear();
        dbManager.executeQuery(GET_ALL_RESOURCES_STMT,
                rs -> {
                    while (rs.next()) {
                        ResourceTypeEnum type = ResourceTypeEnum.fromInt(rs.getInt(TYPE));
                        Blob data = rs.getBlob(DATA);
                        Timestamp creationDate = rs.getTimestamp(CREATION_DATE);
                        boolean isMultimedia = rs.getInt(IS_MULTIMEDIA) == 1;
                        int trackID = rs.getInt(TRACK_ID);

                        if(isMultimedia){
                            int duration = rs.getInt(DURATION);
                            String location = rs.getString(LOCATION);
                            Date resourceDate = rs.getDate(RESOURCE_DATE);
                            cache.add(new MultimediaResource(type, data, creationDate, true, duration, location, resourceDate, trackID));
                        }
                        else
                            cache.add(new Resource(type, data, creationDate, false, trackID));
                    }
                    return null;
                }
        );
    }

    @Override
    public void insert(Resource resource) {
        if(alreadyExists(resource)){
            throw new EntityAlreadyExistsException(Strings.ERR_ENTITY_ALREADY_EXISTS);
        }

        boolean success;

        if(resource instanceof MultimediaResource multimediaResource){
            success = dbManager.executeUpdate(INSERT_RESOURCE_STMT,
                    multimediaResource.getType().getValue(),
                    multimediaResource.getData(),
                    multimediaResource.getCreationDate(),
                    multimediaResource.isMultimedia(),
                    multimediaResource.getDuration(),
                    multimediaResource.getLocation(),
                    multimediaResource.getTrackID()
            );
        }else{
            success = dbManager.executeUpdate(INSERT_RESOURCE_STMT,
                    resource.getType().getValue(),
                    resource.getData(),
                    resource.getCreationDate(),
                    resource.isMultimedia(),
                    resource.getTrackID(),
                    null,
                    null
            );
        }

        if(success)
            cache.add(resource);
    }

    @Override
    public void update(Resource resource) {
        boolean success;

        if(resource instanceof MultimediaResource multimediaResource){
            success = dbManager.executeUpdate(UPDATE_RESOURCE_STMT,
                    multimediaResource.getType().getValue(),
                    multimediaResource.getData(),
                    multimediaResource.getCreationDate(),
                    multimediaResource.isMultimedia(),
                    multimediaResource.getDuration(),
                    multimediaResource.getLocation(),
                    multimediaResource.getResourceDate(),
                    multimediaResource.getCreationDate()
            );
        }else{
            success = dbManager.executeUpdate(UPDATE_RESOURCE_STMT,
                    resource.getType().getValue(),
                    resource.getData(),
                    resource.getCreationDate(),
                    resource.isMultimedia(),
                    null,
                    null,
                    null,
                    resource.getCreationDate()
            );
        }
        if (success) {
            cache.remove(resource);
            cache.add(resource);
        }
    }

    @Override
    public void delete(Resource resource) {
        boolean success = dbManager.executeUpdate(
                DELETE_RESOURCE_STMT,
                resource.getCreationDate()
        );

        if (success) {
            cache.remove(resource);
        }
    }

    @Override
    public Resource getByKey(Object key) {
        return cache.stream()
                .filter(pendingUser -> pendingUser.getCreationDate().equals(key))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Set<Resource> getAll() {
        return cache;
    }

    @Override
    public boolean alreadyExists(Resource resource) {
        return false;
    }
}
