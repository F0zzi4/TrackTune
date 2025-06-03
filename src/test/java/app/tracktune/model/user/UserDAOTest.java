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
public class UserDAOTest {

    private DatabaseManager db;
    private UserDAO userDAO;

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
        userDAO = new UserDAO(db);
    }

    @Test
    void testInsertAndGetByIdAuthenticatedUser() {
        // Create an authenticated user
        Timestamp now = new Timestamp(System.currentTimeMillis());
        AuthenticatedUser user = new AuthenticatedUser(null, "testuser", "password123", "Test", "User", UserStatusEnum.ACTIVE, now);
        Integer id = userDAO.insert(user);
        assertNotNull(id);

        // Get the user by ID
        User fetched = userDAO.getById(id);
        assertTrue(fetched instanceof AuthenticatedUser);
        AuthenticatedUser authUser = (AuthenticatedUser) fetched;
        assertEquals("testuser", authUser.getUsername());
        assertEquals("password123", authUser.getPassword());
        assertEquals("Test", authUser.getName());
        assertEquals("User", authUser.getSurname());
        assertEquals(UserStatusEnum.ACTIVE, authUser.getStatus());
        assertFalse(fetched instanceof Administrator);
    }

    @Test
    void testInsertAndGetByIdAdministrator() {
        // Create an administrator
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Administrator admin = new Administrator(null, "adminuser", "adminpass", "Admin", "User", UserStatusEnum.ACTIVE, now);
        Integer id = userDAO.insert(admin);
        assertNotNull(id);

        // Get the user by ID
        User fetched = userDAO.getById(id);
        assertTrue(fetched instanceof Administrator);
        Administrator fetchedAdmin = (Administrator) fetched;
        assertEquals("adminuser", fetchedAdmin.getUsername());
        assertEquals("adminpass", fetchedAdmin.getPassword());
        assertEquals("Admin", fetchedAdmin.getName());
        assertEquals("User", fetchedAdmin.getSurname());
        assertEquals(UserStatusEnum.ACTIVE, fetchedAdmin.getStatus());
    }

    @Test
    void testUpdateAuthenticatedUser() {
        // Create an authenticated user
        Timestamp now = new Timestamp(System.currentTimeMillis());
        AuthenticatedUser user = new AuthenticatedUser(null, "updateuser", "initialpass", "Initial", "User", UserStatusEnum.ACTIVE, now);
        Integer id = userDAO.insert(user);

        // Update the user
        AuthenticatedUser updated = new AuthenticatedUser(id, "updateuser", "updatedpass", "Updated", "User", UserStatusEnum.SUSPENDED, now);
        userDAO.updateById(updated, id);

        // Get the updated user
        User result = userDAO.getById(id);
        assertTrue(result instanceof AuthenticatedUser);
        AuthenticatedUser authUser = (AuthenticatedUser) result;
        assertEquals("updateuser", authUser.getUsername());
        assertEquals("updatedpass", authUser.getPassword());
        assertEquals("Updated", authUser.getName());
        assertEquals(UserStatusEnum.SUSPENDED, authUser.getStatus());
    }

    @Test
    void testUpdateAdministrator() {
        // Create an administrator
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Administrator admin = new Administrator(null, "updateadmin", "initialpass", "Initial", "Admin", UserStatusEnum.ACTIVE, now);
        Integer id = userDAO.insert(admin);

        // Update the administrator
        Administrator updated = new Administrator(id, "updateadmin", "updatedpass", "Updated", "Admin", UserStatusEnum.SUSPENDED, now);
        userDAO.updateById(updated, id);

        // Get the updated administrator
        User result = userDAO.getById(id);
        assertTrue(result instanceof Administrator);
        Administrator fetchedAdmin = (Administrator) result;
        assertEquals("updateadmin", fetchedAdmin.getUsername());
        assertEquals("updatedpass", fetchedAdmin.getPassword());
        assertEquals("Updated", fetchedAdmin.getName());
        assertEquals(UserStatusEnum.SUSPENDED, fetchedAdmin.getStatus());
    }

    @Test
    void testDelete() {
        // Create a user
        Timestamp now = new Timestamp(System.currentTimeMillis());
        AuthenticatedUser user = new AuthenticatedUser(null, "deleteuser", "password123", "Delete", "User", UserStatusEnum.ACTIVE, now);
        Integer id = userDAO.insert(user);

        // Delete the user
        userDAO.deleteById(id);

        // The getById method should throw an exception when the user is not found
        assertThrows(app.tracktune.exceptions.SQLiteException.class, () -> {
            userDAO.getById(id);
        });
    }

    @Test
    void testGetAll() {
        // Create multiple users
        Timestamp now = new Timestamp(System.currentTimeMillis());
        AuthenticatedUser user1 = new AuthenticatedUser(null, "user1", "password1", "User", "One", UserStatusEnum.ACTIVE, now);
        Administrator admin1 = new Administrator(null, "admin1", "password2", "Admin", "One", UserStatusEnum.ACTIVE, now);
        userDAO.insert(user1);
        userDAO.insert(admin1);

        // Get all users
        List<User> all = userDAO.getAll();
        assertTrue(all.size() >= 2);
        assertTrue(all.stream().anyMatch(u -> u.getUsername().equals("user1")));
        assertTrue(all.stream().anyMatch(u -> u.getUsername().equals("admin1")));
    }

    @Test
    void testGetActiveUserByUsername() {
        // Create a user with a unique username
        String uniqueUsername = "unique" + System.currentTimeMillis();
        Timestamp now = new Timestamp(System.currentTimeMillis());
        AuthenticatedUser user = new AuthenticatedUser(null, uniqueUsername, "password123", "Unique", "User", UserStatusEnum.ACTIVE, now);
        userDAO.insert(user);

        // Get the user by username
        User fetched = userDAO.getActiveUserByUsername(uniqueUsername);
        assertNotNull(fetched);
        assertEquals(uniqueUsername, fetched.getUsername());
        assertEquals("Unique", fetched.getName());
        assertEquals("User", fetched.getSurname());
        assertTrue(fetched instanceof AuthenticatedUser);
        assertEquals(UserStatusEnum.ACTIVE, ((AuthenticatedUser) fetched).getStatus());
    }

    @Test
    void testGetActiveUserByUsernameWithInactiveUser() {
        // Create an inactive user with a unique username
        String uniqueUsername = "inactive" + System.currentTimeMillis();
        Timestamp now = new Timestamp(System.currentTimeMillis());
        AuthenticatedUser user = new AuthenticatedUser(null, uniqueUsername, "password123", "Inactive", "User", UserStatusEnum.SUSPENDED, now);
        userDAO.insert(user);

        // Get the user by username - should return null for inactive users
        User fetched = userDAO.getActiveUserByUsername(uniqueUsername);
        assertNull(fetched);
    }
}