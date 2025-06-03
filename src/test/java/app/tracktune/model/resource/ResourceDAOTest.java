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

    // Store IDs for test data
    private int userId; // Use the admin user that's created by default
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

        // Inserisci utente di test
        Administrator testUser = new Administrator("testuser", "passwordHash", "nome", "cognome", UserStatusEnum.ACTIVE, new Timestamp(System.currentTimeMillis()));
        userId = userDAO.insert(testUser);

        // Inserisci traccia collegata all'utente
        Track track = new Track(null, "Test Track", new Timestamp(System.currentTimeMillis()), userId);
        trackId = trackDAO.insert(track);

        System.out.println("[DEBUG_LOG] User ID: " + userId);
        System.out.println("[DEBUG_LOG] Track ID: " + trackId);
    }

    @Test
    void testInsertAndGetById() {
        // Create a resource
        Resource resource = new Resource(null, ResourceTypeEnum.pdf, new byte[]{1, 2, 3}, 
                new Timestamp(System.currentTimeMillis()), true, false, trackId, userId);
        Integer id = resourceDAO.insert(resource);
        assertNotNull(id);

        // Get the resource by ID
        Resource fetched = resourceDAO.getById(id);
        assertEquals(ResourceTypeEnum.pdf, fetched.getType());
        assertEquals(trackId, fetched.getTrackID());
        assertEquals(userId, fetched.getUserID());
        assertTrue(fetched.isMultimedia());
        assertFalse(fetched.isAuthor());
    }

    @Test
    void testUpdate() {
        // Create a resource
        Resource resource = new Resource(null, ResourceTypeEnum.pdf, new byte[]{1, 2, 3}, 
                new Timestamp(System.currentTimeMillis()), true, false, trackId, userId);
        Integer id = resourceDAO.insert(resource);

        // Update the resource
        Resource updated = new Resource(id, ResourceTypeEnum.mp3, new byte[]{4, 5, 6}, 
                new Timestamp(System.currentTimeMillis()), true, true, trackId, userId);
        resourceDAO.updateById(updated, id);

        // Get the updated resource
        Resource result = resourceDAO.getById(id);
        assertEquals(ResourceTypeEnum.mp3, result.getType());
        assertTrue(result.isAuthor());
    }

    @Test
    void testDelete() {
        // Create a resource
        Resource resource = new Resource(null, ResourceTypeEnum.pdf, new byte[]{1, 2, 3}, 
                new Timestamp(System.currentTimeMillis()), true, false, trackId, userId);
        Integer id = resourceDAO.insert(resource);

        // Delete the resource
        resourceDAO.deleteById(id);

        // The getById method should return null when the resource is not found
        Resource result = resourceDAO.getById(id);
        assertNull(result);
    }

    @Test
    void testGetAll() {
        // Create multiple resources
        Resource r1 = new Resource(null, ResourceTypeEnum.pdf, new byte[]{1, 2, 3}, 
                new Timestamp(System.currentTimeMillis()), true, false, trackId, userId);
        Resource r2 = new Resource(null, ResourceTypeEnum.mp3, new byte[]{4, 5, 6}, 
                new Timestamp(System.currentTimeMillis()), true, true, trackId, userId);
        resourceDAO.insert(r1);
        resourceDAO.insert(r2);

        // Get all resources
        List<Resource> all = resourceDAO.getAll();
        assertTrue(all.size() >= 2);
    }

    @Test
    void testGetAllByUserID() {
        // Create resources for a specific user
        Resource r1 = new Resource(null, ResourceTypeEnum.pdf, new byte[]{1, 2, 3}, 
                new Timestamp(System.currentTimeMillis()), true, false, trackId, userId);
        Resource r2 = new Resource(null, ResourceTypeEnum.mp3, new byte[]{4, 5, 6}, 
                new Timestamp(System.currentTimeMillis()), true, true, trackId, userId);
        resourceDAO.insert(r1);
        resourceDAO.insert(r2);

        // Get all resources for the user
        List<Resource> resources = resourceDAO.getAllByUserID(userId);
        assertNotNull(resources);
        assertTrue(resources.size() >= 2);
    }

    @Test
    void testGetAllByTrackID() {
        // Create resources for a specific track
        Resource r1 = new Resource(null, ResourceTypeEnum.pdf, new byte[]{1, 2, 3}, 
                new Timestamp(System.currentTimeMillis()), true, false, trackId, userId);
        Resource r2 = new Resource(null, ResourceTypeEnum.mp3, new byte[]{4, 5, 6}, 
                new Timestamp(System.currentTimeMillis()), true, true, trackId, userId);
        resourceDAO.insert(r1);
        resourceDAO.insert(r2);

        // Get all resources for the track
        List<Resource> resources = resourceDAO.getAllByTrackID(trackId);
        assertNotNull(resources);
        assertTrue(resources.size() >= 2);
    }

    @Test
    void testGetAllCommentedResourcesByUserID() {
        // This test would require setting up comments first
        // For now, we'll just test that the method doesn't throw an exception
        List<Resource> resources = resourceDAO.getAllCommentedResourcesByUserID(userId);
        assertNotNull(resources);
    }
}
