package app.tracktune.controller;

import app.tracktune.model.user.AuthenticatedUser;
import app.tracktune.model.user.User;

public class SessionManager {
    private static SessionManager instance;
    private final User sessionUser;

    private SessionManager(User sessionUser) {
        this.sessionUser = sessionUser;
    }

    public static void initialize(User sessionUser) {
        if (instance == null) {
            instance = new SessionManager(sessionUser);
        } else {
            throw new IllegalStateException("SessionManager is already initialized.");
        }
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("SessionManager is not initialized yet.");
        }
        return instance;
    }

    public User getUser() {
        return sessionUser;
    }

    public static void reset() {
        instance = null;
    }
}
