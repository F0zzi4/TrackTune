package app.tracktune.model.musicalInstrument;

import app.tracktune.Main;
import app.tracktune.exceptions.GenreAlreadyExistsExeception;
import app.tracktune.interfaces.DAO;
import app.tracktune.model.DatabaseManager;
import app.tracktune.utils.Strings;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Data Access Object (DAO) implementation for managing {@link MusicalInstrument} entities.
 * <p>
 * Provides methods to interact with the database for performing CRUD operations on musical instruments.
 * Maintains a local cache to reduce repeated database queries and to ensure ordering via {@link TreeSet}.
 * </p>
 */
public class MusicalInstrumentDAO implements DAO<MusicalInstrument> {

    private final SortedSet<MusicalInstrument> cache = new TreeSet<>();
    private final DatabaseManager dbManager;

    // Database field constants
    private static final String NAME = "name";
    private static final String DESCRIPTION = "description";

    // SQL statements
    private static final String INSERT_MUSICAL_INSTRUMENT_STMT = """
        INSERT INTO MusicalInstruments (name, description)
        VALUES (?, ?)
    """;

    private static final String UPDATE_MUSICAL_INSTRUMENT_STMT = """
        UPDATE MusicalInstruments
    """;

    private static final String DELETE_MUSICAL_INSTRUMENT_STMT = """
        DELETE FROM MusicalInstruments
        WHERE name = ?
    """;

    private static final String GET_ALL_MUSICAL_INSTRUMENTS_STMT = """
        SELECT *
        FROM MusicalInstruments
    """;

    /**
     * Constructs a new {@code MusicalInstrumentDAO} and initializes the cache
     * by fetching all musical instruments from the database.
     */
    public MusicalInstrumentDAO() {
        this.dbManager = Main.dbManager;
        refreshCache();
    }

    /**
     * Reloads the cache by fetching all musical instruments from the database.
     * <p>
     * This method clears the existing cache and repopulates it with up-to-date data.
     * </p>
     */
    @Override
    public void refreshCache() {
        cache.clear();
        dbManager.executeQuery(GET_ALL_MUSICAL_INSTRUMENTS_STMT,
                rs -> {
                    while (rs.next()) {
                        String name = rs.getString(NAME);
                        String description = rs.getString(DESCRIPTION);
                        cache.add(new MusicalInstrument(name, description));
                    }
                    return null;
                }
        );
    }

    /**
     * Inserts a new {@link MusicalInstrument} into the database and adds it to the cache.
     *
     * @param instrument the instrument to be inserted
     * @throws GenreAlreadyExistsExeception if the instrument already exists in the database
     */
    @Override
    public void insert(MusicalInstrument instrument) {
        if (alreadyExists(instrument)) {
            throw new GenreAlreadyExistsExeception(Strings.ERR_GENRE_ALREADY_EXISTS);
        }

        boolean success = dbManager.executeUpdate(
                INSERT_MUSICAL_INSTRUMENT_STMT,
                instrument.getName(),
                instrument.getDescription()
        );

        if (success) {
            cache.add(instrument);
        }
    }

    /**
     * Updates an existing {@link MusicalInstrument} in the database and the cache.
     *
     * @param instrument the instrument to be updated
     */
    @Override
    public void update(MusicalInstrument instrument) {
        boolean success = dbManager.executeUpdate(
                UPDATE_MUSICAL_INSTRUMENT_STMT,
                instrument.getName(),
                instrument.getDescription()
        );

        if (success) {
            cache.remove(instrument);
            cache.add(instrument);
        }
    }

    /**
     * Deletes a {@link MusicalInstrument} from the database and removes it from the cache.
     *
     * @param instrument the instrument to be deleted
     */
    @Override
    public void delete(MusicalInstrument instrument) {
        boolean success = dbManager.executeUpdate(
                DELETE_MUSICAL_INSTRUMENT_STMT,
                instrument.getName()
        );

        if (success) {
            cache.remove(instrument);
        }
    }

    /**
     * Retrieves a {@link MusicalInstrument} from the cache by its name.
     *
     * @param key the name of the instrument
     * @return the matching instrument, or {@code null} if not found
     */
    @Override
    public MusicalInstrument getByKey(Object key) {
        return cache.stream()
                .filter(instrument -> instrument.getName().equals(key))
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns all musical instruments currently stored in the cache.
     *
     * @return a {@link Set} containing all instruments
     */
    @Override
    public Set<MusicalInstrument> getAll() {
        return cache;
    }

    /**
     * Checks if a given {@link MusicalInstrument} already exists in the cache.
     *
     * @param instrument the instrument to check
     * @return {@code true} if it already exists, {@code false} otherwise
     */
    @Override
    public boolean alreadyExists(MusicalInstrument instrument) {
        return getByKey(instrument.getName()) != null;
    }
}
