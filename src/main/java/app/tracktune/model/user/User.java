package app.tracktune.model.user;

public abstract class User {
    private final Integer id;
    private final String username;
    private final String password;
    private final String name;
    private final String surname;

    /**
     * Constructor for creating a user object
     * @param id The user's unique ID
     * @param username The user's username
     * @param password The user's password
     * @param name The user's name
     * @param surname The user's surname
     */
    public User(Integer id, String username, String password, String name, String surname) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.name = name;
        this.surname = surname;
    }

    public User(String username, String password, String name, String surname) {
        this.id = null;
        this.username = username;
        this.password = password;
        this.name = name;
        this.surname = surname;
    }

    /**
     * Get the user's unique ID
     * @return The user's ID
     */
    public Integer getId() {
        return id;
    }

    /**
     * Get the user's username
     * @return The user's username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Get the user's password
     * @return The user's password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Get the user's name
     * @return The user's name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the user's surname
     * @return The user's surname
     */
    public String getSurname() {
        return surname;
    }

    /**
     * Two users are equal if their username are equal
     * @param other the other user object
     * @return true if their username are equal, false otherwise or if 'other' is not an instance of User
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof User user)) return false;
        return this.username.equals(user.getUsername());
    }
}