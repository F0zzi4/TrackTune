package app.tracktune.model.resource;

import app.tracktune.Main;
import app.tracktune.interfaces.DAO;
import app.tracktune.model.DatabaseManager;

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
    public void insert(Resource data) {

    }

    @Override
    public void update(Resource data) {

    }

    @Override
    public void delete(Resource data) {

    }

    @Override
    public Resource getByKey(Object key) {
        return null;
    }

    @Override
    public Set<Resource> getAll() {
        return Set.of();
    }

    @Override
    public boolean alreadyExists(Resource data) {
        return false;
    }
}
