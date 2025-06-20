package app.tracktune.model.user;

import app.tracktune.model.DatabaseManager;
import app.tracktune.utils.DBInit;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for PendingUserDAO.
 * It verifies the correctness of CRUD operations on pending users.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PendingUserDAOTest {

    private PendingUserDAO pendingUserDAO;

    @BeforeAll
    void setup() throws Exception {
        Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        Statement stmt = connection.createStatement();
        stmt.execute("PRAGMA foreign_keys = ON;");
        String[] ddl = DBInit.getDBInitStatement().split(";");
        for (String query : ddl) {
            if (!query.trim().isEmpty()) {
                stmt.execute(query.trim() + ";");
            }
        }

        DatabaseManager.setTestConnection(connection);
        DatabaseManager db = DatabaseManager.getInstance();
        pendingUserDAO = new PendingUserDAO(db);
    }

    /**
     * Tests insertion and retrieval of a PendingUser by ID.
     */
    @Test
    @Order(1)
    void testInsertAndGetById() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        PendingUser user = new PendingUser("testUser", "password123", "Test", "User", now, AuthRequestStatusEnum.CREATED);
        Integer id = pendingUserDAO.insert(user);
        assertNotNull(id);

        PendingUser fetched = pendingUserDAO.getById(id);
        assertEquals("testUser", fetched.getUsername());
        assertEquals("password123", fetched.getPassword());
        assertEquals("Test", fetched.getName());
        assertEquals("User", fetched.getSurname());
        assertEquals(AuthRequestStatusEnum.CREATED, fetched.getStatus());
    }

    /**
     * Tests updating a PendingUser record.
     */
    @Test
    @Order(2)
    void testUpdate() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        PendingUser user = new PendingUser("updateUser", "initialPass", "Initial", "User", now, AuthRequestStatusEnum.CREATED);
        Integer id = pendingUserDAO.insert(user);

        PendingUser updated = new PendingUser(id, "updateUser", "updatedPass", "Updated", "User", now, AuthRequestStatusEnum.ACCEPTED);
        pendingUserDAO.updateById(updated, id);

        PendingUser result = pendingUserDAO.getById(id);
        assertEquals("updateUser", result.getUsername());
        assertEquals("updatedPass", result.getPassword());
        assertEquals("Updated", result.getName());
        assertEquals(AuthRequestStatusEnum.ACCEPTED, result.getStatus());
    }

    /**
     * Tests deletion of a PendingUser record.
     */
    @Test
    @Order(3)
    void testDelete() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        PendingUser user = new PendingUser("deleteUser", "password123", "Delete", "User", now, AuthRequestStatusEnum.CREATED);
        Integer id = pendingUserDAO.insert(user);

        pendingUserDAO.deleteById(id);

        assertThrows(app.tracktune.exceptions.SQLiteException.class, () -> pendingUserDAO.getById(id));
    }

    /**
     * Tests retrieval of all PendingUser records.
     */
    @Test
    @Order(4)
    void testGetAll() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        PendingUser user1 = new PendingUser("user1", "password1", "User", "One", now, AuthRequestStatusEnum.CREATED);
        PendingUser user2 = new PendingUser("user2", "password2", "User", "Two", now, AuthRequestStatusEnum.REJECTED);
        pendingUserDAO.insert(user1);
        pendingUserDAO.insert(user2);

        List<PendingUser> all = pendingUserDAO.getAll();
        assertTrue(all.size() >= 2);
    }

    /**
     * Tests retrieval of a PendingUser by username.
     */
    @Test
    @Order(5)
    void testGetByUsername() {
        String uniqueUsername = "unique" + System.currentTimeMillis();
        Timestamp now = new Timestamp(System.currentTimeMillis());
        PendingUser user = new PendingUser(uniqueUsername, "password123", "Unique", "User", now, AuthRequestStatusEnum.CREATED);
        pendingUserDAO.insert(user);

        PendingUser fetched = pendingUserDAO.getByUsername(uniqueUsername);
        assertNotNull(fetched);
        assertEquals(uniqueUsername, fetched.getUsername());
        assertEquals("Unique", fetched.getName());
        assertEquals("User", fetched.getSurname());
        assertEquals(AuthRequestStatusEnum.CREATED, fetched.getStatus());
    }
}
