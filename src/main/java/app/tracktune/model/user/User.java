package app.tracktune.model.user;

public class User {
    private final String id;
    private final String username;
    private final String password;
    private final boolean isAdmin;
    
    /**
     * Constructor for creating a user with username and password
     * 
     * @param username The user's username
     * @param password The user's password (stored in plain text for educational purposes)
     */
    public User(String username, String password, boolean isAdmin) {
        this.id = username; // Using username as ID for simplicity
        this.username = username;
        this.password = password;
        this.isAdmin = isAdmin;
    }
    
    /**
     * Factory method to create a new user
     * 
     * @param username The user's username
     * @param password The user's password
     * @return A new User object
     */
    public static User create(String username, String password, boolean isAdmin) {
        return new User(username, password, isAdmin);
    }

    public static User create(String username, String password) {
        return new User(username, password, false);
    }
    
    /**
     * Verify if the provided password matches the stored password
     * 
     * @param password The password to check
     * @return true if the password matches, false otherwise
     */
    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }
    
    /**
     * Get the user's ID
     * 
     * @return The user's ID (same as username in this implementation)
     */
    public String getId() {
        return id;
    }
    
    /**
     * Get the user's username
     * 
     * @return The user's username
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * Get the user's password
     * 
     * @return The user's password
     */
    public String getPassword() {
        return password;
    }

    public boolean isAdmin() {
        return isAdmin;
    }
}