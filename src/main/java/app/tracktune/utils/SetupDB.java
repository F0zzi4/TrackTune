package app.tracktune.utils;

public class SetupDB {
    public static final String CREATE_USERS_TABLE_STMT = """
        CREATE TABLE IF NOT EXISTS users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            username TEXT NOT NULL UNIQUE,
            password TEXT NOT NULL,
            name TEXT NOT NULL,
            surname TEXT NOT NULL,
            user_status INTEGER NOT NULL,
            creation_date TIMESTAMP NOT NULL,
            is_admin INTEGER CHECK (is_admin IN (0, 1)) NOT NULL
        );
    """;

    public static final String CREATE_PENDING_USERS_TABLE_STMT = """
        CREATE TABLE IF NOT EXISTS pending_users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            username TEXT NOT NULL UNIQUE,
            password TEXT NOT NULL,
            name TEXT NOT NULL,
            surname TEXT NOT NULL,
            status INTEGER NOT NULL,
            request_date TIMESTAMP NOT NULL
        );
    """;

    public static final String CREATE_TRACKS_TABLE_STMT = """
        CREATE TABLE IF NOT EXISTS tracks (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id INTEGER NOT NULL,
            title TEXT NOT NULL,
            creation_date TIMESTAMP NOT NULL,
            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
        );
    """;

    public static final String CREATE_COMMENTS_TABLE_STMT = """
        CREATE TABLE IF NOT EXISTS comments (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id INTEGER,
            track_id INTEGER,
            description TEXT NOT NULL,
            start_track_interval INTEGER,
            end_track_interval INTEGER,
            creation_date TIMESTAMP NOT NULL,
            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
            FOREIGN KEY (track_id) REFERENCES tracks(id) ON DELETE CASCADE
        );
    """;

    public static final String CREATE_INTERACTIONS_TABLE_STMT = """
        CREATE TABLE IF NOT EXISTS interactions (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            comment_id INTEGER NOT NULL,
            reply_id INTEGER NOT NULL,
            FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE,
            FOREIGN KEY (reply_id) REFERENCES comments(id) ON DELETE CASCADE
        );
    """;

    public static final String CREATE_INSTRUMENTS_TABLE_STMT = """
        CREATE TABLE IF NOT EXISTS musical_instruments (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            description TEXT NOT NULL
        );
    """;

    public static final String CREATE_TRACKS_INSTRUMENTS_TABLE_STMT = """
        CREATE TABLE IF NOT EXISTS tracks_instruments (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            instrument_id INTEGER NOT NULL,
            track_id INTEGER NOT NULL,
            UNIQUE (instrument_id, track_id),
            FOREIGN KEY (instrument_id) REFERENCES musical_instruments(id) ON DELETE CASCADE,
            FOREIGN KEY (track_id) REFERENCES tracks(id) ON DELETE CASCADE
        );
    """;

    public static final String CREATE_RESOURCES_TABLE_STMT = """
        CREATE TABLE IF NOT EXISTS resources (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            type TEXT NOT NULL,
            data BLOB NOT NULL,
            creation_date TIMESTAMP NOT NULL,
            is_multimedial INTEGER CHECK (is_multimedial IN (0, 1)) NOT NULL,
            resource_date TIMESTAMP NOT NULL,
            track_id INTEGER,
            FOREIGN KEY (track_id) REFERENCES tracks(id) ON DELETE CASCADE
        );
    """;

    public static final String CREATE_AUTHORS_TABLE_STMT = """
        CREATE TABLE IF NOT EXISTS authors (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            authorship_name TEXT NOT NULL
        );
    """;

    public static final String CREATE_TRACK_AUTHOR_TABLE_STMT = """
        CREATE TABLE IF NOT EXISTS track_author (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            track_id INTEGER NOT NULL,
            author_id INTEGER NOT NULL,
            UNIQUE (track_id, author_id),
            FOREIGN KEY (track_id) REFERENCES tracks(id) ON DELETE CASCADE,
            FOREIGN KEY (author_id) REFERENCES authors(id) ON DELETE CASCADE
        );
    """;

    public static final String CREATE_RESOURCE_AUTHOR_TABLE_STMT = """
        CREATE TABLE IF NOT EXISTS resource_author (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            resource_id INTEGER NOT NULL,
            author_id INTEGER NOT NULL,
            UNIQUE (resource_id, author_id),
            FOREIGN KEY (resource_id) REFERENCES resources(id) ON DELETE CASCADE,
            FOREIGN KEY (author_id) REFERENCES authors(id) ON DELETE CASCADE
        );
    """;

    public static final String CHECK_ADMIN_USER_STMT = "SELECT * FROM users WHERE username = 'admin'";
    public static final String INSERT_ADMIN_USER_STMT = "INSERT INTO users (username, password, name, surname, user_status, creation_date, is_admin) VALUES (?, ?, ?, ?, ?, ?, ?)";
    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_PASSWORD = "admin";

    public static String getDBConfigStatement() {
        return CREATE_USERS_TABLE_STMT +
                CREATE_PENDING_USERS_TABLE_STMT +
                CREATE_TRACKS_TABLE_STMT +
                CREATE_COMMENTS_TABLE_STMT +
                CREATE_INTERACTIONS_TABLE_STMT +
                CREATE_INSTRUMENTS_TABLE_STMT +
                CREATE_TRACKS_INSTRUMENTS_TABLE_STMT +
                CREATE_RESOURCES_TABLE_STMT +
                CREATE_AUTHORS_TABLE_STMT +
                CREATE_TRACK_AUTHOR_TABLE_STMT +
                CREATE_RESOURCE_AUTHOR_TABLE_STMT;
    }
}
