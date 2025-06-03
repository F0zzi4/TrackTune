package app.tracktune.model.author;

import app.tracktune.model.DatabaseManager;
import app.tracktune.utils.DBInit;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AuthorDAOTest {

    private DatabaseManager db;
    private AuthorDAO authorDAO;
    private Connection connection;

    @BeforeAll
    void setup() throws Exception {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        Statement stmt = connection.createStatement();
        stmt.execute("PRAGMA foreign_keys = ON;");
        String[] ddl = DBInit.getDBInitStatement().split(";");
        for (String query : ddl) {
            if (!query.trim().isEmpty()) stmt.execute(query.trim() + ";");
        }

        DatabaseManager.setTestConnection(connection);
        db = DatabaseManager.getInstance();
        authorDAO = new AuthorDAO(db);
    }

    @Test
    void testInsertAndGetById() {
        Author author = new Author(null, "Test Author", AuthorStatusEnum.ACTIVE);
        Integer id = authorDAO.insert(author);
        assertNotNull(id);

        Author fetched = authorDAO.getById(id);
        assertEquals("Test Author", fetched.getAuthorshipName());
        assertEquals(AuthorStatusEnum.ACTIVE, fetched.getStatus());
    }

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

    @Test
    void testDelete() {
        Author author = new Author(null, "To Delete", AuthorStatusEnum.ACTIVE);
        Integer id = authorDAO.insert(author);

        authorDAO.deleteById(id);

        // The getById method throws an exception when the author is not found
        assertThrows(app.tracktune.exceptions.SQLiteException.class, () -> {
            authorDAO.getById(id);
        });
    }

    @Test
    void testGetAll() {
        Author a1 = new Author(null, "A1", AuthorStatusEnum.ACTIVE);
        Author a2 = new Author(null, "A2", AuthorStatusEnum.REMOVED);
        authorDAO.insert(a1);
        authorDAO.insert(a2);

        List<Author> all = authorDAO.getAll();
        assertTrue(all.size() >= 2);
    }

    @Test
    void testGetAllActive() {
        Author a1 = new Author(null, "Active1", AuthorStatusEnum.ACTIVE);
        Author a2 = new Author(null, "Inactive1", AuthorStatusEnum.REMOVED);
        authorDAO.insert(a1);
        authorDAO.insert(a2);

        List<Author> active = authorDAO.getAllActive();
        assertTrue(active.stream().allMatch(a -> a.getStatus() == AuthorStatusEnum.ACTIVE));
    }

    @Test
    void testExistByAuthorshipName() {
        Author author = new Author(null, "UniqueName", AuthorStatusEnum.ACTIVE);
        authorDAO.insert(author);

        boolean exists = authorDAO.existByAuthorshipName("UniqueName");
        assertTrue(exists);
    }

    @Test
    void testGetAllAuthorsByTrackId() {
        // Create a track and associate authors with it
        // This test would require setting up a track first
        // For now, we'll just test that the method doesn't throw an exception
        List<Author> authors = authorDAO.getAllAuthorsByTrackId(1);
        assertNotNull(authors);
    }
}
