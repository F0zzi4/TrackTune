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

    private int userId;
    private int trackId;

    /**
     * Initial setup performed once before all tests.
     * It initializes the database in memory, executes DDL queries, and creates a user and a test trace.
     */
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
        TrackDAO trackDAO = new TrackDAO(db);
        UserDAO userDAO = new UserDAO(db);

        Administrator testUser = new Administrator("testUser", "passwordHash", "name", "surname", UserStatusEnum.ACTIVE, new Timestamp(System.currentTimeMillis()));
        userId = userDAO.insert(testUser);

        Track track = new Track(null, "Test Track", new Timestamp(System.currentTimeMillis()), userId);
        trackId = trackDAO.insert(track);
    }

    /**
     * Cleans the resource table before each test to avoid interference.
     */
    @BeforeEach
    void clearTables() {
        db.executeUpdate("DELETE FROM Resources");
    }

    /**
     * Tests the insertion and retrieval of a resource by its ID.
     */
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

    /**
     * Tests the update of a resource and verifies that the data have been changed correctly.
     */
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

    /**
     * Test the deletion of a resource and verify that it is no longer in the database.
     */
    @Test
    void testDelete() {
        Resource resource = new Resource(null, ResourceTypeEnum.pdf, new byte[]{1, 2, 3},
                new Timestamp(System.currentTimeMillis()), true, false, trackId, userId);
        Integer id = resourceDAO.insert(resource);

        resourceDAO.deleteById(id);

        Resource result = resourceDAO.getById(id);
        assertNull(result);
    }

    /**
     * Tests the retrieval of all resources in the database.
     */
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

    /**
     * Tests the recovery of all resources associated with a given user.
     */
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

    /**
     * Tests the recovery of all resources associated with a given track.
     */
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

    /**
     * Tests the retrieval of all resources commented on by a user.
     * Since no comments are included in the test, it only checks that it does not throw exceptions.
     */
    @Test
    void testGetAllCommentedResourcesByUserID() {
        List<Resource> resources = resourceDAO.getAllCommentedResourcesByUserID(userId);
        assertNotNull(resources);
    }
}
