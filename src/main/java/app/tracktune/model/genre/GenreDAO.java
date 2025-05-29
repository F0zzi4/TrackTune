package app.tracktune.model.genre;

import app.tracktune.Main;
import app.tracktune.exceptions.SQLiteException;
import app.tracktune.interfaces.DAO;
import app.tracktune.model.DatabaseManager;
import app.tracktune.utils.Strings;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class GenreDAO implements DAO<Genre> {
    private final DatabaseManager dbManager;

    // FIELDS
    private static final String ID = "ID";
    private static final String NAME = "name";
    private static final String DESCRIPTION = "description";

    // CRUD STATEMENTS
    private static final String INSERT_GENRE_STMT = """
        INSERT INTO Genres (name, description)
        VALUES (?, ?)
    """;

    private static final String UPDATE_GENRE_STMT = """
        UPDATE Genres
        SET name = ?, description = ?
        WHERE ID = ?
    """;

    private static final String DELETE_GENRE_STMT = """
        DELETE FROM Genres
        WHERE ID = ?
    """;

    private static final String GET_ALL_GENRES_STMT = """
        SELECT *
        FROM Genres
    """;

    private static final String GET_ALL_GENRES_USED_STMT = """
        SELECT DISTINCT g.*
        FROM Genres g
        JOIN TracksGenres tg ON tg.genreID = g.ID
    """;

    private static final String GET_GENRE_BY_ID_STMT = """
        SELECT *
        FROM Genres
        WHERE ID = ?
    """;

    private static final String GET_GENRE_BY_TRACK_ID_STMT = """
        SELECT g.*
        FROM Genres g
        JOIN TracksGenres tg ON tg.genreId = g.ID
        WHERE tg.trackID = ?
    """;

    public GenreDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public Integer insert(Genre genre) {
        boolean success = dbManager.executeUpdate(INSERT_GENRE_STMT,
                genre.getName(),
                genre.getDescription()
        );

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }
        return dbManager.getLastInsertId();
    }

    @Override
    public void updateById(Genre genre, int id) {
        boolean success = dbManager.executeUpdate(UPDATE_GENRE_STMT,
                genre.getName(),
                genre.getDescription(),
                id
        );

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }
    }

    @Override
    public void deleteById(int id) {
        boolean success = dbManager.executeUpdate(DELETE_GENRE_STMT, id);

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }
    }

    @Override
    public Genre getById(int id) {
        AtomicReference<Genre> result = new AtomicReference<>();

        boolean success = dbManager.executeQuery(GET_GENRE_BY_ID_STMT,
                rs -> {
                    if (rs.next()) {
                        result.set(mapResultSetToEntity(rs));
                        return true;
                    }
                    return false;
                }, id);

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }

        return result.get();
    }

    @Override
    public List<Genre> getAll() {
        List<Genre> genres = new ArrayList<>();

        dbManager.executeQuery(GET_ALL_GENRES_STMT,
                rs -> {
                    while (rs.next()) {
                        genres.add(mapResultSetToEntity(rs));
                    }
                    return null;
                });

        return genres;
    }

    public List<Genre> getAllUsed() {
        List<Genre> genres = new ArrayList<>();

        dbManager.executeQuery(GET_ALL_GENRES_USED_STMT,
                rs -> {
                    while (rs.next()) {
                        genres.add(mapResultSetToEntity(rs));
                    }
                    return null;
                });

        return genres;
    }


    public List<Genre> getAllGenresByTrackId(int id) {
        List<Genre> genres = new ArrayList<>();

        dbManager.executeQuery(GET_GENRE_BY_TRACK_ID_STMT,
                rs -> {
                    while (rs.next()) {
                        genres.add(mapResultSetToEntity(rs));
                    }
                    return null;
                }, id);

        return genres;
    }

    private Genre mapResultSetToEntity(ResultSet rs) throws SQLException {
        int id = rs.getInt(ID);
        String name = rs.getString(NAME);
        String description = rs.getString(DESCRIPTION);
        return new Genre(id, name, description);
    }
}