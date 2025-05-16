package app.tracktune.model.author;

import app.tracktune.Main;
import app.tracktune.exceptions.PendingUserAlreadyExistsException;
import app.tracktune.interfaces.DAO;
import app.tracktune.model.DatabaseManager;
import app.tracktune.utils.Strings;
import java.util.SortedSet;
import java.util.TreeSet;

public class AuthorDAO implements DAO<Author> {
    private final SortedSet<Author> cache = new TreeSet<>();
    private final DatabaseManager dbManager;
    //FIELD
    private static final String AUTHORSHIP_NAME = "authorshipName";
    private static final String STATUS = "status";

    private static final String INSERT_AUTHOR_STMT = """
        INSERT INTO Authors (authorshipName, status)
        VALUES (?, ?)
    """;

    private static final String UPDATE_AUTHOR_STMT = """
        UPDATE Authors
        SET authorshipName = ?, status = ?
        WHERE authorshipName = ?
    """;

    private static final String DELETE_AUTHOR_STMT = """
            DELETE FROM Authors
            WHERE authorshipName = ?
    """;

    private static final String GET_ALL_AUTHORS_STMT = """
            SELECT *
            FROM Authors
    """;

    public AuthorDAO() {
        dbManager = Main.dbManager;
        refreshCache();
    }

    @Override
    public void refreshCache() {
        cache.clear();
        dbManager.executeQuery(GET_ALL_AUTHORS_STMT,
                rs -> {
                    while (rs.next()) {
                        String authorshipName = rs.getString(AUTHORSHIP_NAME);
                        AuthorStatusEnum status = AuthorStatusEnum.fromInt(rs.getInt(STATUS));
                        cache.add(new Author(authorshipName, status));
                    }
                    return null;
                }
        );
    }

    @Override
    public void insert(Author author) {
        if(alreadyExists(author)){
            throw new PendingUserAlreadyExistsException(Strings.ERR_PENDING_USER_ALREADY_EXISTS);
        }

        boolean success = dbManager.executeUpdate(INSERT_AUTHOR_STMT,
                author.getAuthorshipName(),
                author.getStatus().ordinal()
        );

        if(success)
            cache.add(author);

    }

    @Override
    public void update(Author author) {
        boolean success = dbManager.executeUpdate(
                UPDATE_AUTHOR_STMT,
                author.getAuthorshipName(),
                author.getStatus().ordinal(),
                author.getAuthorshipName()
        );

        if (success) {
            cache.remove(author);
            cache.add(author);
        }
    }

    @Override
    public void delete(Author author) {

    }

    @Override
    public Author getByKey(Object key) {
        return cache.stream()
                .filter(pendingUser -> pendingUser.getAuthorshipName().equals(key))
                .findFirst()
                .orElse(null);
    }

    @Override
    public SortedSet<Author> getAll() {
        return cache;
    }

    @Override
    public boolean alreadyExists(Author author) {
        return getByKey(author.getAuthorshipName()) != null;
    }
}
