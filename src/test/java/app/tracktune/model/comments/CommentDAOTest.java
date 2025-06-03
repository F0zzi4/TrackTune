package app.tracktune.model.comments;

import app.tracktune.model.DatabaseManager;
import app.tracktune.model.resource.Resource;
import app.tracktune.model.resource.ResourceDAO;
import app.tracktune.model.resource.ResourceTypeEnum;
import app.tracktune.model.track.Track;
import app.tracktune.model.track.TrackDAO;
import app.tracktune.utils.DBInit;
import org.junit.jupiter.api.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CommentDAOTest {

    private DatabaseManager db;
    private CommentDAO commentDAO;
    private ResourceDAO resourceDAO;
    private TrackDAO trackDAO;

    // Store IDs for test data
    private final int userId = 1; // Use the admin user that's created by default
    private int trackId;
    private int resourceId;

    @BeforeAll
    void setup() throws Exception {
        // Use an in-memory database for testing
        String url = "jdbc:sqlite::memory:";
        Connection connection = DriverManager.getConnection(url);
        Statement stmt = connection.createStatement();
        stmt.execute("PRAGMA foreign_keys = ON;");
        String[] ddl = DBInit.getDBInitStatement().split(";");
        for (String query : ddl) {
            if (!query.trim().isEmpty()) stmt.execute(query.trim() + ";");
        }
        connection.close();

        // Manual override of the connection for testing
        db = DatabaseManager.getInstance();
        commentDAO = new CommentDAO(db);
        resourceDAO = new ResourceDAO(db);
        trackDAO = new TrackDAO(db);

        // Create a test track to use in the tests
        Track track = new Track(null, "Test Track", new Timestamp(System.currentTimeMillis()), userId);
        trackId = trackDAO.insert(track);

        // Create a test resource to use in the tests
        Resource resource = new Resource(null, ResourceTypeEnum.pdf, new byte[]{1, 2, 3}, 
                new Timestamp(System.currentTimeMillis()), true, false, trackId, userId);
        resourceId = resourceDAO.insert(resource);

        System.out.println("[DEBUG_LOG] User ID: " + userId);
        System.out.println("[DEBUG_LOG] Track ID: " + trackId);
        System.out.println("[DEBUG_LOG] Resource ID: " + resourceId);
    }

    @Test
    void testInsertAndGetById() {
        // Create a comment
        Comment comment = new Comment("Test Comment", 0, 0, new Timestamp(System.currentTimeMillis()), userId, resourceId);
        Integer id = commentDAO.insert(comment);
        assertNotNull(id);

        // Get the comment by ID
        Comment fetched = commentDAO.getById(id);
        assertEquals("Test Comment", fetched.getDescription());
        assertEquals(userId, fetched.getUserID());
        assertEquals(resourceId, fetched.getResourceID());
    }

    @Test
    void testUpdate() {
        // Create a comment
        Comment comment = new Comment("Initial Comment", 0, 0, new Timestamp(System.currentTimeMillis()), userId, resourceId);
        Integer id = commentDAO.insert(comment);

        // Update the comment
        Comment updated = new Comment(id, "Updated Comment", 0, 0, new Timestamp(System.currentTimeMillis()), userId, resourceId);
        commentDAO.updateById(updated, id);

        // Get the updated comment
        Comment result = commentDAO.getById(id);
        assertEquals("Updated Comment", result.getDescription());
    }

    @Test
    void testDelete() {
        // Create a comment
        Comment comment = new Comment("To Delete", 0, 0, new Timestamp(System.currentTimeMillis()), userId, resourceId);
        Integer id = commentDAO.insert(comment);

        // Delete the comment
        commentDAO.deleteById(id);

        // The getById method should throw an exception when the comment is not found
        assertThrows(app.tracktune.exceptions.SQLiteException.class, () -> {
            commentDAO.getById(id);
        });
    }

    @Test
    void testGetAll() {
        // Create multiple comments
        Comment c1 = new Comment("Comment 1", 0, 0, new Timestamp(System.currentTimeMillis()), userId, resourceId);
        Comment c2 = new Comment("Comment 2", 0, 0, new Timestamp(System.currentTimeMillis()), userId, resourceId);
        commentDAO.insert(c1);
        commentDAO.insert(c2);

        // Get all comments
        List<Comment> all = commentDAO.getAll();
        assertTrue(all.size() >= 2);
    }

    @Test
    void testInsertReplyAndGetAllReplies() {
        // Create a parent comment
        Comment parent = new Comment("Parent Comment", 0, 0, new Timestamp(System.currentTimeMillis()), userId, resourceId);
        Integer parentId = commentDAO.insert(parent);

        // Create a reply comment
        Comment reply = new Comment("Reply Comment", 0, 0, new Timestamp(System.currentTimeMillis()), userId, resourceId);
        Integer replyId = commentDAO.insert(reply);

        // Link the reply to the parent
        commentDAO.insertReply(parentId, replyId);

        // Get all replies for the parent
        List<Comment> replies = commentDAO.getAllReplies(parentId);
        assertNotNull(replies);
        assertTrue(replies.size() >= 1);

        // Verify the reply is in the list
        boolean found = false;
        for (Comment c : replies) {
            if (c.getID().equals(replyId)) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    @Test
    void testGetAllCommentByResource() {
        // Create comments for a specific resource
        Comment c1 = new Comment("Resource Comment 1", 0, 0, new Timestamp(System.currentTimeMillis()), userId, resourceId);
        Comment c2 = new Comment("Resource Comment 2", 0, 0, new Timestamp(System.currentTimeMillis()), userId, resourceId);
        commentDAO.insert(c1);
        commentDAO.insert(c2);

        // Get all comments for the resource
        List<Comment> comments = commentDAO.getAllCommentByResource(resourceId);
        assertNotNull(comments);
        assertTrue(comments.size() >= 2);
    }
}
