package app.tracktune.model.genre;

import app.tracktune.Main;
import app.tracktune.exceptions.GenreAlreadyExistsExeception;
import app.tracktune.interfaces.DAO;
import app.tracktune.model.DatabaseManager;
import app.tracktune.utils.Strings;
import java.util.SortedSet;
import java.util.TreeSet;

public class GenreDAO implements DAO<Genre> {
    private final SortedSet<Genre> cache = new TreeSet<>();
    private final DatabaseManager dbManager;
    //FIELDS
    private static final String NAME = "name";
    private static final String DESCRIPTION = "description";
    // CRUD STATEMENTS
    private static final String INSERT_GENRE_STMT = """
        INSERT INTO Genres (name, description)
        VALUES (?, ?)
    """;
    private static final String UPDATE_GENRE_STMT = """
        UPDATE Genres
    """;
    private static final String DELETE_GENRE_STMT = """
        DELETE FROM Genres
        where name = ?
    """;
    private static final String GET_ALL_GENRES_STMT = """
        SELECT *
        FROM Genres
    """;

    public GenreDAO() {
        dbManager = Main.dbManager;
        refreshCache();
    }

    @Override
    public void refreshCache() {
        cache.clear();
        dbManager.executeQuery(GET_ALL_GENRES_STMT,
                rs -> {
                    while (rs.next()) {
                        String name = rs.getString(NAME);
                        String description = rs.getString(DESCRIPTION);

                        cache.add(new Genre(name, description));
                    }
                    return null;
                }
        );
    }

    @Override
    public void insert(Genre genre) {
        boolean success = false;
        if(alreadyExists(genre)){
            throw new GenreAlreadyExistsExeception(Strings.ERR_GENRE_ALREADY_EXISTS);
        }

        success = dbManager.executeUpdate(INSERT_GENRE_STMT,genre.getName(),genre.getDescription());
        if (success) {
            cache.add(genre);
        }
    }

    @Override
    public void update(Genre genre) {
        boolean success = false;
        success = dbManager.executeUpdate(
                UPDATE_GENRE_STMT,
                genre.getName(),
                genre.getDescription()
        );

        if(success){
            cache.remove(genre);
            cache.add(genre);
        }
    }

    @Override
    public void delete(Genre genre) {
        boolean success = dbManager.executeUpdate(
                DELETE_GENRE_STMT,
                genre.getName()
        );

        if (success) {
            cache.remove(genre);
        }
    }

    @Override
    public Genre getByKey(Object key) {
        return cache.stream()
                .filter(genre -> genre.getName().equals(key))
                .findFirst()
                .orElse(null);
    }

    @Override
    public SortedSet<Genre> getAll() {
        return cache;
    }

    @Override
    public boolean alreadyExists(Genre data) {
        return getByKey(data.getName()) != null;
    }
}
