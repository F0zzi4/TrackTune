package app.tracktune.model.author;

import app.tracktune.model.DatabaseManager;
import app.tracktune.utils.DBInit;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the AuthorDAO class.
 * Tests include insertion, retrieval, update, deletion, and queries related to authors.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AuthorDAOTest {

    private AuthorDAO authorDAO;

    /**
     * Initializes an in-memory SQLite database and sets up the AuthorDAO before all tests.
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
        authorDAO = new AuthorDAO(db);
    }

    /**
     * Tests insertion and retrieval of an Author by its ID.
     */
    @Test
    void testInsertAndGetById() {
        Author author = new Author(null, "Test Author", AuthorStatusEnum.ACTIVE);
        Integer id = authorDAO.insert(author);
        assertNotNull(id);

        Author fetched = authorDAO.getById(id);
        assertEquals("Test Author", fetched.getAuthorshipName());
        assertEquals(AuthorStatusEnum.ACTIVE, fetched.getStatus());
    }

    /**
     * Tests the update of an existing Author by ID.
     */
    @Test
    void testUpdate() {
        Author author = new Author(null, "Initial Author", AuthorStatusEnum.ACTIVE);
        Integer id = authorDAO.insert(author);

        Author updated = new Author(id, "Updated Author", AuthorStatusEnum.REMOVED);
        authorDAO.updateById(updated, id);

        Author result = authorDAO.getById(id);
        assertEquals("Updated Author", result.getAuthorshipName());
        assertEquals(AuthorStatusEnum.REMOVED, result.getStatus());
    }

    /**
     * Tests the deletion of an Author by ID.
     * Expects an exception when trying to retrieve a deleted author.
     */
    @Test
    void testDelete() {
        Author author = new Author(null, "To Delete", AuthorStatusEnum.ACTIVE);
        Integer id = authorDAO.insert(author);

        authorDAO.deleteById(id);

        assertThrows(app.tracktune.exceptions.SQLiteException.class, () -> authorDAO.getById(id));
    }

    /**
     * Tests retrieval of all authors.
     */
    @Test
    void testGetAll() {
        Author a1 = new Author(null, "A1", AuthorStatusEnum.ACTIVE);
        Author a2 = new Author(null, "A2", AuthorStatusEnum.REMOVED);
        authorDAO.insert(a1);
        authorDAO.insert(a2);

        List<Author> all = authorDAO.getAll();
        assertTrue(all.size() >= 2);
    }

    /**
     * Tests retrieval of all active authors.
     */
    @Test
    void testGetAllActive() {
        Author a1 = new Author(null, "Active1", AuthorStatusEnum.ACTIVE);
        Author a2 = new Author(null, "Inactive1", AuthorStatusEnum.REMOVED);
        authorDAO.insert(a1);
        authorDAO.insert(a2);

        List<Author> active = authorDAO.getAllActive();
        assertTrue(active.stream().allMatch(a -> a.getStatus() == AuthorStatusEnum.ACTIVE));
    }

    /**
     * Tests if the DAO correctly checks the existence of an author by name.
     */
    @Test
    void testExistByAuthorshipName() {
        Author author = new Author(null, "UniqueName", AuthorStatusEnum.ACTIVE);
        authorDAO.insert(author);

        boolean exists = authorDAO.existByAuthorshipName("UniqueName");
        assertTrue(exists);
    }

    /**
     * Tests retrieval of all authors associated with a specific track ID.
     * Currently, checks that the method returns a non-null list.
     */
    @Test
    void testGetAllAuthorsByTrackId() {
        List<Author> authors = authorDAO.getAllAuthorsByTrackId(1);
        assertNotNull(authors);
    }
}
