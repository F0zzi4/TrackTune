package app.tracktune.utils;

public class SetupDB {
    public static final String CREATE_USERS_TABLE_STMT = "CREATE TABLE IF NOT EXISTS users (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "username TEXT NOT NULL UNIQUE, " +
            "password TEXT NOT NULL UNIQUE, " +
            "name TEXT," +
            "surname TEXT," +
            "authRequestStatus INTEGER," +
            "," +
            "is_admin INTEGER" +
            ")";
    public static final String CHECK_ADMIN_USER_STMT = "SELECT * FROM users WHERE username = 'admin'";
    public static final String INSERT_ADMIN_USER_STMT = "INSERT INTO users (username, password, is_admin) VALUES (?, ?, ?)";
    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_PASSWORD = "admin";

    public static String getDBConfigStatement(){
        // TODO - Concatenare tutti gli statement in maniera che ci sia un unico statement di configurazione
        return "";
    }
}
