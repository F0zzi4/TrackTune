package app.tracktune.model.user;

import app.tracktune.Main;
import app.tracktune.exceptions.SQLiteException;
import app.tracktune.interfaces.DAO;
import app.tracktune.utils.DatabaseManager;
import app.tracktune.utils.Strings;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Data Access Object (DAO) for managing User entities in the database.
 * Supports CRUD operations for Users, including Administrators and AuthenticatedUsers.
 */
public class UserDAO implements DAO<User> {
    private final DatabaseManager dbManager;

    private static final String ID = "ID";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String NAME = "name";
    private static final String SURNAME = "surname";
    private static final String STATUS = "status";
    private static final String CREATION_DATE = "creationDate";
    private static final String IS_ADMIN = "isAdmin";

    private static final String INSERT_USER_STMT = """
        INSERT INTO Users (username, password, name, surname, status, creationDate, isAdmin)
        VALUES (?, ?, ?, ?, ?, ?, ?)
    """;

    private static final String UPDATE_USER_STMT = """
        UPDATE Users
        SET username = ?,
            password = ?,
            name = ?,
            surname = ?,
            status = ?,
            creationDate = ?,
            isAdmin = ?
        WHERE ID = ?
    """;

    private static final String DELETE_USER_STMT = """
        DELETE FROM Users
        WHERE ID = ?
    """;

    private static final String GET_ALL_USERS_STMT = """
        SELECT *
        FROM Users
    """;

    private static final String GET_USER_BY_ID_STMT = """
        SELECT *
        FROM Users
        WHERE ID = ?
    """;

    private static final String GET_USER_BY_USERNAME_STMT = """
        SELECT *
        FROM Users
        WHERE username = ? AND status = 0
    """;

    /**
     * Default constructor using the global database manager.
     */
    public UserDAO() {
        this.dbManager = Main.dbManager;
    }

    /**
     * Constructor allowing to specify a custom DatabaseManager instance.
     *
     * @param dbManager the DatabaseManager instance to use for DB operations
     */
    public UserDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Inserts a new User into the database.
     * Handles both Administrator and AuthenticatedUser instances.
     *
     * @param user the User entity to insert
     * @return the ID of the newly inserted user
     * @throws SQLiteException if the insertion fails
     */
    @Override
    public Integer insert(User user) {
        boolean success = false;
        if (user instanceof Administrator admin) {
            success = dbManager.executeUpdate(
                    INSERT_USER_STMT,
                    admin.getUsername(),
                    admin.getPassword(),
                    admin.getName(),
                    admin.getSurname(),
                    admin.getStatus().ordinal(),
                    admin.getCreationDate(),
                    1
            );
        } else if (user instanceof AuthenticatedUser authUser) {
            success = dbManager.executeUpdate(
                    INSERT_USER_STMT,
                    authUser.getUsername(),
                    authUser.getPassword(),
                    authUser.getName(),
                    authUser.getSurname(),
                    authUser.getStatus().ordinal(),
                    authUser.getCreationDate(),
                    0
            );
        }

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }
        return dbManager.getLastInsertId();
    }

    /**
     * Updates an existing User by its ID.
     * Supports updating both Administrator and AuthenticatedUser instances.
     *
     * @param user the User entity with updated data
     * @param id   the ID of the user to update
     * @throws SQLiteException if the update fails
     */
    @Override
    public void updateById(User user, int id) {
        boolean success = false;
        if (user instanceof Administrator admin) {
            success = dbManager.executeUpdate(
                    UPDATE_USER_STMT,
                    admin.getUsername(),
                    admin.getPassword(),
                    admin.getName(),
                    admin.getSurname(),
                    admin.getStatus().ordinal(),
                    admin.getCreationDate(),
                    1,
                    id
            );
        } else if (user instanceof AuthenticatedUser authUser) {
            success = dbManager.executeUpdate(
                    UPDATE_USER_STMT,
                    authUser.getUsername(),
                    authUser.getPassword(),
                    authUser.getName(),
                    authUser.getSurname(),
                    authUser.getStatus().ordinal(),
                    authUser.getCreationDate(),
                    0,
                    id
            );
        }

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }
    }

    /**
     * Deletes a User by its ID.
     *
     * @param id the ID of the user to delete
     * @throws SQLiteException if the deletion fails
     */
    @Override
    public void deleteById(int id) {
        boolean success = dbManager.executeUpdate(DELETE_USER_STMT, id);

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }
    }

    /**
     * Retrieves a User by its ID.
     *
     * @param id the ID of the user to retrieve
     * @return the User object if found
     * @throws SQLiteException if no user is found or query fails
     */
    @Override
    public User getById(int id) {
        AtomicReference<User> userRef = new AtomicReference<>();

        boolean success = dbManager.executeQuery(GET_USER_BY_ID_STMT,
                rs -> {
                    if (rs.next()) {
                        userRef.set(mapResultSetToEntity(rs));
                        return true;
                    }
                    return false;
                }, id);

        if (!success) {
            throw new SQLiteException(Strings.ERR_USER_NOT_FOUND);
        }

        return userRef.get();
    }

    /**
     * Retrieves an active (status = 0) User by username.
     *
     * @param username the username of the user to retrieve
     * @return the User object if found; null otherwise
     */
    public User getActiveUserByUsername(String username) {
        AtomicReference<User> userRef = new AtomicReference<>();

        dbManager.executeQuery(GET_USER_BY_USERNAME_STMT,
                rs -> {
                    if (rs.next()) {
                        userRef.set(mapResultSetToEntity(rs));
                    }
                    return null;
                }, username);

        return userRef.get();
    }

    /**
     * Retrieves all users from the database.
     *
     * @return a list of all User objects
     */
    @Override
    public List<User> getAll() {
        List<User> users = new ArrayList<>();

        dbManager.executeQuery(GET_ALL_USERS_STMT,
                rs -> {
                    while (rs.next()) {
                        users.add(mapResultSetToEntity(rs));
                    }
                    return null;
                }
        );

        return users;
    }

    /**
     * Maps a ResultSet row to a User entity, returning either an Administrator
     * or AuthenticatedUser based on the 'isAdmin' flag.
     *
     * @param rs the ResultSet positioned at the current row
     * @return a User entity representing the data from the current row
     * @throws SQLException if any SQL error occurs
     */
    private User mapResultSetToEntity(ResultSet rs) throws SQLException {
        Integer id = rs.getInt(ID);
        String username = rs.getString(USERNAME);
        String password = rs.getString(PASSWORD);
        String name = rs.getString(NAME);
        String surname = rs.getString(SURNAME);
        UserStatusEnum status = UserStatusEnum.fromInt(rs.getInt(STATUS));
        Timestamp creationDate = rs.getTimestamp(CREATION_DATE);
        boolean isAdmin = rs.getInt(IS_ADMIN) == 1;

        if (isAdmin) {
            return new Administrator(id, username, password, name, surname, status, creationDate);
        } else {
            return new AuthenticatedUser(id, username, password, name, surname, status, creationDate);
        }
    }
}
