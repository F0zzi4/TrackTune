package app.tracktune.model.track;

import app.tracktune.model.DatabaseManager;
import app.tracktune.model.musicalInstrument.MusicalInstrument;
import app.tracktune.model.musicalInstrument.MusicalInstrumentDAO;
import app.tracktune.utils.DBInit;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TrackInstrumentDAOTest {

    private DatabaseManager db;
    private TrackInstrumentDAO trackInstrumentDAO;
    private TrackDAO trackDAO;
    private MusicalInstrumentDAO instrumentDAO;

    // Store IDs for test data
    private final int userId = 1; // Use the admin user that's created by default
    private int trackId;
    private int trackId2;
    private int trackId3;
    private int trackId4;
    private int instrumentId1;
    private int instrumentId2;
    private int instrumentId3;

    @BeforeAll
    void setup() throws Exception {
        // Use an in-memory database for testing
        String url = "jdbc:sqlite::memory:";
        Connection connection = DriverManager.getConnection(url);
        Statement stmt = connection.createStatement();
        stmt.execute("PRAGMA foreign_keys = ON;");
        String[] ddl = DBInit.getDBInitStatement().split(";");
        for (String query : ddl) {
            if (!query.trim().isEmpty()) stmt.execute(query.trim() + ";");
        }
        connection.close();

        // Manual override of the connection for testing
        db = DatabaseManager.getInstance();
        trackInstrumentDAO = new TrackInstrumentDAO(db);
        trackDAO = new TrackDAO(db);
        instrumentDAO = new MusicalInstrumentDAO(db);

        // Create a test track to use in the tests
        Track track = new Track(null, "Test Track", new Timestamp(System.currentTimeMillis()), userId);
        trackId = trackDAO.insert(track);

        // Create test instruments to use in the tests
        MusicalInstrument instrument1 = new MusicalInstrument("Test Instrument 1", "Test Description 1");
        MusicalInstrument instrument2 = new MusicalInstrument("Test Instrument 2", "Test Description 2");
        MusicalInstrument instrument3 = new MusicalInstrument("Test Instrument 3", "Test Description 3");
        instrumentId1 = instrumentDAO.insert(instrument1);
        instrumentId2 = instrumentDAO.insert(instrument2);
        instrumentId3 = instrumentDAO.insert(instrument3);

        // Create additional tracks for testing
        Track track2 = new Track(null, "Test Track 2", new Timestamp(System.currentTimeMillis()), userId);
        Track track3 = new Track(null, "Test Track 3", new Timestamp(System.currentTimeMillis()), userId);
        Track track4 = new Track(null, "Test Track 4", new Timestamp(System.currentTimeMillis()), userId);
        int trackId2 = trackDAO.insert(track2);
        int trackId3 = trackDAO.insert(track3);
        int trackId4 = trackDAO.insert(track4);

        System.out.println("[DEBUG_LOG] User ID: " + userId);
        System.out.println("[DEBUG_LOG] Track ID: " + trackId);
        System.out.println("[DEBUG_LOG] Instrument ID 1: " + instrumentId1);
        System.out.println("[DEBUG_LOG] Instrument ID 2: " + instrumentId2);
        System.out.println("[DEBUG_LOG] Instrument ID 3: " + instrumentId3);
    }

    @Test
    void testInsertAndGetById() {
        // Create a track-instrument relationship
        TrackInstrument trackInstrument = new TrackInstrument(trackId, instrumentId1);
        Integer id = trackInstrumentDAO.insert(trackInstrument);
        assertNotNull(id);

        // Get the track-instrument by ID
        TrackInstrument fetched = trackInstrumentDAO.getById(id);
        assertEquals(trackId, fetched.getTrackId());
        assertEquals(instrumentId1, fetched.getInstrumentId());
    }

    @Test
    void testUpdate() {
        // Create a track-instrument relationship
        TrackInstrument trackInstrument = new TrackInstrument(trackId, instrumentId1);
        Integer id = trackInstrumentDAO.insert(trackInstrument);

        // Update the track-instrument
        TrackInstrument updated = new TrackInstrument(id, trackId, instrumentId2);
        trackInstrumentDAO.updateById(updated, id);

        // Get the updated track-instrument
        TrackInstrument result = trackInstrumentDAO.getById(id);
        assertEquals(trackId, result.getTrackId());
        assertEquals(instrumentId2, result.getInstrumentId());
    }

    @Test
    void testDelete() {
        // Create a track-instrument relationship
        TrackInstrument trackInstrument = new TrackInstrument(trackId, instrumentId1);
        Integer id = trackInstrumentDAO.insert(trackInstrument);

        // Delete the track-instrument
        trackInstrumentDAO.deleteById(id);

        // The getById method should throw an exception when the track-instrument is not found
        assertThrows(app.tracktune.exceptions.SQLiteException.class, () -> {
            trackInstrumentDAO.getById(id);
        });
    }

    @Test
    void testGetAll() {
        // Create multiple track-instrument relationships
        TrackInstrument ti1 = new TrackInstrument(trackId, instrumentId1);
        TrackInstrument ti2 = new TrackInstrument(trackId, instrumentId2);
        trackInstrumentDAO.insert(ti1);
        trackInstrumentDAO.insert(ti2);

        // Get all track-instrument relationships
        List<TrackInstrument> all = trackInstrumentDAO.getAll();
        assertTrue(all.size() >= 2);
    }

    @Test
    void testGetByTrackId() {
        // Create multiple track-instrument relationships for the same track
        TrackInstrument ti1 = new TrackInstrument(trackId, instrumentId1);
        TrackInstrument ti2 = new TrackInstrument(trackId, instrumentId2);
        trackInstrumentDAO.insert(ti1);
        trackInstrumentDAO.insert(ti2);

        // Get all track-instrument relationships for the track
        List<TrackInstrument> byTrackId = trackInstrumentDAO.getByTrackId(trackId);
        assertTrue(byTrackId.size() >= 2);
    }

    @Test
    void testGetByTrackIdAndInstrumentId() {
        // Create a track-instrument relationship
        TrackInstrument ti = new TrackInstrument(trackId, instrumentId3);
        trackInstrumentDAO.insert(ti);

        // Get the track-instrument by track ID and instrument ID
        TrackInstrument result = trackInstrumentDAO.getByTrackIdAndInstrumentId(trackId, instrumentId3);
        assertNotNull(result);
        assertEquals(trackId, result.getTrackId());
        assertEquals(instrumentId3, result.getInstrumentId());
    }
}
