package app.tracktune.model.user;

import app.tracktune.Main;
import app.tracktune.interfaces.DAO;
import app.tracktune.model.DatabaseManager;
import app.tracktune.utils.SQLiteScripts;

import java.util.SortedSet;
import java.util.TreeSet;

public class PendingUserDAO implements DAO<PendingUser> {
    // FIELDS
    private final String USERNAME = "username";
    private final String PASSWORD = "password";
    private final String NAME = "name";
    private final String SURNAME = "surname";
    private final String STATUS = "status";
    private final String REQUEST_DATE = "requestDate";

    private final SortedSet<User> userCache = new TreeSet<>();
    private final DatabaseManager dbManager;

    // CRUD STATEMENTS
    private static final String INSERT_USER_STMT = """
    INSERT INTO PendingUsers (username, password, name, surname, status, requestDate)
    VALUES (?, ?, ?, ?, ?, ?)
""";
    private static final String UPDATE_USER_STMT = """
    UPDATE PendingUsers
    SET password = ?, name = ?, surname = ?, status = ?, requestDate = ?
    WHERE username = ?
""";

    private static final String DELETE_USER_STMT = """
    DELETE FROM PendingUsers
    WHERE username = ?
""";

    private static final String GET_ALL_USERS_STMT = """
    SELECT * FROM PendingUsers
""";

    public PendingUserDAO() {
        dbManager = Main.dbManager;
    }

    @Override
    public void insert(PendingUser data) {
        if (this.getByKey(data.getUsername()) == null) {
            dbManager.executeUpdate(INSERT_USER_STMT,
                    data.getUsername(),
                    data.getPassword(),
                    data.getName(),
                    data.getSurname(),
                    data.getStatus().ordinal(), // Convert enum to int
                    data.getRequestDate());
        }
    }

    @Override
    public void update(PendingUser data) {

    }

    @Override
    public void delete(PendingUser data) {

    }

    @Override
    public PendingUser getByKey(Object key) {

        // Execute the query and return the result
        return dbManager.executeQuery(SQLiteScripts.SELECT_PENDING_USERS_BY_USERNAME, rs -> {
            if (rs.next()) {
                return new PendingUser(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("name"),
                        rs.getString("surname"),
                        rs.getTimestamp("requestDate"),
                        AuthRequestStatusEnum.fromInt(rs.getInt("status"))
                );
            }
            return null;
        }, key); // Pass the username as a parameter to the query
    }

    @Override
    public SortedSet<PendingUser> getAll() {
        return null;
    }
}
