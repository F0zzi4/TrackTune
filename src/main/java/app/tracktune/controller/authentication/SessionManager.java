package app.tracktune.controller.authentication;

import app.tracktune.model.user.User;

public class SessionManager {
    private static SessionManager instance;
    private User sessionUser;

    private SessionManager(User sessionUser) {
        this.sessionUser = sessionUser;
    }

    public static void initialize(User sessionUser) {
        if (instance == null) {
            instance = new SessionManager(sessionUser);
        }
    }

    public static SessionManager getInstance() {
        return instance;
    }

    public User getUser() {
        return sessionUser;
    }

    public static void reset() {
        instance = null;
    }
}
