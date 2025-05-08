package app.tracktune.model.user;

public abstract class User implements Comparable<User>{
    private final String username;
    private final String password;
    private final String name;
    private final String surname;

    /**
     * Constructor for creating a user object
     * @param username The user's username
     * @param password The user's password
     * @param name The user's name
     * @param surname The user's surname
     */
    public User(String username, String password, String name, String surname) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.surname = surname;
    }

    /**
     * Verify if the provided password matches the stored password
     * 
     * @param password The password to check
     * @return true if the password matches, false otherwise
     */
    public boolean checkPassword(String password) { return this.password.equals(password); }
    
    /**
     * Get the user's username
     * 
     * @return The user's username
     */
    public String getUsername() { return username; }
    
    /**
     * Get the user's password
     * 
     * @return The user's password
     */
    public String getPassword() { return password; }

    /**
     * Get the user's name
     *
     * @return The user's name
     */
    public String getName() { return name; }

    /**
     * Get the user's surname
     *
     * @return The user's surname
     */
    public String getSurname() { return surname; }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;
        if (obj instanceof User user) {
            result = this.getUsername().equals(user.getUsername());
        }
        return result;
    }

    @Override
    public int compareTo(User o) {
        return this.getUsername().compareToIgnoreCase(o.getUsername());
    }
}