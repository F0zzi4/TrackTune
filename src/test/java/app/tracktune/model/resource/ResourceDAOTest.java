package app.tracktune.model.resource;

import app.tracktune.model.DatabaseManager;
import app.tracktune.model.track.Track;
import app.tracktune.model.track.TrackDAO;
import app.tracktune.model.user.Administrator;
import app.tracktune.model.user.UserDAO;
import app.tracktune.model.user.UserStatusEnum;
import app.tracktune.utils.DBInit;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ResourceDAOTest {

    private DatabaseManager db;
    private ResourceDAO resourceDAO;
    private TrackDAO trackDAO;

    private int userId;
    private int trackId;

    @BeforeAll
    void setup() throws Exception {
        Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        Statement stmt = connection.createStatement();
        stmt.execute("PRAGMA foreign_keys = ON;");
        String[] ddl = DBInit.getDBInitStatement().split(";");
        for (String query : ddl) {
            if (!query.trim().isEmpty()) stmt.execute(query.trim() + ";");
        }

        DatabaseManager.setTestConnection(connection);
        db = DatabaseManager.getInstance();
        resourceDAO = new ResourceDAO(db);
        trackDAO = new TrackDAO(db);
        UserDAO userDAO = new UserDAO(db);

        Administrator testUser = new Administrator("testuser", "passwordHash", "nome", "cognome", UserStatusEnum.ACTIVE, new Timestamp(System.currentTimeMillis()));
        userId = userDAO.insert(testUser);

        Track track = new Track(null, "Test Track", new Timestamp(System.currentTimeMillis()), userId);
        trackId = trackDAO.insert(track);
    }

    @BeforeEach
    void clearTables() {
        db.executeUpdate("DELETE FROM Resources");
    }

    @Test
    void testInsertAndGetById() {
        Resource resource = new Resource(null, ResourceTypeEnum.pdf, new byte[]{1, 2, 3},
                new Timestamp(System.currentTimeMillis()), true, false, trackId, userId);
        Integer id = resourceDAO.insert(resource);
        assertNotNull(id);

        Resource fetched = resourceDAO.getById(id);
        assertNotNull(fetched);
        assertEquals(ResourceTypeEnum.pdf, fetched.getType());
        assertEquals(trackId, fetched.getTrackID());
        assertEquals(userId, fetched.getUserID());
        assertTrue(fetched.isMultimedia());
        assertFalse(fetched.isAuthor());
    }

    @Test
    void testUpdate() {
        Resource resource = new Resource(null, ResourceTypeEnum.pdf, new byte[]{1, 2, 3},
                new Timestamp(System.currentTimeMillis()), true, false, trackId, userId);
        Integer id = resourceDAO.insert(resource);

        Resource updated = new Resource(id, ResourceTypeEnum.mp3, new byte[]{4, 5, 6},
                new Timestamp(System.currentTimeMillis()), true, true, trackId, userId);
        resourceDAO.updateById(updated, id);

        Resource result = resourceDAO.getById(id);
        assertNotNull(result);
        assertEquals(ResourceTypeEnum.mp3, result.getType());
        assertTrue(result.isAuthor());
    }

    @Test
    void testDelete() {
        Resource resource = new Resource(null, ResourceTypeEnum.pdf, new byte[]{1, 2, 3},
                new Timestamp(System.currentTimeMillis()), true, false, trackId, userId);
        Integer id = resourceDAO.insert(resource);

        resourceDAO.deleteById(id);

        Resource result = resourceDAO.getById(id);
        assertNull(result);
    }

    @Test
    void testGetAll() {
        resourceDAO.insert(new Resource(null, ResourceTypeEnum.pdf, new byte[]{1, 2, 3},
                new Timestamp(System.currentTimeMillis()), true, false, trackId, userId));
        resourceDAO.insert(new Resource(null, ResourceTypeEnum.mp3, new byte[]{4, 5, 6},
                new Timestamp(System.currentTimeMillis()), true, true, trackId, userId));

        List<Resource> all = resourceDAO.getAll();
        assertNotNull(all);
        assertEquals(2, all.size());
    }

    @Test
    void testGetAllByUserID() {
        resourceDAO.insert(new Resource(null, ResourceTypeEnum.pdf, new byte[]{1, 2, 3},
                new Timestamp(System.currentTimeMillis()), true, false, trackId, userId));
        resourceDAO.insert(new Resource(null, ResourceTypeEnum.mp3, new byte[]{4, 5, 6},
                new Timestamp(System.currentTimeMillis()), true, true, trackId, userId));

        List<Resource> resources = resourceDAO.getAllByUserID(userId);
        assertNotNull(resources);
        assertEquals(2, resources.size());
    }

    @Test
    void testGetAllByTrackID() {
        resourceDAO.insert(new Resource(null, ResourceTypeEnum.pdf, new byte[]{1, 2, 3},
                new Timestamp(System.currentTimeMillis()), true, false, trackId, userId));
        resourceDAO.insert(new Resource(null, ResourceTypeEnum.mp3, new byte[]{4, 5, 6},
                new Timestamp(System.currentTimeMillis()), true, true, trackId, userId));

        List<Resource> resources = resourceDAO.getAllByTrackID(trackId);
        assertNotNull(resources);
        assertEquals(2, resources.size());
    }

    @Test
    void testGetAllCommentedResourcesByUserID() {
        List<Resource> resources = resourceDAO.getAllCommentedResourcesByUserID(userId);
        assertNotNull(resources);
    }
}
