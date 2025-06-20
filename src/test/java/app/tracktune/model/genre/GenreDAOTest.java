package app.tracktune.model.genre;

import app.tracktune.utils.DatabaseManager;
import app.tracktune.utils.DBInit;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the GenreDAO class.
 * Verifies operations like insertion, update, deletion, and various retrievals.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GenreDAOTest {

    private GenreDAO genreDAO;

    /**
     * Sets up the in-memory SQLite database and initializes the GenreDAO.
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
        DatabaseManager db = DatabaseManager.getInstance();
        genreDAO = new GenreDAO(db);
    }

    /**
     * Tests insertion of a genre and retrieval by its ID.
     */
    @Test
    void testInsertAndGetById() {
        Genre genre = new Genre("Test Genre", "Test Description");
        Integer id = genreDAO.insert(genre);
        assertNotNull(id);

        Genre fetched = genreDAO.getById(id);
        assertEquals("Test Genre", fetched.getName());
        assertEquals("Test Description", fetched.getDescription());
    }

    /**
     * Tests updating a genre by ID and verifies the change.
     */
    @Test
    void testUpdate() {
        Genre genre = new Genre("Initial Genre", "Initial Description");
        Integer id = genreDAO.insert(genre);

        Genre updated = new Genre(id, "Updated Genre", "Updated Description");
        genreDAO.updateById(updated, id);

        Genre result = genreDAO.getById(id);
        assertEquals("Updated Genre", result.getName());
        assertEquals("Updated Description", result.getDescription());
    }

    /**
     * Tests deletion of a genre by ID and checks that it no longer exists.
     */
    @Test
    void testDelete() {
        Genre genre = new Genre("To Delete", "Delete Description");
        Integer id = genreDAO.insert(genre);

        genreDAO.deleteById(id);

        assertThrows(app.tracktune.exceptions.SQLiteException.class, () -> genreDAO.getById(id));
    }

    /**
     * Tests retrieval of all genres.
     */
    @Test
    void testGetAll() {
        Genre g1 = new Genre("G1", "Description 1");
        Genre g2 = new Genre("G2", "Description 2");
        genreDAO.insert(g1);
        genreDAO.insert(g2);

        List<Genre> all = genreDAO.getAll();
        assertTrue(all.size() >= 2);
    }

    /**
     * Tests retrieval of all genres used by at least one track.
     * For now, this test checks only that the method does not throw.
     */
    @Test
    void testGetAllUsed() {
        List<Genre> used = genreDAO.getAllUsed();
        assertNotNull(used);
    }

    /**
     * Tests retrieval of genres associated with a specific track ID.
     * For now, this test checks only that the method does not throw.
     */
    @Test
    void testGetAllGenresByTrackId() {
        List<Genre> genres = genreDAO.getAllGenresByTrackId(1);
        assertNotNull(genres);
    }
}
