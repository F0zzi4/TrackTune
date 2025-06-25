package app.tracktune.model.user;

/**
 * Abstract base class representing a user in the system.
 */
public abstract class User {
    private final Integer id;
    private final String username;
    private final String password;
    private final String name;
    private final String surname;

    /**
     * Constructs a User object with a specified unique ID.
     *
     * @param id       the unique identifier of the user
     * @param username the user's username
     * @param password the user's password
     * @param name     the user's first name
     * @param surname  the user's last name
     */
    public User(Integer id, String username, String password, String name, String surname) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.name = name;
        this.surname = surname;
    }

    /**
     * Constructs a User object without specifying an ID.
     * Typically used when the ID is auto-generated or unknown at construction time.
     *
     * @param username the user's username
     * @param password the user's password
     * @param name     the user's first name
     * @param surname  the user's last name
     */
    public User(String username, String password, String name, String surname) {
        this.id = null;
        this.username = username;
        this.password = password;
        this.name = name;
        this.surname = surname;
    }

    /**
     * Returns the unique identifier of the user.
     *
     * @return the user ID, or null if not set
     */
    public Integer getId() {
        return id;
    }

    /**
     * Returns the username of the user.
     *
     * @return the username string
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the password of the user.
     *
     * @return the password string
     */
    public String getPassword() {
        return password;
    }

    /**
     * Returns the first name of the user.
     *
     * @return the user's first name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the last name (surname) of the user.
     *
     * @return the user's surname
     */
    public String getSurname() {
        return surname;
    }

    /**
     * Determines whether two User objects are equal based on their username.
     *
     * @param other the other object to compare to
     * @return true if both are User instances with the same username; false otherwise
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof User user)) return false;
        return this.username.equals(user.getUsername());
    }
}
