package app.tracktune.model.user;

import app.tracktune.Main;
import app.tracktune.exceptions.EntityAlreadyExistsException;
import app.tracktune.interfaces.DAO;
import app.tracktune.model.DatabaseManager;
import app.tracktune.utils.Strings;

import java.sql.Timestamp;
import java.util.SortedSet;
import java.util.TreeSet;

public class PendingUserDAO implements DAO<PendingUser> {
    // FIELDS
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String NAME = "name";
    private static final String SURNAME = "surname";
    private static final String STATUS = "status";
    private static final String REQUEST_DATE = "requestDate";

    private final SortedSet<PendingUser> cache = new TreeSet<>();
    private final DatabaseManager dbManager;

    // CRUD STATEMENTS
    private static final String INSERT_PENDING_USER_STMT = """
        INSERT INTO PendingUsers (username, password, name, surname, status, requestDate)
        VALUES (?, ?, ?, ?, ?, ?)
    """;
    private static final String UPDATE_PENDING_USER_STMT = """
        UPDATE PendingUsers
        SET password = ?, name = ?, surname = ?, status = ?
        WHERE username = ?
    """;

    private static final String DELETE_PENDING_USER_STMT = """
        DELETE FROM PendingUsers
        WHERE username = ?
    """;

    private static final String GET_ALL_PENDING_USERS_STMT = """
        SELECT *
        FROM PendingUsers
    """;

    public PendingUserDAO() {
        dbManager = Main.dbManager;
        refreshCache();
    }

    /**
     * Refresh the pending user cache from the database
     */
    public void refreshCache() {
        cache.clear();
        dbManager.executeQuery(GET_ALL_PENDING_USERS_STMT,
                rs -> {
                    while (rs.next()) {
                        String username = rs.getString(USERNAME);
                        String password = rs.getString(PASSWORD);
                        String name = rs.getString(NAME);
                        String surname = rs.getString(SURNAME);
                        AuthRequestStatusEnum status = AuthRequestStatusEnum.fromInt(rs.getInt(STATUS));
                        Timestamp creationDate = rs.getTimestamp(REQUEST_DATE);
                        cache.add(new PendingUser(username, password, name, surname, creationDate, status));
                    }
                    return null;
                }
        );
    }

    @Override
    public void insert(PendingUser pendingUser) {
        if(alreadyExists(pendingUser)){
            throw new EntityAlreadyExistsException(Strings.ERR_ENTITY_ALREADY_EXISTS);
        }

        boolean success = dbManager.executeUpdate(INSERT_PENDING_USER_STMT,
                pendingUser.getUsername(),
                pendingUser.getPassword(),
                pendingUser.getName(),
                pendingUser.getSurname(),
                pendingUser.getStatus().ordinal(),
                pendingUser.getRequestDate());

        if(success)
            cache.add(pendingUser);
    }

    @Override
    public void update(PendingUser user) {
        boolean success = dbManager.executeUpdate(
                UPDATE_PENDING_USER_STMT,
                user.getPassword(),
                user.getName(),
                user.getSurname(),
                user.getStatus().ordinal(),
                user.getUsername()
        );

        if (success) {
            cache.remove(user);
            cache.add(user);
        }
    }

    @Override
    public void delete(PendingUser pendingUser) {
        boolean success = dbManager.executeUpdate(
                DELETE_PENDING_USER_STMT,
                pendingUser.getUsername()
        );

        if (success) {
            cache.remove(pendingUser);
        }
    }

    @Override
    public PendingUser getByKey(Object key) {
        return cache.stream()
                .filter(pendingUser -> pendingUser.getUsername().equals(key))
                .findFirst()
                .orElse(null);
    }

    @Override
    public SortedSet<PendingUser> getAll() {
        return cache;
    }

    /**
     * Check if a username exists
     * @param pendingUser pendingUser to check if exists
     * @return true if exists, false otherwise
     */
    public boolean alreadyExists(PendingUser pendingUser) {
        return getByKey(pendingUser.getUsername()) != null;
    }
}
