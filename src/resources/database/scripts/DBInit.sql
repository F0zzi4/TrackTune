-- USERS
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

-- PENDING USERS
CREATE TABLE IF NOT EXISTS PendingUsers (
                                            ID INTEGER PRIMARY KEY AUTOINCREMENT,
                                            username TEXT NOT NULL UNIQUE,
                                            password TEXT NOT NULL,
                                            name TEXT NOT NULL,
                                            surname TEXT NOT NULL,
                                            status INTEGER NOT NULL,
                                            requestDate TIMESTAMP NOT NULL
);

-- TRACKS
CREATE TABLE IF NOT EXISTS Tracks (
                                      ID INTEGER PRIMARY KEY AUTOINCREMENT,
                                      userID INTEGER NOT NULL,
                                      title TEXT NOT NULL,
                                      creationDate TIMESTAMP NOT NULL,
                                      FOREIGN KEY (userID) REFERENCES Users(ID)
);

-- COMMENTS
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

-- INTERACTIONS
CREATE TABLE IF NOT EXISTS Interactions (
                                            ID INTEGER PRIMARY KEY AUTOINCREMENT,
                                            commentID INTEGER NOT NULL,
                                            replyID INTEGER NOT NULL,
                                            FOREIGN KEY (commentID) REFERENCES Comments(ID),
                                            FOREIGN KEY (replyID) REFERENCES Comments(ID)
);

-- MUSICAL INSTRUMENTS
CREATE TABLE IF NOT EXISTS MusicalInstruments (
                                                  ID INTEGER PRIMARY KEY AUTOINCREMENT,
                                                  name TEXT NOT NULL,
                                                  description TEXT NOT NULL
);

-- TRACKS - INSTRUMENTS RELATION
CREATE TABLE IF NOT EXISTS TracksInstruments (
                                                 ID INTEGER PRIMARY KEY AUTOINCREMENT,
                                                 instrumentID INTEGER NOT NULL,
                                                 trackID INTEGER NOT NULL,
                                                 UNIQUE (instrumentID, trackID),
                                                 FOREIGN KEY (instrumentID) REFERENCES MusicalInstruments(ID),
                                                 FOREIGN KEY (trackID) REFERENCES Tracks(ID)
);

-- RESOURCES
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

-- AUTHORS
CREATE TABLE IF NOT EXISTS Authors (
                                       ID INTEGER PRIMARY KEY AUTOINCREMENT,
                                       authorshipName TEXT NOT NULL
);

-- TRACKS - AUTHORS RELATION
CREATE TABLE IF NOT EXISTS TracksAuthors (
                                             ID INTEGER PRIMARY KEY AUTOINCREMENT,
                                             trackID INTEGER NOT NULL,
                                             authorID INTEGER NOT NULL,
                                             UNIQUE (trackID, authorID),
                                             FOREIGN KEY (trackID) REFERENCES Tracks(ID),
                                             FOREIGN KEY (authorID) REFERENCES Authors(ID)
);

-- RESOURCES - AUTHORS RELATION
CREATE TABLE IF NOT EXISTS ResourcesAuthors (
                                                ID INTEGER PRIMARY KEY AUTOINCREMENT,
                                                resourceID INTEGER NOT NULL,
                                                authorID INTEGER NOT NULL,
                                                UNIQUE (resourceID, authorID),
                                                FOREIGN KEY (resourceID) REFERENCES Resources(ID),
                                                FOREIGN KEY (authorID) REFERENCES Authors(ID)
);

-- GENRES
CREATE TABLE IF NOT EXISTS Genres (
                                      ID INTEGER PRIMARY KEY AUTOINCREMENT,
                                      name TEXT NOT NULL,
                                      description TEXT NOT NULL
);

-- RESOURCES - GENRES RELATION
CREATE TABLE IF NOT EXISTS ResourcesGenres (
                                               ID INTEGER PRIMARY KEY AUTOINCREMENT,
                                               genreID INTEGER NOT NULL,
                                               resourceID INTEGER NOT NULL,
                                               FOREIGN KEY (genreID) REFERENCES Genres(ID),
                                               FOREIGN KEY (resourceID) REFERENCES Resources(ID)
);
