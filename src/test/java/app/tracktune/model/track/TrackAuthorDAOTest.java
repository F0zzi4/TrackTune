package app.tracktune.model.track;

import app.tracktune.model.DatabaseManager;
import app.tracktune.model.author.Author;
import app.tracktune.model.author.AuthorDAO;
import app.tracktune.model.author.AuthorStatusEnum;
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
 * Unit tests for the TrackAuthorDAO class, which handles the mapping between tracks and authors.
 * Uses an in-memory SQLite database to isolate test cases.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TrackAuthorDAOTest {

    private DatabaseManager db;
    private TrackAuthorDAO trackAuthorDAO;
    private TrackDAO trackDAO;
    private AuthorDAO authorDAO;

    private int trackId;
    private int authorId1;
    private int authorId2;
    private int authorId3;

    /**
     * Initializes the in-memory database, runs DDL statements, and inserts test data.
     */
    @BeforeAll
    void setup() throws Exception {
        Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        Statement stmt = connection.createStatement();
        stmt.execute("PRAGMA foreign_keys = ON;");
        for (String query : DBInit.getDBInitStatement().split(";")) {
            if (!query.trim().isEmpty()) stmt.execute(query.trim() + ";");
        }

        DatabaseManager.setTestConnection(connection);
        db = DatabaseManager.getInstance();
        trackAuthorDAO = new TrackAuthorDAO(db);
        trackDAO = new TrackDAO(db);
        authorDAO = new AuthorDAO(db);
        UserDAO userDAO = new UserDAO(db);

        Administrator testUser = new Administrator(
                "testuser", "password", "nome", "cognome",
                UserStatusEnum.ACTIVE, new Timestamp(System.currentTimeMillis())
        );
        int userId = userDAO.insert(testUser);

        Track track = new Track(null, "Test Track", new Timestamp(System.currentTimeMillis()), userId);
        trackId = trackDAO.insert(track);

        authorId1 = authorDAO.insert(new Author(null, "Test Author 1", AuthorStatusEnum.ACTIVE));
        authorId2 = authorDAO.insert(new Author(null, "Test Author 2", AuthorStatusEnum.ACTIVE));
        authorId3 = authorDAO.insert(new Author(null, "Test Author 3", AuthorStatusEnum.ACTIVE));
    }

    /**
     * Cleans up the track-author relationships after each test to avoid test interference.
     */
    @AfterEach
    void cleanup() {
        trackAuthorDAO.getAll().forEach(ta -> trackAuthorDAO.deleteById(ta.getId()));
    }

    /**
     * Tests insertion of a TrackAuthor relationship and retrieval by its ID.
     */
    @Test
    void testInsertAndGetById() {
        TrackAuthor trackAuthor = new TrackAuthor(trackId, authorId1);
        Integer id = trackAuthorDAO.insert(trackAuthor);
        assertNotNull(id);

        TrackAuthor fetched = trackAuthorDAO.getById(id);
        assertEquals(trackId, fetched.getTrackId());
        assertEquals(authorId1, fetched.getAuthorId());
    }

    /**
     * Tests updating an existing TrackAuthor relationship with a different author ID.
     */
    @Test
    void testUpdate() {
        TrackAuthor original = new TrackAuthor(trackId, authorId1);
        Integer id = trackAuthorDAO.insert(original);

        TrackAuthor updated = new TrackAuthor(id, trackId, authorId2);
        trackAuthorDAO.updateById(updated, id);

        TrackAuthor result = trackAuthorDAO.getById(id);
        assertEquals(trackId, result.getTrackId());
        assertEquals(authorId2, result.getAuthorId());
    }

    /**
     * Tests deletion of a TrackAuthor relationship and verifies it is no longer retrievable.
     */
    @Test
    void testDelete() {
        TrackAuthor trackAuthor = new TrackAuthor(trackId, authorId1);
        Integer id = trackAuthorDAO.insert(trackAuthor);

        trackAuthorDAO.deleteById(id);

        assertThrows(app.tracktune.exceptions.SQLiteException.class, () -> trackAuthorDAO.getById(id));
    }

    /**
     * Tests retrieving all TrackAuthor relationships in the database.
     */
    @Test
    void testGetAll() {
        trackAuthorDAO.insert(new TrackAuthor(trackId, authorId1));
        trackAuthorDAO.insert(new TrackAuthor(trackId, authorId2));

        List<TrackAuthor> all = trackAuthorDAO.getAll();
        assertTrue(all.size() >= 2);
    }

    /**
     * Tests retrieving all TrackAuthor relationships for a specific track.
     */
    @Test
    void testGetByTrackId() {
        trackAuthorDAO.insert(new TrackAuthor(trackId, authorId1));
        trackAuthorDAO.insert(new TrackAuthor(trackId, authorId2));

        List<TrackAuthor> results = trackAuthorDAO.getByTrackId(trackId);
        assertTrue(results.size() >= 2);
    }

    /**
     * Tests retrieving a TrackAuthor relationship by both track ID and author ID.
     */
    @Test
    void testGetByTrackIdAndAuthorId() {
        trackAuthorDAO.insert(new TrackAuthor(trackId, authorId3));

        TrackAuthor result = trackAuthorDAO.getByTrackIdAndAuthorId(trackId, authorId3);
        assertNotNull(result);
        assertEquals(trackId, result.getTrackId());
        assertEquals(authorId3, result.getAuthorId());
    }
}
