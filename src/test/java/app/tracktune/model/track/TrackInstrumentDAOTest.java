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
 * Test class for TrackInstrumentDAO.
 * It tests CRUD operations on the TrackInstrument entity which links tracks and musical instruments.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TrackInstrumentDAOTest {

    private DatabaseManager db;
    private TrackInstrumentDAO trackInstrumentDAO;
    private TrackDAO trackDAO;
    private MusicalInstrumentDAO instrumentDAO;
    private UserDAO userDAO;

    private int userID;
    private int trackId;
    private int trackId2;
    private int trackId3;
    private int trackId4;
    private int instrumentId1;
    private int instrumentId2;
    private int instrumentId3;

    @BeforeAll
    void setup() throws Exception {
        Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        Statement stmt = connection.createStatement();
        stmt.execute("PRAGMA foreign_keys = ON;");
        String[] ddl = DBInit.getDBInitStatement().split(";");
        for (String query : ddl) {
            if (!query.trim().isEmpty()) {
                stmt.execute(query.trim() + ";");
            }
        }

        DatabaseManager.setTestConnection(connection);
        db = DatabaseManager.getInstance();
        trackInstrumentDAO = new TrackInstrumentDAO(db);
        trackDAO = new TrackDAO(db);
        instrumentDAO = new MusicalInstrumentDAO(db);
        userDAO = new UserDAO(db);

        Administrator testUser = new Administrator("testuser", "passwordHash", "nome", "cognome", UserStatusEnum.ACTIVE, new Timestamp(System.currentTimeMillis()));
        userID = userDAO.insert(testUser);

        Track track = new Track(null, "Test Track", new Timestamp(System.currentTimeMillis()), userID);
        trackId = trackDAO.insert(track);

        Track track2 = new Track(null, "Test Track 2", new Timestamp(System.currentTimeMillis()), userID);
        Track track3 = new Track(null, "Test Track 3", new Timestamp(System.currentTimeMillis()), userID);
        Track track4 = new Track(null, "Test Track 4", new Timestamp(System.currentTimeMillis()), userID);
        trackId2 = trackDAO.insert(track2);
        trackId3 = trackDAO.insert(track3);
        trackId4 = trackDAO.insert(track4);

        MusicalInstrument instrument1 = new MusicalInstrument("Test Instrument 1", "Test Description 1");
        MusicalInstrument instrument2 = new MusicalInstrument("Test Instrument 2", "Test Description 2");
        MusicalInstrument instrument3 = new MusicalInstrument("Test Instrument 3", "Test Description 3");
        instrumentId1 = instrumentDAO.insert(instrument1);
        instrumentId2 = instrumentDAO.insert(instrument2);
        instrumentId3 = instrumentDAO.insert(instrument3);
    }

    @BeforeEach
    void cleanTrackInstruments() {
        List<TrackInstrument> all = trackInstrumentDAO.getAll();
        for (TrackInstrument ti : all) {
            trackInstrumentDAO.deleteById(ti.getId());
        }
    }

    /**
     * Tests insertion of a TrackInstrument and retrieval by ID.
     */
    @Test
    @Order(1)
    void testInsertAndGetById() {
        TrackInstrument trackInstrument = new TrackInstrument(trackId, instrumentId1);
        Integer id = trackInstrumentDAO.insert(trackInstrument);
        assertNotNull(id);

        TrackInstrument fetched = trackInstrumentDAO.getById(id);
        assertEquals(trackId, fetched.getTrackId());
        assertEquals(instrumentId1, fetched.getInstrumentId());
    }

    /**
     * Tests updating an existing TrackInstrument record.
     */
    @Test
    @Order(2)
    void testUpdate() {
        TrackInstrument trackInstrument = new TrackInstrument(trackId, instrumentId1);
        Integer id = trackInstrumentDAO.insert(trackInstrument);

        TrackInstrument updated = new TrackInstrument(id, trackId, instrumentId2);
        trackInstrumentDAO.updateById(updated, id);

        TrackInstrument result = trackInstrumentDAO.getById(id);
        assertEquals(trackId, result.getTrackId());
        assertEquals(instrumentId2, result.getInstrumentId());
    }

    /**
     * Tests deletion of a TrackInstrument record by ID.
     */
    @Test
    @Order(3)
    void testDelete() {
        TrackInstrument trackInstrument = new TrackInstrument(trackId, instrumentId1);
        Integer id = trackInstrumentDAO.insert(trackInstrument);

        trackInstrumentDAO.deleteById(id);

        assertThrows(app.tracktune.exceptions.SQLiteException.class, () -> {
            trackInstrumentDAO.getById(id);
        });
    }

    /**
     * Tests retrieval of all TrackInstrument records.
     */
    @Test
    @Order(4)
    void testGetAll() {
        TrackInstrument ti1 = new TrackInstrument(trackId, instrumentId1);
        TrackInstrument ti2 = new TrackInstrument(trackId2, instrumentId2);
        trackInstrumentDAO.insert(ti1);
        trackInstrumentDAO.insert(ti2);

        List<TrackInstrument> all = trackInstrumentDAO.getAll();
        assertTrue(all.size() >= 2);
    }

    /**
     * Tests retrieval of TrackInstrument records filtered by track ID.
     */
    @Test
    @Order(5)
    void testGetByTrackId() {
        TrackInstrument ti1 = new TrackInstrument(trackId, instrumentId1);
        TrackInstrument ti2 = new TrackInstrument(trackId, instrumentId2);
        trackInstrumentDAO.insert(ti1);
        trackInstrumentDAO.insert(ti2);

        List<TrackInstrument> byTrackId = trackInstrumentDAO.getByTrackId(trackId);
        assertTrue(byTrackId.size() >= 2);
        for (TrackInstrument ti : byTrackId) {
            assertEquals(trackId, ti.getTrackId());
        }
    }

    /**
     * Tests retrieval of a TrackInstrument record filtered by track ID and instrument ID.
     */
    @Test
    @Order(6)
    void testGetByTrackIdAndInstrumentId() {
        TrackInstrument ti = new TrackInstrument(trackId, instrumentId3);
        trackInstrumentDAO.insert(ti);

        TrackInstrument result = trackInstrumentDAO.getByTrackIdAndInstrumentId(trackId, instrumentId3);
        assertNotNull(result);
        assertEquals(trackId, result.getTrackId());
        assertEquals(instrumentId3, result.getInstrumentId());
    }
}
