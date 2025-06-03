package app.tracktune.model.track;

import app.tracktune.model.DatabaseManager;
import app.tracktune.model.author.AuthorDAO;
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

    private DatabaseManager db;
    private TrackGenreDAO trackGenreDAO;
    private TrackDAO trackDAO;
    private app.tracktune.model.genre.GenreDAO genreDAO;

    // Store the IDs of the test data
    private int trackId;
    private int genreId1;
    private int genreId2;
    private int genreId3;

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
        trackGenreDAO = new TrackGenreDAO(db);
        trackDAO = new TrackDAO(db);
        genreDAO = new app.tracktune.model.genre.GenreDAO(db);

        UserDAO userDAO = new UserDAO(db);

        // Inserisci utente di test
        Administrator testUser = new Administrator("testuser", "passwordHash", "nome", "cognome", UserStatusEnum.ACTIVE, new Timestamp(System.currentTimeMillis()));
        int userId = userDAO.insert(testUser);

        // Inserisci traccia collegata all'utente
        Track track = new Track(null, "Test Track", new Timestamp(System.currentTimeMillis()), userId);
        trackId = trackDAO.insert(track);


        // Create test genres to use in the tests
        app.tracktune.model.genre.Genre genre1 = new app.tracktune.model.genre.Genre("Test Genre 1", "Test Description 1");
        app.tracktune.model.genre.Genre genre2 = new app.tracktune.model.genre.Genre("Test Genre 2", "Test Description 2");
        app.tracktune.model.genre.Genre genre3 = new app.tracktune.model.genre.Genre("Test Genre 3", "Test Description 3");
        genreId1 = genreDAO.insert(genre1);
        genreId2 = genreDAO.insert(genre2);
        genreId3 = genreDAO.insert(genre3);

        System.out.println("[DEBUG_LOG] Track ID: " + trackId);
        System.out.println("[DEBUG_LOG] Genre ID 1: " + genreId1);
        System.out.println("[DEBUG_LOG] Genre ID 2: " + genreId2);
        System.out.println("[DEBUG_LOG] Genre ID 3: " + genreId3);
    }

    @Test
    void testInsertAndGetById() {
        // Create a track-genre relationship using the actual IDs
        TrackGenre trackGenre = new TrackGenre(trackId, genreId1);
        Integer id = trackGenreDAO.insert(trackGenre);
        assertNotNull(id);

        TrackGenre fetched = trackGenreDAO.getById(id);
        assertEquals(trackId, fetched.getTrackId());
        assertEquals(genreId1, fetched.getGenreId());
    }

    @Test
    void testUpdate() {
        // Create a track-genre relationship
        TrackGenre trackGenre = new TrackGenre(trackId, genreId1);
        Integer id = trackGenreDAO.insert(trackGenre);

        // Update it to use a different genre
        TrackGenre updated = new TrackGenre(id, trackId, genreId2);
        trackGenreDAO.updateById(updated, id);

        TrackGenre result = trackGenreDAO.getById(id);
        assertEquals(trackId, result.getTrackId());
        assertEquals(genreId2, result.getGenreId());
    }

    @Test
    void testDelete() {
        TrackGenre trackGenre = new TrackGenre(trackId, genreId1);
        Integer id = trackGenreDAO.insert(trackGenre);

        trackGenreDAO.deleteById(id);

        // The getById method throws an exception when the track-genre relationship is not found
        assertThrows(app.tracktune.exceptions.SQLiteException.class, () -> {
            trackGenreDAO.getById(id);
        });
    }

    @Test
    void testGetAll() {
        TrackGenre tg1 = new TrackGenre(trackId, genreId1);
        TrackGenre tg2 = new TrackGenre(trackId, genreId2);
        trackGenreDAO.insert(tg1);
        trackGenreDAO.insert(tg2);

        List<TrackGenre> all = trackGenreDAO.getAll();
        assertTrue(all.size() >= 2);
    }

    @Test
    void testGetByTrackId() {
        TrackGenre tg1 = new TrackGenre(trackId, genreId1);
        TrackGenre tg2 = new TrackGenre(trackId, genreId2);
        trackGenreDAO.insert(tg1);
        trackGenreDAO.insert(tg2);

        List<TrackGenre> byTrackId = trackGenreDAO.getByTrackId(trackId);
        assertTrue(byTrackId.size() >= 2);
    }

    @Test
    void testGetByTrackIdAndGenreId() {
        TrackGenre tg = new TrackGenre(trackId, genreId3);
        trackGenreDAO.insert(tg);

        TrackGenre result = trackGenreDAO.getByTrackIdAndGenreId(trackId, genreId3);
        assertNotNull(result);
        assertEquals(trackId, result.getTrackId());
        assertEquals(genreId3, result.getGenreId());
    }
}
