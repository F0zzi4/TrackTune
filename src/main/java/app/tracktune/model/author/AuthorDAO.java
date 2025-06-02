package app.tracktune.model.author;

import app.tracktune.exceptions.SQLiteException;
import app.tracktune.interfaces.DAO;
import app.tracktune.model.DatabaseManager;
import app.tracktune.utils.Strings;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class AuthorDAO implements DAO<Author> {
    private final DatabaseManager dbManager;

    // FIELDS
    private static final String ID = "ID";
    private static final String AUTHORSHIP_NAME = "authorshipName";
    private static final String STATUS = "status";

    // CRUD STATEMENTS
    private static final String INSERT_AUTHOR_STMT = """
        INSERT INTO Authors (authorshipName, status)
        VALUES (?, ?)
    """;

    private static final String UPDATE_AUTHOR_STMT = """
        UPDATE Authors
        SET authorshipName = ?, status = ?
        WHERE ID = ?
    """;

    private static final String DELETE_AUTHOR_STMT = """
        DELETE FROM Authors
        WHERE ID = ?
    """;

    private static final String GET_ALL_AUTHORS_STMT = """
        SELECT * FROM Authors
    """;

    private static final String GET_ALL_AUTHORS_ACTIVE_STMT = """
        SELECT * FROM Authors
        where status = 0
    """;

    private static final String GET_AUTHOR_BY_ID_STMT = """
        SELECT * FROM Authors
        WHERE ID = ?
    """;

    private static final String GET_AUTHOR_BY_AUTHORSHIP_NAME_STMT = """
        SELECT * FROM Authors
        WHERE authorshipName = ?
    """;

    private static final String GET_ALL_AUTHORS_BY_TRACK_ID_STMT = """
        SELECT a.*
        FROM Authors a
        JOIN TracksAuthors ta ON ta.authorId = a.ID
        WHERE ta.trackID = ?
    """;

    public AuthorDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public Integer insert(Author author) {
        boolean success = dbManager.executeUpdate(INSERT_AUTHOR_STMT,
                author.getAuthorshipName(),
                author.getStatus().ordinal()
        );

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }

        return dbManager.getLastInsertId();
    }

    @Override
    public void updateById(Author author, int id) {
        boolean success = dbManager.executeUpdate(UPDATE_AUTHOR_STMT,
                author.getAuthorshipName(),
                author.getStatus().ordinal(),
                id
        );

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }
    }

    @Override
    public void deleteById(int id) {
        boolean success = dbManager.executeUpdate(DELETE_AUTHOR_STMT, id);

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }
    }

    @Override
    public Author getById(int id) {
        AtomicReference<Author> result = new AtomicReference<>();

        boolean success = dbManager.executeQuery(GET_AUTHOR_BY_ID_STMT,
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

    public boolean existByAuthorshipName(String id) {
        AtomicReference<Author> result = new AtomicReference<>();

        boolean success = dbManager.executeQuery(GET_AUTHOR_BY_AUTHORSHIP_NAME_STMT,
                rs -> {
                    if (rs.next()) {
                        result.set(mapResultSetToEntity(rs));
                        return true;
                    }
                    return false;
                }, id);

        return !success;
    }

    @Override
    public List<Author> getAll() {
        List<Author> authors = new ArrayList<>();
        dbManager.executeQuery(GET_ALL_AUTHORS_STMT,
                rs -> {
                    while (rs.next()) {
                        authors.add(mapResultSetToEntity(rs));
                    }
                    return null;
                });

        return authors;
    }


    public List<Author> getAllAuthorsByTrackId(int id) {
        List<Author> authors = new ArrayList<>();
        dbManager.executeQuery(GET_ALL_AUTHORS_BY_TRACK_ID_STMT,
                rs -> {
                    while (rs.next()) {
                        authors.add(mapResultSetToEntity(rs));
                    }
                    return null;
                }, id);

        return authors;
    }

    public List<Author> getAllActive() {
        List<Author> authors = new ArrayList<>();
        dbManager.executeQuery(GET_ALL_AUTHORS_ACTIVE_STMT,
                rs -> {
                    while (rs.next()) {
                        authors.add(mapResultSetToEntity(rs));
                    }
                    return null;
                });

        return authors;
    }

    private Author mapResultSetToEntity(ResultSet rs) throws SQLException {
        int id = rs.getInt(ID);
        String authorshipName = rs.getString(AUTHORSHIP_NAME);
        AuthorStatusEnum status = AuthorStatusEnum.fromInt(rs.getInt(STATUS));
        return new Author(id, authorshipName, status);
    }
}