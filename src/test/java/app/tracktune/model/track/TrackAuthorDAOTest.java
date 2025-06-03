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

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TrackAuthorDAOTest {

    private DatabaseManager db;
    private TrackAuthorDAO trackAuthorDAO;
    private TrackDAO trackDAO;
    private AuthorDAO authorDAO;

    // Store the IDs of the test data
    private int trackId;
    private int authorId1;
    private int authorId2;
    private int authorId3;

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
        trackAuthorDAO = new TrackAuthorDAO(db);
        trackDAO = new TrackDAO(db);
        authorDAO = new AuthorDAO(db);

        UserDAO userDAO = new UserDAO(db);

        // Inserisci utente di test
        Administrator testUser = new Administrator("testuser", "passwordHash", "nome", "cognome", UserStatusEnum.ACTIVE, new Timestamp(System.currentTimeMillis()));
        int userId = userDAO.insert(testUser);

        // Inserisci traccia collegata all'utente
        Track track = new Track(null, "Test Track", new Timestamp(System.currentTimeMillis()), userId);
        trackId = trackDAO.insert(track);

        // Create test authors to use in the tests
        Author author1 = new Author(null, "Test Author 1", AuthorStatusEnum.ACTIVE);
        Author author2 = new Author(null, "Test Author 2", AuthorStatusEnum.ACTIVE);
        Author author3 = new Author(null, "Test Author 3", AuthorStatusEnum.ACTIVE);
        authorId1 = authorDAO.insert(author1);
        authorId2 = authorDAO.insert(author2);
        authorId3 = authorDAO.insert(author3);

        System.out.println("[DEBUG_LOG] Track ID: " + trackId);
        System.out.println("[DEBUG_LOG] Author ID 1: " + authorId1);
        System.out.println("[DEBUG_LOG] Author ID 2: " + authorId2);
        System.out.println("[DEBUG_LOG] Author ID 3: " + authorId3);
    }

    @AfterEach
    void cleanup() {
        // Clean up any track-author relationships created in the tests
        List<TrackAuthor> all = trackAuthorDAO.getAll();
        for (TrackAuthor ta : all) {
            trackAuthorDAO.deleteById(ta.getId());
        }
    }

    @Test
    void testInsertAndGetById() {
        // Create a track-author relationship using the actual IDs
        TrackAuthor trackAuthor = new TrackAuthor(trackId, authorId1);
        Integer id = trackAuthorDAO.insert(trackAuthor);
        assertNotNull(id);

        TrackAuthor fetched = trackAuthorDAO.getById(id);
        assertEquals(trackId, fetched.getTrackId());
        assertEquals(authorId1, fetched.getAuthorId());
    }

    @Test
    void testUpdate() {
        // Create a track-author relationship
        TrackAuthor trackAuthor = new TrackAuthor(trackId, authorId1);
        Integer id = trackAuthorDAO.insert(trackAuthor);

        // Update it to use a different author
        TrackAuthor updated = new TrackAuthor(id, trackId, authorId2);
        trackAuthorDAO.updateById(updated, id);

        TrackAuthor result = trackAuthorDAO.getById(id);
        assertEquals(trackId, result.getTrackId());
        assertEquals(authorId2, result.getAuthorId());
    }

    @Test
    void testDelete() {
        TrackAuthor trackAuthor = new TrackAuthor(trackId, authorId1);
        Integer id = trackAuthorDAO.insert(trackAuthor);

        trackAuthorDAO.deleteById(id);

        // The getById method throws an exception when the track-author relationship is not found
        assertThrows(app.tracktune.exceptions.SQLiteException.class, () -> {
            trackAuthorDAO.getById(id);
        });
    }

    @Test
    void testGetAll() {
        TrackAuthor ta1 = new TrackAuthor(trackId, authorId1);
        TrackAuthor ta2 = new TrackAuthor(trackId, authorId2);
        trackAuthorDAO.insert(ta1);
        trackAuthorDAO.insert(ta2);

        List<TrackAuthor> all = trackAuthorDAO.getAll();
        assertTrue(all.size() >= 2);
    }

    @Test
    void testGetByTrackId() {
        TrackAuthor ta1 = new TrackAuthor(trackId, authorId1);
        TrackAuthor ta2 = new TrackAuthor(trackId, authorId2);
        trackAuthorDAO.insert(ta1);
        trackAuthorDAO.insert(ta2);

        List<TrackAuthor> byTrackId = trackAuthorDAO.getByTrackId(trackId);
        assertTrue(byTrackId.size() >= 2);
    }

    @Test
    void testGetByTrackIdAndAuthorId() {
        TrackAuthor ta = new TrackAuthor(trackId, authorId3);
        trackAuthorDAO.insert(ta);

        TrackAuthor result = trackAuthorDAO.getByTrackIdAndAuthorId(trackId, authorId3);
        assertNotNull(result);
        assertEquals(trackId, result.getTrackId());
        assertEquals(authorId3, result.getAuthorId());
    }
}
