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

    /**
     * Two users are equal if their username are equal
     * @param other the other user object
     * @return true if their username are equal, false otherwise or if 'other' is not an instance of User
     */
    @Override
    public boolean equals(Object other) {
        boolean result = false;
        if (other instanceof User user) {
            result = this.getUsername().equals(user.getUsername());
        }
        return result;
    }

    /**
     * Two user are comparable by their username
     * @param other the object to be compared
     * @return an int value based on alphabetic sort of the two users
     */
    @Override
    public int compareTo(User other) {
        return this.getUsername().compareToIgnoreCase(other.getUsername());
    }
}