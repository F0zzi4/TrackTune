package app.tracktune.model.comments;

import app.tracktune.model.DatabaseManager;
import app.tracktune.model.resource.Resource;
import app.tracktune.model.resource.ResourceDAO;
import app.tracktune.model.resource.ResourceTypeEnum;
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

/**
 * JUnit test class for {@link CommentDAO}.
 * Tests CRUD operations and specific query methods for the Comment entity.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CommentDAOTest {

    private CommentDAO commentDAO;

    private int userId;
    private int resourceId;

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
        DatabaseManager db = DatabaseManager.getInstance();
        commentDAO = new CommentDAO(db);
        ResourceDAO resourceDAO = new ResourceDAO(db);
        TrackDAO trackDAO = new TrackDAO(db);
        UserDAO userDAO = new UserDAO(db);

        Administrator testUser = new Administrator("testUser", "passwordHash", "name", "surname", UserStatusEnum.ACTIVE, new Timestamp(System.currentTimeMillis()));
        userId = userDAO.insert(testUser);

        Track track = new Track(null, "Test Track", new Timestamp(System.currentTimeMillis()), userId);
        int trackId = trackDAO.insert(track);

        Resource resource = new Resource(null, ResourceTypeEnum.pdf, new byte[]{1, 2, 3},
                new Timestamp(System.currentTimeMillis()), true, false, trackId, userId);
        resourceId = resourceDAO.insert(resource);
    }

    /**
     * Tests inserting and retrieving a Comment by ID.
     */
    @Test
    void testInsertAndGetById() {
        Comment comment = new Comment("Test Comment", 0, 0, new Timestamp(System.currentTimeMillis()), userId, resourceId);
        Integer id = commentDAO.insert(comment);
        assertNotNull(id);

        Comment fetched = commentDAO.getById(id);
        assertEquals("Test Comment", fetched.getDescription());
        assertEquals(userId, fetched.getUserID());
        assertEquals(resourceId, fetched.getResourceID());
    }

    /**
     * Tests updating a Comment.
     */
    @Test
    void testUpdate() {
        Comment comment = new Comment("Initial Comment", 0, 0, new Timestamp(System.currentTimeMillis()), userId, resourceId);
        Integer id = commentDAO.insert(comment);

        Comment updated = new Comment(id, "Updated Comment", 0, 0, new Timestamp(System.currentTimeMillis()), userId, resourceId);
        commentDAO.updateById(updated, id);

        Comment result = commentDAO.getById(id);
        assertEquals("Updated Comment", result.getDescription());
    }

    /**
     * Tests deleting a Comment by ID.
     */
    @Test
    void testDelete() {
        Comment comment = new Comment("To Delete", 0, 0, new Timestamp(System.currentTimeMillis()), userId, resourceId);
        Integer id = commentDAO.insert(comment);

        commentDAO.deleteById(id);

        assertThrows(app.tracktune.exceptions.SQLiteException.class, () -> commentDAO.getById(id));
    }

    /**
     * Tests retrieving all Comments.
     */
    @Test
    void testGetAll() {
        Comment c1 = new Comment("Comment 1", 0, 0, new Timestamp(System.currentTimeMillis()), userId, resourceId);
        Comment c2 = new Comment("Comment 2", 0, 0, new Timestamp(System.currentTimeMillis()), userId, resourceId);
        commentDAO.insert(c1);
        commentDAO.insert(c2);

        List<Comment> all = commentDAO.getAll();
        assertTrue(all.size() >= 2);
    }

    /**
     * Tests inserting a reply to a Comment and retrieving all replies.
     */
    @Test
    void testInsertReplyAndGetAllReplies() {
        Comment parent = new Comment("Parent Comment", 0, 0, new Timestamp(System.currentTimeMillis()), userId, resourceId);
        Integer parentId = commentDAO.insert(parent);

        Comment reply = new Comment("Reply Comment", 0, 0, new Timestamp(System.currentTimeMillis()), userId, resourceId);
        Integer replyId = commentDAO.insert(reply);

        commentDAO.insertReply(parentId, replyId);

        List<Comment> replies = commentDAO.getAllReplies(parentId);
        assertNotNull(replies);
        assertTrue(replies.stream().anyMatch(c -> c.getID().equals(replyId)));
    }

    /**
     * Tests retrieving all Comments associated with a specific Resource.
     */
    @Test
    void testGetAllCommentByResource() {
        Comment c1 = new Comment("Resource Comment 1", 0, 0, new Timestamp(System.currentTimeMillis()), userId, resourceId);
        Comment c2 = new Comment("Resource Comment 2", 0, 0, new Timestamp(System.currentTimeMillis()), userId, resourceId);
        commentDAO.insert(c1);
        commentDAO.insert(c2);

        List<Comment> comments = commentDAO.getAllCommentByResource(resourceId);
        assertNotNull(comments);
        assertTrue(comments.size() >= 2);
    }
}
