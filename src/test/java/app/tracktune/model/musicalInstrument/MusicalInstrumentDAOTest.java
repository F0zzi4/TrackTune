package app.tracktune.model.musicalInstrument;

import app.tracktune.model.DatabaseManager;
import app.tracktune.model.track.Track;
import app.tracktune.model.track.TrackDAO;
import app.tracktune.model.track.TrackInstrument;
import app.tracktune.model.track.TrackInstrumentDAO;
import app.tracktune.model.user.Administrator;
import app.tracktune.model.user.UserDAO;
import app.tracktune.model.user.UserStatusEnum;
import app.tracktune.utils.DBInit;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the MusicalInstrumentDAO class.
 * Verifies CRUD operations and track-instrument associations.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MusicalInstrumentDAOTest {

    private DatabaseManager db;
    private MusicalInstrumentDAO instrumentDAO;
    private TrackDAO trackDAO;
    private TrackInstrumentDAO trackInstrumentDAO;
    private UserDAO userDAO;

    private int userId;
    private int trackId;

    /**
     * Initializes in-memory database and inserts a test user and track.
     */
    @BeforeAll
    void setup() throws Exception {
        Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        Statement stmt = connection.createStatement();
        stmt.execute("PRAGMA foreign_keys = ON;");

        String[] ddl = DBInit.getDBInitStatement().split(";");
        for (String query : ddl) {
            if (!query.trim().isEmpty()) stmt.execute(query.trim() + ";");
        }

        DatabaseManager.setTestConnection(connection);
        db = DatabaseManager.getInstance();
        instrumentDAO = new MusicalInstrumentDAO(db);
        trackDAO = new TrackDAO(db);
        trackInstrumentDAO = new TrackInstrumentDAO(db);
        userDAO = new UserDAO(db);

        Administrator testUser = new Administrator(
                "testuser", "passwordHash", "nome", "cognome",
                UserStatusEnum.ACTIVE, new Timestamp(System.currentTimeMillis())
        );
        userId = userDAO.insert(testUser);

        Track track = new Track(null, "Test Track", new Timestamp(System.currentTimeMillis()), userId);
        trackId = trackDAO.insert(track);
    }

    /**
     * Clears the MusicalInstruments and TracksInstruments tables before each test.
     */
    @BeforeEach
    void clearTable() {
        db.executeUpdate("DELETE FROM TracksInstruments");
        db.executeUpdate("DELETE FROM MusicalInstruments");
    }

    /**
     * Tests insertion and retrieval of a musical instrument by ID.
     */
    @Test
    void testInsertAndGetById() {
        MusicalInstrument instrument = new MusicalInstrument("Test Instrument", "Test Description");
        Integer id = instrumentDAO.insert(instrument);
        assertNotNull(id);

        MusicalInstrument fetched = instrumentDAO.getById(id);
        assertEquals("Test Instrument", fetched.getName());
        assertEquals("Test Description", fetched.getDescription());
    }

    /**
     * Tests update of a musical instrument and verifies the changes.
     */
    @Test
    void testUpdate() {
        MusicalInstrument instrument = new MusicalInstrument("Initial Instrument", "Initial Description");
        Integer id = instrumentDAO.insert(instrument);

        MusicalInstrument updated = new MusicalInstrument(id, "Updated Instrument", "Updated Description");
        instrumentDAO.updateById(updated, id);

        MusicalInstrument result = instrumentDAO.getById(id);
        assertEquals("Updated Instrument", result.getName());
        assertEquals("Updated Description", result.getDescription());
    }

    /**
     * Tests deletion of a musical instrument and verifies it no longer exists.
     */
    @Test
    void testDelete() {
        MusicalInstrument instrument = new MusicalInstrument("To Delete", "Delete Description");
        Integer id = instrumentDAO.insert(instrument);

        instrumentDAO.deleteById(id);

        assertThrows(app.tracktune.exceptions.SQLiteException.class, () -> instrumentDAO.getById(id));
    }

    /**
     * Tests retrieval of all musical instruments.
     */
    @Test
    void testGetAll() {
        instrumentDAO.insert(new MusicalInstrument("Instrument 1", "Description 1"));
        instrumentDAO.insert(new MusicalInstrument("Instrument 2", "Description 2"));

        List<MusicalInstrument> all = instrumentDAO.getAll();
        assertEquals(2, all.size());
    }

    /**
     * Tests retrieval of all instruments associated with a specific track.
     */
    @Test
    void testGetAllInstrumentByTrackId() {
        Integer id1 = instrumentDAO.insert(new MusicalInstrument("Track Instrument 1", "Track Description 1"));
        Integer id2 = instrumentDAO.insert(new MusicalInstrument("Track Instrument 2", "Track Description 2"));

        trackInstrumentDAO.insert(new TrackInstrument(trackId, id1));
        trackInstrumentDAO.insert(new TrackInstrument(trackId, id2));

        List<MusicalInstrument> instruments = instrumentDAO.getAllInstrumentByTrackId(trackId);
        assertNotNull(instruments);
        assertEquals(2, instruments.size());
        assertTrue(instruments.stream().anyMatch(i -> i.getId().equals(id1)));
        assertTrue(instruments.stream().anyMatch(i -> i.getId().equals(id2)));
    }
}
