package app.tracktune.utils;

import java.sql.Timestamp;

/**
 * Class containing all the statements to create database structure
 * Used this approach because the init db is faster and more efficient, because SQLite library functions accept string parameters
 */
public class DBInit {
    private static final Timestamp currTimestamp = new Timestamp(System.currentTimeMillis());
    private static final String CREATE_USERS_TABLE_STMT = """
        CREATE TABLE IF NOT EXISTS Users (
            ID INTEGER PRIMARY KEY AUTOINCREMENT,
            username TEXT NOT NULL UNIQUE,
            password TEXT NOT NULL,
            name TEXT NOT NULL,
            surname TEXT NOT NULL,
            status INTEGER NOT NULL,
            creationDate TIMESTAMP NOT NULL,
            isAdmin INTEGER CHECK (isAdmin IN (0, 1)) NOT NULL
        );
    """;

    private static final String CREATE_PENDING_USERS_TABLE_STMT = """
        CREATE TABLE IF NOT EXISTS PendingUsers (
            ID INTEGER PRIMARY KEY AUTOINCREMENT,
            username TEXT NOT NULL UNIQUE,
            password TEXT NOT NULL,
            name TEXT NOT NULL,
            surname TEXT NOT NULL,
            status INTEGER NOT NULL,
            requestDate TIMESTAMP NOT NULL
        );
    """;

    private static final String CREATE_TRACKS_TABLE_STMT = """
        CREATE TABLE IF NOT EXISTS Tracks (
            ID INTEGER PRIMARY KEY AUTOINCREMENT,
            userID INTEGER NOT NULL,
            title TEXT NOT NULL,
            creationDate TIMESTAMP NOT NULL,
            FOREIGN KEY (userID) REFERENCES Users(ID)
        );
    """;

    private static final String CREATE_COMMENTS_TABLE_STMT = """
        CREATE TABLE IF NOT EXISTS Comments (
            ID INTEGER PRIMARY KEY AUTOINCREMENT,
            userID INTEGER,
            trackID INTEGER,
            description TEXT NOT NULL,
            startTrackInterval INTEGER,
            endTrackInterval INTEGER,
            creationDate TIMESTAMP NOT NULL,
            FOREIGN KEY (userID) REFERENCES Users(ID),
            FOREIGN KEY (trackID) REFERENCES Tracks(ID)
        );
    """;

    private static final String CREATE_INTERACTIONS_TABLE_STMT = """
        CREATE TABLE IF NOT EXISTS Interactions (
            ID INTEGER PRIMARY KEY AUTOINCREMENT,
            commentID INTEGER NOT NULL,
            replyID INTEGER NOT NULL,
            FOREIGN KEY (commentID) REFERENCES Comments(ID),
            FOREIGN KEY (replyID) REFERENCES Comments(ID)
        );
    """;

    private static final String CREATE_INSTRUMENTS_TABLE_STMT = """
        CREATE TABLE IF NOT EXISTS MusicalInstruments (
            ID INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            description TEXT NOT NULL
        );
    """;

    private static final String CREATE_TRACKS_INSTRUMENTS_TABLE_STMT = """
        CREATE TABLE IF NOT EXISTS TracksInstruments (
            ID INTEGER PRIMARY KEY AUTOINCREMENT,
            instrumentID INTEGER NOT NULL,
            trackID INTEGER NOT NULL,
            UNIQUE (instrumentID, trackID),
            FOREIGN KEY (instrumentID) REFERENCES MusicalInstruments(ID),
            FOREIGN KEY (trackID) REFERENCES Tracks(ID)
        );
    """;

    private static final String CREATE_RESOURCES_TABLE_STMT = """
        CREATE TABLE IF NOT EXISTS Resources (
            ID INTEGER PRIMARY KEY AUTOINCREMENT,
            type TEXT NOT NULL,
            data BLOB NOT NULL,
            creationDate TIMESTAMP NOT NULL,
            isMultimedia INTEGER CHECK (isMultimedia IN (0, 1)) NOT NULL,
            resourceDate TIMESTAMP NOT NULL,
            trackID INTEGER,
            FOREIGN KEY (trackID) REFERENCES Tracks(ID)
        );
    """;

    private static final String CREATE_AUTHORS_TABLE_STMT = """
        CREATE TABLE IF NOT EXISTS Authors (
            ID INTEGER PRIMARY KEY AUTOINCREMENT,
            authorshipName TEXT NOT NULL
        );
    """;

    private static final String CREATE_TRACK_AUTHOR_TABLE_STMT = """
        CREATE TABLE IF NOT EXISTS TracksAuthors (
            ID INTEGER PRIMARY KEY AUTOINCREMENT,
            trackID INTEGER NOT NULL,
            authorID INTEGER NOT NULL,
            UNIQUE (trackID, authorID),
            FOREIGN KEY (trackID) REFERENCES Tracks(ID),
            FOREIGN KEY (authorID) REFERENCES Authors(ID)
        );
    """;

    private static final String CREATE_RESOURCE_AUTHOR_TABLE_STMT = """  
        CREATE TABLE IF NOT EXISTS ResourcesAuthors (
            ID INTEGER PRIMARY KEY AUTOINCREMENT,
            resourceID INTEGER NOT NULL,
            authorID INTEGER NOT NULL,
            UNIQUE (resourceID, authorID),
            FOREIGN KEY (resourceID) REFERENCES resources(ID),
            FOREIGN KEY (authorID) REFERENCES authors(ID)
        );
    """;

    private static final String CREATE_GENRES_TABLE_STMT = """  
        CREATE TABLE IF NOT EXISTS Genres (
            ID INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            description TEXT NOT NULL
        );
    """;

    private static final String CREATE_RESOURCE_GENRE_TABLE_STMT = """  
        CREATE TABLE IF NOT EXISTS ResourcesGenres (
            ID INTEGER PRIMARY KEY AUTOINCREMENT,
            genreID INTEGER NOT NULL,
            resourceID INTEGER NOT NULL,
            FOREIGN KEY (genreID) REFERENCES Genres(ID),
            FOREIGN KEY (resourceID) REFERENCES resources(ID)
        );
    """;

    public static final String CHECK_ADMIN_USER_STMT = """
        SELECT *
        FROM Users
        WHERE username = 'admin'
    """;
    public static final String INSERT_ADMIN_USER_STMT = """
        INSERT INTO Users (username, password, name, surname, status, creationDate, isAdmin)
        VALUES (?, ?, ?, ?, ?, ?, ?)
    """;
    public static final Object[] ADMIN_PARAMS = {
            "admin",                      // username
            "admin12345",                 // password
            "admin",                      // name
            "",                           // surname
            0,                            // status
            currTimestamp,                // creationDate
            1                             // isAdmin
    };

    public static String getDBInitStatement() {
        return CREATE_USERS_TABLE_STMT + ";" +
                CREATE_PENDING_USERS_TABLE_STMT + ";" +
                CREATE_TRACKS_TABLE_STMT + ";" +
                CREATE_COMMENTS_TABLE_STMT + ";" +
                CREATE_INTERACTIONS_TABLE_STMT + ";" +
                CREATE_INSTRUMENTS_TABLE_STMT + ";" +
                CREATE_TRACKS_INSTRUMENTS_TABLE_STMT + ";" +
                CREATE_RESOURCES_TABLE_STMT + ";" +
                CREATE_AUTHORS_TABLE_STMT + ";" +
                CREATE_TRACK_AUTHOR_TABLE_STMT + ";" +
                CREATE_GENRES_TABLE_STMT + ";"+
                CREATE_RESOURCE_GENRE_TABLE_STMT + ";" +
                CREATE_RESOURCE_AUTHOR_TABLE_STMT;
    }
}
