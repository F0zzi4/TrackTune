package app.tracktune.model.user;

import app.tracktune.Main;
import app.tracktune.exceptions.UserAlreadyExistsException;
import app.tracktune.interfaces.DAO;
import app.tracktune.model.DatabaseManager;
import app.tracktune.utils.Strings;

import java.sql.Timestamp;
import java.util.SortedSet;
import java.util.TreeSet;

public class UserDAO implements DAO<User> {
    private final SortedSet<User> cache = new TreeSet<>();
    private final DatabaseManager dbManager;
    // FIELDS
    private final String USERNAME = "username";
    private final String PASSWORD = "password";
    private final String NAME = "name";
    private final String SURNAME = "surname";
    private final String STATUS = "status";
    private final String CREATION_DATE = "creationDate";
    private final String IS_ADMIN = "isAdmin";
    // CRUD STATEMENTS
    private static final String INSERT_USER_STMT = """
        INSERT INTO Users (username, password, name, surname, status, creationDate, isAdmin)
        VALUES (?, ?, ?, ?, ?, ?, ?)
    """;
    private static final String UPDATE_USER_STMT = """
        UPDATE Users
        SET name = ?, surname = ?, status = ?
        WHERE username = ?
    """;
    private static final String DELETE_USER_STMT = """
        DELETE FROM Users
        WHERE username = ?
    """;
    private static final String GET_ALL_USERS_STMT = """
        SELECT *
        FROM Users
    """;

    /**
     * Constructor to initialize a dao that manages 'User' entity
     * Copy the reference of the database manager object
     */
    public UserDAO() {
        dbManager = Main.dbManager;
        refreshCache();
    }
    
    /**
     * Refresh the user cache from the database
     */
    @Override
    public void refreshCache() {
        cache.clear();
        dbManager.executeQuery(GET_ALL_USERS_STMT,
            rs -> {
                while (rs.next()) {
                    String username = rs.getString(USERNAME);
                    String password = rs.getString(PASSWORD);
                    String name = rs.getString(NAME);
                    String surname = rs.getString(SURNAME);
                    UserStatusEnum status = UserStatusEnum.fromInt(rs.getInt(STATUS));
                    Timestamp creationDate = rs.getTimestamp(CREATION_DATE);
                    boolean isAdmin = rs.getInt(IS_ADMIN) == 1;

                    if (isAdmin){
                        cache.add(new Administrator(username, password, name, surname, status, creationDate));
                    }else{
                        cache.add(new AuthenticatedUser(username, password, name, surname, status, creationDate));
                    }
                }
                return null;
            }
        );
    }

    /**
     * Inserts a new user into the database
     *
     * @param user the user to be inserted into the data source
     */
    @Override
    public void insert(User user) {
        boolean success = false;

        if(alreadyExists(user)){
            throw new UserAlreadyExistsException(Strings.ERR_USER_ALREADY_EXISTS);
        }

        if(user instanceof Administrator admin){
            success = dbManager.executeUpdate(
                    INSERT_USER_STMT,
                    admin.getUsername(),
                    admin.getPassword(),
                    admin.getName(),
                    admin.getSurname(),
                    admin.getStatus(),
                    admin.getCreationDate(),
                    1
            );
        }else if(user instanceof AuthenticatedUser authUser){
            success = dbManager.executeUpdate(
                    INSERT_USER_STMT,
                    authUser.getUsername(),
                    authUser.getPassword(),
                    authUser.getName(),
                    authUser.getSurname(),
                    authUser.getStatus(),
                    authUser.getCreationDate(),
                    0
            );
        }

        if (success) {
            cache.add(user);
        }
    }

    /**
     * Updates a user into the database
     *
     * @param user the user to be updated on database
     */
    @Override
    public void update(User user) {
        boolean success = false;

        if(user instanceof Administrator admin){
            success = dbManager.executeUpdate(
                    UPDATE_USER_STMT,
                    admin.getName(),
                    admin.getSurname(),
                    admin.getStatus(),
                    admin.getUsername()
                    );
        }else if(user instanceof AuthenticatedUser authUser){
            success = dbManager.executeUpdate(
                    UPDATE_USER_STMT,
                    authUser.getName(),
                    authUser.getSurname(),
                    authUser.getStatus(),
                    authUser.getUsername()
            );
        }
        
        if (success) {
            cache.remove(user);
            cache.add(user);
        }
    }

    /**
     * Deletes a user record on database
     *
     * @param user the user to be deleted on database
     */
    @Override
    public void delete(User user) {
        boolean success = dbManager.executeUpdate(
                DELETE_USER_STMT,
                user.getUsername()
        );

        if (success) {
            cache.remove(user);
        }
    }

    /**
     * Get the user from database with given key
     * @param key is the logic key -> username
     * @return the related user if key exists, null otherwise
     */
    @Override
    public User getByKey(Object key) {
        return cache.stream()
                .filter(user -> user.getUsername().equals(key))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get all the users stored in the cache
     * @return sorted list of stored users
     */
    @Override
    public SortedSet<User> getAll() {
        return cache;
    }

    /**
     * Verify if the provided username and password match the stored ones
     * @param username The username to check
     * @param password The password to check
     * @return true if the username and password match, false otherwise
     */
    public boolean checkCredentials(String username, String password) {
        boolean result = false;
        User user = getByKey(username);

        if(user != null){
            result = user.getPassword().equals(password);
        }
        return result;
    }

    /**
     * Check if a username exists
     * @param user user to check if exists
     * @return true if exists, false otherwise
     */
    @Override
    public boolean alreadyExists(User user) {
        return getByKey(user.getUsername()) != null;
    }
}