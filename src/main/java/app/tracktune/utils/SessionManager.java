package app.tracktune.utils;

import app.tracktune.model.user.User;

/**
 * Manages the user session throughout the application.
 * Implements the Singleton design pattern to ensure a single active session.
 */
public class SessionManager {
    /**
     * Singleton instance of the SessionManager.
     */
    private static SessionManager instance;

    /**
     * The user associated with the current session.
     */
    private final User sessionUser;

    /**
     * Private constructor to prevent direct instantiation.
     *
     * @param sessionUser the user to associate with the session
     */
    private SessionManager(User sessionUser) {
        this.sessionUser = sessionUser;
    }

    /**
     * Initializes the session with the given user if no session exists.
     *
     * @param sessionUser the user to start the session with
     */
    public static void initialize(User sessionUser) {
        if (instance == null) {
            instance = new SessionManager(sessionUser);
        }
    }

    /**
     * Retrieves the current instance of the SessionManager.
     *
     * @return the current SessionManager instance
     */
    public static SessionManager getInstance() {
        return instance;
    }

    /**
     * Returns the user associated with the current session.
     *
     * @return the session user
     */
    public User getUser() {
        return sessionUser;
    }

    /**
     * Resets the session by clearing the current instance.
     */
    public static void reset() {
        instance = null;
    }
}