package app.tracktune.model.user;

import app.tracktune.Main;
import app.tracktune.interfaces.DAO;
import app.tracktune.model.DatabaseManager;

import java.sql.Timestamp;
import java.util.SortedSet;
import java.util.TreeSet;

public class UserDAO implements DAO<User> {
    private SortedSet<User> userCache = new TreeSet<>();
    private DatabaseManager dbManager;
    // FIELDS
    private final String USERNAME = "username";
    private final String PASSWORD = "password";
    private final String NAME = "name";
    private final String SURNAME = "surname";
    private final String STATUS = "status";
    private final String CREATION_DATE = "creationDate";
    private final String IS_ADMIN = "isAdmin";

    public UserDAO() {
        dbManager = Main.dbManager;
        refreshUserCache();
    }
    
    /**
     * Refresh the user cache from the database
     */
    private void refreshUserCache() {
        userCache.clear();
        Main.dbManager.executeQuery("",
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
                        userCache.add(new AuthenticatedUser(username, password, name, surname, status, creationDate));
                    }else{
                        userCache.add(new Administrator(username, password, name, surname, status, creationDate));
                    }
                }
                return null;
            }
        );
    }

    @Override
    public void insert(User data) {

    }

    @Override
    public void update(User user) {
        boolean success = false;

        if(user instanceof Administrator admin){
            success = dbManager.executeUpdate(
                    "",
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
                    "",
                    authUser.getUsername(),
                    authUser.getPassword(),
                    authUser.getName(),
                    authUser.getSurname(),
                    authUser.getCreationDate(),
                    0
            );
        }
        
        if (success) {
            userCache.add(user);
        }
    }
    
    @Override
    public void delete(User user) {
        boolean success = false;
        success = dbManager.executeUpdate(
                "",
                ""
        );

        if (success) {
            userCache.remove(user);
        }
    }

    @Override
    public User getByKey(String username) {
        return userCache.stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }
    
    @Override
    public SortedSet<User> getAll() {
        return userCache;
    }

    /**
     * Check if a username exists
     */
    public boolean usernameExists(String username) {
        return true;
    }
}