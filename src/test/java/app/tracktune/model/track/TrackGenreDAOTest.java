package app.tracktune.model.track;

import app.tracktune.utils.DatabaseManager;
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
public class TrackGenreDAOTest {

    private TrackGenreDAO trackGenreDAO;

    private int trackId;
    private int genreId1;
    private int genreId2;
    private int genreId3;

    @BeforeAll
    void setup() throws Exception {
        Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        Statement stmt = connection.createStatement();
        stmt.execute("PRAGMA foreign_keys = ON;");
        for (String query : DBInit.getDBInitStatement().split(";")) {
            if (!query.trim().isEmpty()) stmt.execute(query.trim() + ";");
        }

        DatabaseManager.setTestConnection(connection);
        DatabaseManager db = DatabaseManager.getInstance();
        trackGenreDAO = new TrackGenreDAO(db);
        TrackDAO trackDAO = new TrackDAO(db);
        app.tracktune.model.genre.GenreDAO genreDAO = new app.tracktune.model.genre.GenreDAO(db);
        UserDAO userDAO = new UserDAO(db);

        int userId = userDAO.insert(new Administrator("testUser", "passwordHash", "name", "surname", UserStatusEnum.ACTIVE, new Timestamp(System.currentTimeMillis())));
        trackId = trackDAO.insert(new Track(null, "Test Track", new Timestamp(System.currentTimeMillis()), userId));

        genreId1 = genreDAO.insert(new app.tracktune.model.genre.Genre("Test Genre 1", "Test Description 1"));
        genreId2 = genreDAO.insert(new app.tracktune.model.genre.Genre("Test Genre 2", "Test Description 2"));
        genreId3 = genreDAO.insert(new app.tracktune.model.genre.Genre("Test Genre 3", "Test Description 3"));
    }

    /**
     * Tests inserting a TrackGenre and retrieving it by ID.
     */
    @Test
    void testInsertAndGetById() {
        TrackGenre tg = new TrackGenre(trackId, genreId1);
        Integer id = trackGenreDAO.insert(tg);
        assertNotNull(id);

        TrackGenre fetched = trackGenreDAO.getById(id);
        assertEquals(trackId, fetched.getTrackId());
        assertEquals(genreId1, fetched.getGenreId());
    }

    /**
     * Tests updating a TrackGenre by ID.
     */
    @Test
    void testUpdate() {
        Integer id = trackGenreDAO.insert(new TrackGenre(trackId, genreId1));
        TrackGenre updated = new TrackGenre(id, trackId, genreId2);
        trackGenreDAO.updateById(updated, id);

        TrackGenre result = trackGenreDAO.getById(id);
        assertEquals(trackId, result.getTrackId());
        assertEquals(genreId2, result.getGenreId());
    }

    /**
     * Tests deleting a TrackGenre by ID.
     */
    @Test
    void testDelete() {
        Integer id = trackGenreDAO.insert(new TrackGenre(trackId, genreId1));
        trackGenreDAO.deleteById(id);

        assertThrows(app.tracktune.exceptions.SQLiteException.class, () -> trackGenreDAO.getById(id));
    }

    /**
     * Tests fetching all TrackGenres.
     */
    @Test
    void testGetAll() {
        trackGenreDAO.insert(new TrackGenre(trackId, genreId1));
        trackGenreDAO.insert(new TrackGenre(trackId, genreId2));

        List<TrackGenre> all = trackGenreDAO.getAll();
        assertTrue(all.size() >= 2);
    }

    /**
     * Tests retrieving TrackGenres by track ID.
     */
    @Test
    void testGetByTrackId() {
        trackGenreDAO.insert(new TrackGenre(trackId, genreId1));
        trackGenreDAO.insert(new TrackGenre(trackId, genreId2));

        List<TrackGenre> result = trackGenreDAO.getByTrackId(trackId);
        assertTrue(result.size() >= 2);
    }

    /**
     * Tests retrieving a TrackGenre by track ID and genre ID.
     */
    @Test
    void testGetByTrackIdAndGenreId() {
        trackGenreDAO.insert(new TrackGenre(trackId, genreId3));

        TrackGenre result = trackGenreDAO.getByTrackIdAndGenreId(trackId, genreId3);
        assertNotNull(result);
        assertEquals(trackId, result.getTrackId());
        assertEquals(genreId3, result.getGenreId());
    }
}
