package app.tracktune.model.user;

import app.tracktune.Main;
import app.tracktune.exceptions.SQLiteException;
import app.tracktune.interfaces.DAO;
import app.tracktune.utils.DatabaseManager;
import app.tracktune.utils.Strings;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Data Access Object for the PendingUser entity.
 * Provides CRUD operations and specific queries to interact with the PendingUsers table.
 */
public class PendingUserDAO implements DAO<PendingUser> {
    private static final String ID = "ID";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String NAME = "name";
    private static final String SURNAME = "surname";
    private static final String STATUS = "status";
    private static final String REQUEST_DATE = "requestDate";

    private final DatabaseManager dbManager;

    private static final String INSERT_PENDING_USER_STMT = """
        INSERT INTO PendingUsers (username, password, name, surname, status, requestDate)
        VALUES (?, ?, ?, ?, ?, ?)
    """;

    private static final String UPDATE_PENDING_USER_STMT = """
        UPDATE PendingUsers
        SET password = ?,
            name = ?,
            surname = ?,
            status = ?
        WHERE ID = ?
    """;

    private static final String UPDATE_USERNAME_BY_USERNAME_STMT = """
        UPDATE PendingUsers
        SET username = ?
        WHERE username = ?
    """;

    private static final String DELETE_PENDING_USER_STMT = """
        DELETE FROM PendingUsers
        WHERE ID = ?
    """;

    private static final String GET_ALL_PENDING_USERS_STMT = """
        SELECT *
        FROM PendingUsers
    """;

    private static final String GET_PENDING_USER_BY_ID = """
        SELECT *
        FROM PendingUsers
        WHERE ID = ?
    """;

    private static final String GET_PENDING_USER_BY_USERNAME_STMT = """
        SELECT *
        FROM PendingUsers
        WHERE username = ?
    """;

    /**
     * Default constructor using the main application's DatabaseManager instance.
     */
    public PendingUserDAO() {
        this.dbManager = Main.dbManager;
    }

    /**
     * Constructor allowing to specify a custom DatabaseManager instance.
     *
     * @param dbManager DatabaseManager instance to be used
     */
    public PendingUserDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Inserts a new PendingUser into the database.
     *
     * @param pendingUser the PendingUser object to insert
     * @return the generated ID of the new record
     * @throws SQLiteException if the insertion fails
     */
    @Override
    public Integer insert(PendingUser pendingUser) {
        boolean success = dbManager.executeUpdate(
                INSERT_PENDING_USER_STMT,
                pendingUser.getUsername(),
                pendingUser.getPassword(),
                pendingUser.getName(),
                pendingUser.getSurname(),
                pendingUser.getStatus().ordinal(),
                pendingUser.getRequestDate()
        );

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }
        return dbManager.getLastInsertId();
    }

    /**
     * Updates an existing PendingUser in the database by its ID.
     *
     * @param user the PendingUser object with updated data
     * @param id   the ID of the PendingUser to update
     * @throws SQLiteException if the update fails
     */
    @Override
    public void updateById(PendingUser user, int id) {
        boolean success = dbManager.executeUpdate(
                UPDATE_PENDING_USER_STMT,
                user.getPassword(),
                user.getName(),
                user.getSurname(),
                user.getStatus().ordinal(),
                id
        );

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }
    }

    /**
     * Updates an existing PendingUser in the database by its username.
     *
     * @param newUsername the newUsername
     * @param lastUsername the lastUsername
     * @throws SQLiteException if the update fails
     */
    public void updateUsername(String newUsername, String lastUsername) {
        boolean success = dbManager.executeUpdate(
                UPDATE_USERNAME_BY_USERNAME_STMT,
                newUsername,
                lastUsername
        );

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }
    }

    /**
     * Deletes a PendingUser by its ID.
     *
     * @param id the ID of the PendingUser to delete
     * @throws SQLiteException if the deletion fails
     */
    @Override
    public void deleteById(int id) {
        boolean success = dbManager.executeUpdate(
                DELETE_PENDING_USER_STMT,
                id
        );

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }
    }

    /**
     * Retrieves a PendingUser by its ID.
     *
     * @param id the ID of the PendingUser to retrieve
     * @return the PendingUser object, or null if not found
     * @throws SQLiteException if the query fails
     */
    @Override
    public PendingUser getById(int id) {
        AtomicReference<PendingUser> result = new AtomicReference<>();

        boolean success = dbManager.executeQuery(GET_PENDING_USER_BY_ID,
                rs -> {
                    if (rs.next()) {
                        result.set(mapResultSetToEntity(rs));
                        return true;
                    }
                    return false;
                }, id);

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }

        return result.get();
    }

    /**
     * Retrieves all PendingUser records.
     *
     * @return list of all PendingUsers
     */
    @Override
    public List<PendingUser> getAll() {
        List<PendingUser> users = new ArrayList<>();

        dbManager.executeQuery(GET_ALL_PENDING_USERS_STMT,
                rs -> {
                    while (rs.next()) {
                        users.add(mapResultSetToEntity(rs));
                    }
                    return null;
                });

        return users;
    }

    /**
     * Retrieves a PendingUser by username.
     *
     * @param username the username to search for
     * @return the PendingUser object, or null if not found
     */
    public PendingUser getByUsername(String username) {
        AtomicReference<PendingUser> pendUser = new AtomicReference<>();

        dbManager.executeQuery(GET_PENDING_USER_BY_USERNAME_STMT,
                rs -> {
                    if (rs.next()) {
                        pendUser.set(mapResultSetToEntity(rs));
                    }
                    return null;
                }, username);

        return pendUser.get();
    }

    /**
     * Helper method to convert a ResultSet row into a PendingUser object.
     *
     * @param rs the ResultSet positioned at a valid row
     * @return a PendingUser object with the data from the current row
     * @throws SQLException if a database access error occurs
     */
    private PendingUser mapResultSetToEntity(ResultSet rs) throws SQLException {
        return new PendingUser(
                rs.getInt(ID),
                rs.getString(USERNAME),
                rs.getString(PASSWORD),
                rs.getString(NAME),
                rs.getString(SURNAME),
                rs.getTimestamp(REQUEST_DATE),
                AuthRequestStatusEnum.fromInt(rs.getInt(STATUS))
        );
    }
}
