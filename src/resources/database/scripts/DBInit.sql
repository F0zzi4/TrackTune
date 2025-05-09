CREATE TABLE IF NOT EXISTS Users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    name TEXT NOT NULL,
    surname TEXT NOT NULL,
    user_status INTEGER NOT NULL,
    creation_date TIMESTAMP NOT NULL,
    is_admin INTEGER CHECK (is_admin IN (0, 1)) NOT NULL
);

CREATE TABLE IF NOT EXISTS PendingUsers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    name TEXT NOT NULL,
    surname TEXT NOT NULL,
    status INTEGER NOT NULL,
    requestDate TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS tracks (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    title TEXT NOT NULL,
    creation_date TIMESTAMP NOT NULL ,

    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS comments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER,
    track_id INTEGER,
    description TEXT NOT NULL,
    start_track_interval INTEGER,
    end_track_interval INTEGER,
    creation_date TIMESTAMP NOT NULL ,

    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (track_id) REFERENCES tracks(id)
);

CREATE TABLE IF NOT EXISTS interactions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    comment_id INTEGER NOT NULL,
    reply_id INTEGER NOT NULL,

    FOREIGN KEY (comment_id) REFERENCES comments(id),
    FOREIGN KEY (reply_id) REFERENCES comments(id)
);

CREATE TABLE IF NOT EXISTS musical_instruments(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    description TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS tracks_instruments(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    instrument_id INTEGER NOT NULL ,
    track_id INTEGER NOT NULL ,
    unique (instrument_id, track_id),

    FOREIGN KEY (instrument_id) REFERENCES musical_instruments(id),
    FOREIGN KEY (track_id) REFERENCES tracks(id)
);

CREATE TABLE IF NOT EXISTS resources(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    type TEXT NOT NULL,
    data BLOB NOT NULL,
    creation_date TIMESTAMP NOT NULL,
    is_multimedia INTEGER CHECK (is_multimedia IN (0, 1)) NOT NULL,
    resource_date TIMESTAMP NOT NULL,
    track_id INTEGER,

    FOREIGN KEY (track_id) REFERENCES tracks(id)
);

CREATE TABLE IF NOT EXISTS authors(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    authorship_name TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS track_author(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    track_id INTEGER NOT NULL,
    author_id INTEGER NOT NULL,
    UNIQUE (track_id, author_id),
    FOREIGN KEY (track_id) REFERENCES tracks(id),
    FOREIGN KEY (author_id) REFERENCES authors(id)
);

CREATE TABLE IF NOT EXISTS resource_author(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    resource_id INTEGER NOT NULL,
    author_id INTEGER NOT NULL,
    UNIQUE (resource_id, author_id),
    FOREIGN KEY (resource_id) REFERENCES resources(id),
    FOREIGN KEY (author_id) REFERENCES authors(id)
)



