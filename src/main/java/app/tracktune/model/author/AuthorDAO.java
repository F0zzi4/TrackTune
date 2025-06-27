package app.tracktune.model.author;

import app.tracktune.exceptions.SQLiteException;
import app.tracktune.interfaces.DAO;
import app.tracktune.utils.DatabaseManager;
import app.tracktune.utils.Strings;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Data Access Object (DAO) class for managing {@link Author} entities in the database.
 * Provides CRUD operations and queries specific to the Author table.
 */
public class AuthorDAO implements DAO<Author> {

    /**
     * Database manager instance used to execute SQL queries and updates.
     */
    private final DatabaseManager dbManager;

    // Column names used in the Authors table
    private static final String ID = "ID";
    private static final String AUTHORSHIP_NAME = "authorshipName";
    private static final String STATUS = "status";

    // SQL statements for CRUD and query operations
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
        WHERE status = 0
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

    /**
     * Constructs an AuthorDAO with the specified DatabaseManager.
     *
     * @param dbManager the DatabaseManager instance for DB access
     */
    public AuthorDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Inserts a new Author into the database.
     *
     * @param author the Author entity to insert
     * @return the generated ID of the inserted author
     * @throws SQLiteException if the insertion fails
     */
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

    /**
     * Updates an existing Author in the database by ID.
     *
     * @param author the Author entity with updated data
     * @param id     the ID of the author to update
     * @throws SQLiteException if the update fails
     */
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

    /**
     * Deletes an Author from the database by ID.
     *
     * @param id the ID of the author to delete
     * @throws SQLiteException if the deletion fails
     */
    @Override
    public void deleteById(int id) {
        boolean success = dbManager.executeUpdate(DELETE_AUTHOR_STMT, id);

        if (!success) {
            throw new SQLiteException(Strings.ERR_DATABASE);
        }
    }

    /**
     * Retrieves an Author by its ID.
     *
     * @param id the ID of the author to retrieve
     * @return the Author entity, or null if not found
     * @throws SQLiteException if the query fails
     */
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

    /**
     * Checks whether an Author exists by authorship name.
     *
     * @param authorshipName the authorship name to check
     * @return true if an Author with the given name exists, false otherwise
     */
    public boolean existByAuthorshipName(String authorshipName) {

        return dbManager.executeQuery(GET_AUTHOR_BY_AUTHORSHIP_NAME_STMT,
                ResultSet::next, authorshipName);
    }

    /**
     * Retrieves all Authors from the database.
     *
     * @return a list of all Authors
     */
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

    /**
     * Retrieves all Authors associated with a specific Track ID.
     *
     * @param id the track ID to find authors for
     * @return a list of Authors associated with the given track
     */
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

    /**
     * Retrieves all Authors with an active status.
     *
     * @return a list of active Authors
     */
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

    /**
     * Maps the current row of a ResultSet to an Author entity.
     *
     * @param rs the ResultSet positioned at a row
     * @return the mapped Author entity
     * @throws SQLException if a database access error occurs
     */
    private Author mapResultSetToEntity(ResultSet rs) throws SQLException {
        int id = rs.getInt(ID);
        String authorshipName = rs.getString(AUTHORSHIP_NAME);
        AuthorStatusEnum status = AuthorStatusEnum.fromInt(rs.getInt(STATUS));
        return new Author(id, authorshipName, status);
    }
}
