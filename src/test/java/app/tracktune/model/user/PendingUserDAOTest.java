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

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PendingUserDAOTest {

    private DatabaseManager db;
    private PendingUserDAO pendingUserDAO;

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
        pendingUserDAO = new PendingUserDAO(db);
    }

    @Test
    void testInsertAndGetById() {
        // Create a pending user
        Timestamp now = new Timestamp(System.currentTimeMillis());
        PendingUser user = new PendingUser("testuser", "password123", "Test", "User", now, AuthRequestStatusEnum.CREATED);
        Integer id = pendingUserDAO.insert(user);
        assertNotNull(id);

        // Get the user by ID
        PendingUser fetched = pendingUserDAO.getById(id);
        assertEquals("testuser", fetched.getUsername());
        assertEquals("password123", fetched.getPassword());
        assertEquals("Test", fetched.getName());
        assertEquals("User", fetched.getSurname());
        assertEquals(AuthRequestStatusEnum.CREATED, fetched.getStatus());
    }

    @Test
    void testUpdate() {
        // Create a pending user
        Timestamp now = new Timestamp(System.currentTimeMillis());
        PendingUser user = new PendingUser("updateuser", "initialpass", "Initial", "User", now, AuthRequestStatusEnum.CREATED);
        Integer id = pendingUserDAO.insert(user);

        // Update the user
        PendingUser updated = new PendingUser(id, "updateuser", "updatedpass", "Updated", "User", now, AuthRequestStatusEnum.ACCEPTED);
        pendingUserDAO.updateById(updated, id);

        // Get the updated user
        PendingUser result = pendingUserDAO.getById(id);
        assertEquals("updateuser", result.getUsername());
        assertEquals("updatedpass", result.getPassword());
        assertEquals("Updated", result.getName());
        assertEquals(AuthRequestStatusEnum.ACCEPTED, result.getStatus());
    }

    @Test
    void testDelete() {
        // Create a pending user
        Timestamp now = new Timestamp(System.currentTimeMillis());
        PendingUser user = new PendingUser("deleteuser", "password123", "Delete", "User", now, AuthRequestStatusEnum.CREATED);
        Integer id = pendingUserDAO.insert(user);

        // Delete the user
        pendingUserDAO.deleteById(id);

        // The getById method should throw an exception when the user is not found
        assertThrows(app.tracktune.exceptions.SQLiteException.class, () -> {
            pendingUserDAO.getById(id);
        });
    }

    @Test
    void testGetAll() {
        // Create multiple pending users
        Timestamp now = new Timestamp(System.currentTimeMillis());
        PendingUser user1 = new PendingUser("user1", "password1", "User", "One", now, AuthRequestStatusEnum.CREATED);
        PendingUser user2 = new PendingUser("user2", "password2", "User", "Two", now, AuthRequestStatusEnum.REJECTED);
        pendingUserDAO.insert(user1);
        pendingUserDAO.insert(user2);

        // Get all pending users
        List<PendingUser> all = pendingUserDAO.getAll();
        assertTrue(all.size() >= 2);
    }

    @Test
    void testGetByUsername() {
        // Create a pending user with a unique username
        String uniqueUsername = "unique" + System.currentTimeMillis();
        Timestamp now = new Timestamp(System.currentTimeMillis());
        PendingUser user = new PendingUser(uniqueUsername, "password123", "Unique", "User", now, AuthRequestStatusEnum.CREATED);
        pendingUserDAO.insert(user);

        // Get the user by username
        PendingUser fetched = pendingUserDAO.getByUsername(uniqueUsername);
        assertNotNull(fetched);
        assertEquals(uniqueUsername, fetched.getUsername());
        assertEquals("Unique", fetched.getName());
        assertEquals("User", fetched.getSurname());
        assertEquals(AuthRequestStatusEnum.CREATED, fetched.getStatus());
    }
}