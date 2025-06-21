package app.tracktune.model.user;

import app.tracktune.utils.DatabaseManager;
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

    private UserDAO userDAO;

    @BeforeAll
    void setup() throws Exception {
        Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
            String[] ddl = DBInit.getDBInitStatement().split(";");
            for (String query : ddl) {
                if (!query.trim().isEmpty()) {
                    stmt.execute(query.trim() + ";");
                }
            }
        }
        DatabaseManager.setTestConnection(connection);
        DatabaseManager db = DatabaseManager.getInstance();
        userDAO = new UserDAO(db);
    }

    /**
     * Verifies the entry and retrieval of an AuthenticatedUser by ID.
     */
    @Test
    void insertAndGetById_AuthenticatedUser() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        AuthenticatedUser user = new AuthenticatedUser(null, "testUser", "password123", "Test", "User", UserStatusEnum.ACTIVE, now);
        Integer id = userDAO.insert(user);
        assertNotNull(id);

        User fetched = userDAO.getById(id);
        assertInstanceOf(AuthenticatedUser.class, fetched);
        AuthenticatedUser authUser = (AuthenticatedUser) fetched;
        assertEquals("testUser", authUser.getUsername());
        assertEquals("password123", authUser.getPassword());
        assertEquals("Test", authUser.getName());
        assertEquals("User", authUser.getSurname());
        assertEquals(UserStatusEnum.ACTIVE, authUser.getStatus());
        assertFalse(fetched instanceof Administrator);
    }

    /**
     * Verifies the entry and retrieval of an Administrator by ID.
     */
    @Test
    void insertAndGetById_Administrator() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Administrator admin = new Administrator(null, "adminUser", "adminPass", "Admin", "User", UserStatusEnum.ACTIVE, now);
        Integer id = userDAO.insert(admin);
        assertNotNull(id);

        User fetched = userDAO.getById(id);
        assertInstanceOf(Administrator.class, fetched);
        Administrator fetchedAdmin = (Administrator) fetched;
        assertEquals("adminUser", fetchedAdmin.getUsername());
        assertEquals("adminPass", fetchedAdmin.getPassword());
        assertEquals("Admin", fetchedAdmin.getName());
        assertEquals("User", fetchedAdmin.getSurname());
        assertEquals(UserStatusEnum.ACTIVE, fetchedAdmin.getStatus());
    }

    /**
     * Verify the update of an existing AuthenticatedUser.
     */
    @Test
    void update_AuthenticatedUser() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        AuthenticatedUser user = new AuthenticatedUser(null, "updateUser", "initialPass", "Initial", "User", UserStatusEnum.ACTIVE, now);
        Integer id = userDAO.insert(user);

        AuthenticatedUser updated = new AuthenticatedUser(id, "updateUser", "updatedPass", "Updated", "User", UserStatusEnum.SUSPENDED, now);
        userDAO.updateById(updated, id);

        User result = userDAO.getById(id);
        assertInstanceOf(AuthenticatedUser.class, result);
        AuthenticatedUser authUser = (AuthenticatedUser) result;
        assertEquals("updateUser", authUser.getUsername());
        assertEquals("updatedPass", authUser.getPassword());
        assertEquals("Updated", authUser.getName());
        assertEquals(UserStatusEnum.SUSPENDED, authUser.getStatus());
    }

    /**
     * Verify the update of an existing Administrator.
     */
    @Test
    void update_Administrator() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Administrator admin = new Administrator(null, "updateAdmin", "initialPass", "Initial", "Admin", UserStatusEnum.ACTIVE, now);
        Integer id = userDAO.insert(admin);

        Administrator updated = new Administrator(id, "updateAdmin", "updatedPass", "Updated", "Admin", UserStatusEnum.SUSPENDED, now);
        userDAO.updateById(updated, id);

        User result = userDAO.getById(id);
        assertInstanceOf(Administrator.class, result);
        Administrator fetchedAdmin = (Administrator) result;
        assertEquals("updateAdmin", fetchedAdmin.getUsername());
        assertEquals("updatedPass", fetchedAdmin.getPassword());
        assertEquals("Updated", fetchedAdmin.getName());
        assertEquals(UserStatusEnum.SUSPENDED, fetchedAdmin.getStatus());
    }

    /**
     * Verifies the deletion of a user by ID and that subsequent retrieval throws an exception.
     */
    @Test
    void deleteById_UserNotFound() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        AuthenticatedUser user = new AuthenticatedUser(null, "deleteUser", "password123", "Delete", "User", UserStatusEnum.ACTIVE, now);
        Integer id = userDAO.insert(user);

        userDAO.deleteById(id);

        assertThrows(app.tracktune.exceptions.SQLiteException.class, () -> userDAO.getById(id));
    }

    /**
     * Verify that the getAll method returns all users entered.
     */
    @Test
    void getAllUsers() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        AuthenticatedUser user1 = new AuthenticatedUser(null, "user1", "password1", "User", "One", UserStatusEnum.ACTIVE, now);
        Administrator admin1 = new Administrator(null, "admin1", "password2", "Admin", "One", UserStatusEnum.ACTIVE, now);
        userDAO.insert(user1);
        userDAO.insert(admin1);

        List<User> all = userDAO.getAll();
        assertTrue(all.size() >= 2);
        assertTrue(all.stream().anyMatch(u -> u.getUsername().equals("user1")));
        assertTrue(all.stream().anyMatch(u -> u.getUsername().equals("admin1")));
    }

    /**
     * Check recovery of an active user by username.
     */
    @Test
    void getActiveUserByUsername_ReturnsUser() {
        String uniqueUsername = "unique" + System.currentTimeMillis();
        Timestamp now = new Timestamp(System.currentTimeMillis());
        AuthenticatedUser user = new AuthenticatedUser(null, uniqueUsername, "password123", "Unique", "User", UserStatusEnum.ACTIVE, now);
        userDAO.insert(user);

        User fetched = userDAO.getActiveUserByUsername(uniqueUsername);
        assertNotNull(fetched);
        assertEquals(uniqueUsername, fetched.getUsername());
        assertEquals("Unique", fetched.getName());
        assertEquals("User", fetched.getSurname());
        assertInstanceOf(AuthenticatedUser.class, fetched);
        assertEquals(UserStatusEnum.ACTIVE, ((AuthenticatedUser) fetched).getStatus());
    }

    /**
     * Verifies that retrieving an inactive user by username returns null.
     */
    @Test
    void getActiveUserByUsername_InactiveUserReturnsNull() {
        String uniqueUsername = "inactive" + System.currentTimeMillis();
        Timestamp now = new Timestamp(System.currentTimeMillis());
        AuthenticatedUser user = new AuthenticatedUser(null, uniqueUsername, "password123", "Inactive", "User", UserStatusEnum.SUSPENDED, now);
        userDAO.insert(user);

        User fetched = userDAO.getActiveUserByUsername(uniqueUsername);
        assertNull(fetched);
    }
}
