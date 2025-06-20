package app.tracktune.model.track;

import app.tracktune.model.DatabaseManager;
import app.tracktune.model.musicalInstrument.MusicalInstrument;
import app.tracktune.model.musicalInstrument.MusicalInstrumentDAO;
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
 * Unit tests for the TrackInstrumentDAO class.
 * Verifies CRUD operations and queries involving TrackInstrument entities.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TrackInstrumentDAOTest {

    private TrackInstrumentDAO trackInstrumentDAO;

    private int trackId, trackId2;
    private int instrumentId1, instrumentId2, instrumentId3;

    @BeforeAll
    void setup() throws Exception {
        Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        Statement stmt = connection.createStatement();
        stmt.execute("PRAGMA foreign_keys = ON;");
        for (String query : DBInit.getDBInitStatement().split(";")) {
            if (!query.trim().isEmpty()) {
                stmt.execute(query.trim() + ";");
            }
        }

        DatabaseManager.setTestConnection(connection);
        DatabaseManager db = DatabaseManager.getInstance();
        trackInstrumentDAO = new TrackInstrumentDAO(db);
        TrackDAO trackDAO = new TrackDAO(db);
        MusicalInstrumentDAO instrumentDAO = new MusicalInstrumentDAO(db);
        UserDAO userDAO = new UserDAO(db);

        Administrator user = new Administrator("testUser", "passwordHash", "name", "surname", UserStatusEnum.ACTIVE, new Timestamp(System.currentTimeMillis()));
        int userID = userDAO.insert(user);

        trackId = trackDAO.insert(new Track(null, "Test Track", new Timestamp(System.currentTimeMillis()), userID));
        trackId2 = trackDAO.insert(new Track(null, "Test Track 2", new Timestamp(System.currentTimeMillis()), userID));

        instrumentId1 = instrumentDAO.insert(new MusicalInstrument("Test Instrument 1", "Test Description 1"));
        instrumentId2 = instrumentDAO.insert(new MusicalInstrument("Test Instrument 2", "Test Description 2"));
        instrumentId3 = instrumentDAO.insert(new MusicalInstrument("Test Instrument 3", "Test Description 3"));
    }

    @BeforeEach
    void cleanTrackInstruments() {
        for (TrackInstrument ti : trackInstrumentDAO.getAll()) {
            trackInstrumentDAO.deleteById(ti.getId());
        }
    }

    /**
     * Verifies that a TrackInstrument can be inserted and retrieved by ID.
     */
    @Test
    @Order(1)
    void testInsertAndGetById() {
        TrackInstrument ti = new TrackInstrument(trackId, instrumentId1);
        Integer id = trackInstrumentDAO.insert(ti);
        assertNotNull(id);

        TrackInstrument fetched = trackInstrumentDAO.getById(id);
        assertEquals(trackId, fetched.getTrackId());
        assertEquals(instrumentId1, fetched.getInstrumentId());
    }

    /**
     * Verifies that a TrackInstrument can be updated by ID.
     */
    @Test
    @Order(2)
    void testUpdate() {
        Integer id = trackInstrumentDAO.insert(new TrackInstrument(trackId, instrumentId1));
        TrackInstrument updated = new TrackInstrument(id, trackId, instrumentId2);
        trackInstrumentDAO.updateById(updated, id);

        TrackInstrument result = trackInstrumentDAO.getById(id);
        assertEquals(trackId, result.getTrackId());
        assertEquals(instrumentId2, result.getInstrumentId());
    }

    /**
     * Verifies that a TrackInstrument can be deleted by ID.
     */
    @Test
    @Order(3)
    void testDelete() {
        Integer id = trackInstrumentDAO.insert(new TrackInstrument(trackId, instrumentId1));
        trackInstrumentDAO.deleteById(id);

        assertThrows(app.tracktune.exceptions.SQLiteException.class, () -> trackInstrumentDAO.getById(id));
    }

    /**
     * Verifies that all TrackInstruments can be retrieved.
     */
    @Test
    @Order(4)
    void testGetAll() {
        trackInstrumentDAO.insert(new TrackInstrument(trackId, instrumentId1));
        trackInstrumentDAO.insert(new TrackInstrument(trackId2, instrumentId2));

        List<TrackInstrument> all = trackInstrumentDAO.getAll();
        assertTrue(all.size() >= 2);
    }

    /**
     * Verifies retrieval of TrackInstruments by track ID.
     */
    @Test
    @Order(5)
    void testGetByTrackId() {
        trackInstrumentDAO.insert(new TrackInstrument(trackId, instrumentId1));
        trackInstrumentDAO.insert(new TrackInstrument(trackId, instrumentId2));

        List<TrackInstrument> results = trackInstrumentDAO.getByTrackId(trackId);
        assertEquals(2, results.size());
        for (TrackInstrument ti : results) {
            assertEquals(trackId, ti.getTrackId());
        }
    }

    /**
     * Verifies retrieval of a TrackInstrument by track ID and instrument ID.
     */
    @Test
    @Order(6)
    void testGetByTrackIdAndInstrumentId() {
        trackInstrumentDAO.insert(new TrackInstrument(trackId, instrumentId3));
        TrackInstrument result = trackInstrumentDAO.getByTrackIdAndInstrumentId(trackId, instrumentId3);

        assertNotNull(result);
        assertEquals(trackId, result.getTrackId());
        assertEquals(instrumentId3, result.getInstrumentId());
    }
}
