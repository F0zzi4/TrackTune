package app.tracktune.model.user;

import app.tracktune.Main;
import app.tracktune.model.DatabaseManager;

import java.util.HashMap;
import java.util.Map;

public class UserDAO {
    private Map<String, User> userCache = new HashMap<>();
    private DatabaseManager dbManager;
    // TABLE FIELDS
    private final String USERNAME = "username";
    private final String PASSWORD = "password";
    private final String IS_ADMIN = "is_admin";
    // QUERIES
    private final String SELECT_USER_STMT = "SELECT username, password, is_admin FROM users";
    private final String INSERT_USER_STMT = "INSERT OR REPLACE INTO users (username, password, is_admin) VALUES (?, ?, ?)";
    private final String DELETE_USER_STMT = "DELETE FROM users WHERE username = ?";

    public UserDAO() {
        dbManager = Main.dbManager;
        refreshUserCache();
    }
    
    /**
     * Refresh the user cache from the database
     */
    private void refreshUserCache() {
        userCache.clear();

        Main.dbManager.executeQuery(SELECT_USER_STMT,
            rs -> {
                while (rs.next()) {
                    String username = rs.getString(USERNAME);
                    String password = rs.getString(PASSWORD);
                    boolean isAdmin = rs.getInt(IS_ADMIN) == 1;
                    
                    User user = new User(username, password, isAdmin);
                    userCache.put(username, user);
                }
                return null;
            }
        );
    }
    
    /**
     * Save a user to the repository
     */
    public void saveUser(User user) {
        boolean success = dbManager.executeUpdate(
            INSERT_USER_STMT,
            user.getUsername(),
            user.getPassword(),
            user.isAdmin() ? 1 : 0
        );
        
        if (success) {
            userCache.put(user.getUsername(), user);
        }
    }
    
    /**
     * Delete a user from the repository
     */
    public void deleteUser(String username) {
        User user = userCache.get(username);
        
        // Never delete admin users as a safety measure
        if (user != null && !user.isAdmin()) {
            boolean success = dbManager.executeUpdate(
                DELETE_USER_STMT,
                username
            );
            
            if (success) {
                userCache.remove(username);
            }
        }
    }
    
    /**
     * Get a user by username
     */
    public User getUser(String username) {
        return userCache.get(username);
    }
    
    /**
     * Check if a username exists
     */
    public boolean usernameExists(String username) {
        return userCache.containsKey(username);
    }
    
    /**
     * Get all users
     */
    public Map<String, User> getAllUsers() {
        return new HashMap<>(userCache);
    }
}