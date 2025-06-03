package app.tracktune.model.musicalInstrument;

import app.tracktune.model.DatabaseManager;
import app.tracktune.model.track.Track;
import app.tracktune.model.track.TrackDAO;
import app.tracktune.model.track.TrackInstrument;
import app.tracktune.model.track.TrackInstrumentDAO;
import app.tracktune.utils.DBInit;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MusicalInstrumentDAOTest {

    private DatabaseManager db;
    private MusicalInstrumentDAO instrumentDAO;
    private TrackDAO trackDAO;
    private TrackInstrumentDAO trackInstrumentDAO;
    
    // Store IDs for test data
    private final int userId = 1; // Use the admin user that's created by default
    private int trackId;

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
        instrumentDAO = new MusicalInstrumentDAO(db);
        trackDAO = new TrackDAO(db);
        trackInstrumentDAO = new TrackInstrumentDAO(db);

        // Create a test track to use in the tests
        Track track = new Track(null, "Test Track", new Timestamp(System.currentTimeMillis()), userId);
        trackId = trackDAO.insert(track);

        System.out.println("[DEBUG_LOG] User ID: " + userId);
        System.out.println("[DEBUG_LOG] Track ID: " + trackId);
    }

    @Test
    void testInsertAndGetById() {
        // Create a musical instrument
        MusicalInstrument instrument = new MusicalInstrument("Test Instrument", "Test Description");
        Integer id = instrumentDAO.insert(instrument);
        assertNotNull(id);

        // Get the instrument by ID
        MusicalInstrument fetched = instrumentDAO.getById(id);
        assertEquals("Test Instrument", fetched.getName());
        assertEquals("Test Description", fetched.getDescription());
    }

    @Test
    void testUpdate() {
        // Create a musical instrument
        MusicalInstrument instrument = new MusicalInstrument("Initial Instrument", "Initial Description");
        Integer id = instrumentDAO.insert(instrument);

        // Update the instrument
        MusicalInstrument updated = new MusicalInstrument(id, "Updated Instrument", "Updated Description");
        instrumentDAO.updateById(updated, id);

        // Get the updated instrument
        MusicalInstrument result = instrumentDAO.getById(id);
        assertEquals("Updated Instrument", result.getName());
        assertEquals("Updated Description", result.getDescription());
    }

    @Test
    void testDelete() {
        // Create a musical instrument
        MusicalInstrument instrument = new MusicalInstrument("To Delete", "Delete Description");
        Integer id = instrumentDAO.insert(instrument);

        // Delete the instrument
        instrumentDAO.deleteById(id);

        // The getById method should throw an exception when the instrument is not found
        assertThrows(app.tracktune.exceptions.SQLiteException.class, () -> {
            instrumentDAO.getById(id);
        });
    }

    @Test
    void testGetAll() {
        // Create multiple instruments
        MusicalInstrument i1 = new MusicalInstrument("Instrument 1", "Description 1");
        MusicalInstrument i2 = new MusicalInstrument("Instrument 2", "Description 2");
        instrumentDAO.insert(i1);
        instrumentDAO.insert(i2);

        // Get all instruments
        List<MusicalInstrument> all = instrumentDAO.getAll();
        assertTrue(all.size() >= 2);
    }

    @Test
    void testGetAllInstrumentByTrackId() {
        // Create instruments
        MusicalInstrument i1 = new MusicalInstrument("Track Instrument 1", "Track Description 1");
        MusicalInstrument i2 = new MusicalInstrument("Track Instrument 2", "Track Description 2");
        Integer id1 = instrumentDAO.insert(i1);
        Integer id2 = instrumentDAO.insert(i2);

        // Associate instruments with the track
        TrackInstrument ti1 = new TrackInstrument(trackId, id1);
        TrackInstrument ti2 = new TrackInstrument(trackId, id2);
        trackInstrumentDAO.insert(ti1);
        trackInstrumentDAO.insert(ti2);

        // Get all instruments for the track
        List<MusicalInstrument> instruments = instrumentDAO.getAllInstrumentByTrackId(trackId);
        assertNotNull(instruments);
        assertTrue(instruments.size() >= 2);

        // Verify the instruments are in the list
        boolean found1 = false;
        boolean found2 = false;
        for (MusicalInstrument i : instruments) {
            if (i.getId().equals(id1)) found1 = true;
            if (i.getId().equals(id2)) found2 = true;
        }
        assertTrue(found1);
        assertTrue(found2);
    }
}