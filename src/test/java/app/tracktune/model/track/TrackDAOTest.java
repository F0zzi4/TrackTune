package app.tracktune.model.track;

import app.tracktune.model.DatabaseManager;
import app.tracktune.model.author.Author;
import app.tracktune.model.author.AuthorDAO;
import app.tracktune.model.author.AuthorStatusEnum;
import app.tracktune.model.musicalInstrument.MusicalInstrument;
import app.tracktune.model.musicalInstrument.MusicalInstrumentDAO;
import app.tracktune.model.resource.Resource;
import app.tracktune.model.resource.ResourceDAO;
import app.tracktune.model.resource.ResourceTypeEnum;
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

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TrackDAOTest {

    private DatabaseManager db;
    private TrackDAO trackDAO;
    private AuthorDAO authorDAO;
    private MusicalInstrumentDAO instrumentDAO;
    private TrackAuthorDAO trackAuthorDAO;
    private TrackInstrumentDAO trackInstrumentDAO;
    private ResourceDAO resourceDAO;
    private UserDAO userDAO;

    private int userId;
    private int authorId;
    private int instrumentId;

    @BeforeAll
    void setup() throws Exception {
        // Creo la connessione e abilito foreign keys
        Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        Statement stmt = connection.createStatement();
        stmt.execute("PRAGMA foreign_keys = ON;");

        // Eseguo gli script di creazione tabelle
        String[] ddl = DBInit.getDBInitStatement().split(";");
        for (String query : ddl) {
            if (!query.trim().isEmpty()) stmt.execute(query.trim() + ";");
        }

        // Setto la connessione nel DatabaseManager
        DatabaseManager.setTestConnection(connection);
        db = DatabaseManager.getInstance();

        // Inizializzo DAO con la stessa istanza db
        trackDAO = new TrackDAO(db);
        authorDAO = new AuthorDAO(db);
        instrumentDAO = new MusicalInstrumentDAO(db);
        trackAuthorDAO = new TrackAuthorDAO(db);
        trackInstrumentDAO = new TrackInstrumentDAO(db);
        resourceDAO = new ResourceDAO(db);
        UserDAO userDAO = new UserDAO(db);

        // Inserisci utente di test
        Administrator testUser = new Administrator("testuser", "passwordHash", "nome", "cognome", UserStatusEnum.ACTIVE, new Timestamp(System.currentTimeMillis()));
        userId = userDAO.insert(testUser);

        // Inserisco dati di base
        authorId = authorDAO.insert(new Author(null, "Test Author", AuthorStatusEnum.ACTIVE));
        instrumentId = instrumentDAO.insert(new MusicalInstrument("Test Instrument", "Test Description"));
    }

    @BeforeEach
    void clearTables() {
        // Ordine corretto per evitare errori di FK
        db.executeUpdate("DELETE FROM Resources");
        db.executeUpdate("DELETE FROM TracksAuthors");
        db.executeUpdate("DELETE FROM TracksInstruments");
        db.executeUpdate("DELETE FROM Tracks");
        db.executeUpdate("DELETE FROM MusicalInstruments");
        db.executeUpdate("DELETE FROM Authors");

        // Reinserisco dati di base dopo la pulizia
        authorId = authorDAO.insert(new Author(null, "Test Author", AuthorStatusEnum.ACTIVE));
        instrumentId = instrumentDAO.insert(new MusicalInstrument("Test Instrument", "Test Description"));
    }

    @Test
    void testInsertAndGetById() {
        Track track = new Track(null, "Test Track", new Timestamp(System.currentTimeMillis()), userId);
        Integer id = trackDAO.insert(track);
        assertNotNull(id);

        Track fetched = trackDAO.getById(id);
        assertNotNull(fetched);
        assertEquals("Test Track", fetched.getTitle());
        assertEquals(userId, fetched.getUserID());
    }

    @Test
    void testUpdate() {
        Track track = new Track(null, "Initial", new Timestamp(System.currentTimeMillis()), userId);
        Integer id = trackDAO.insert(track);

        Track updated = new Track(id, "Updated", new Timestamp(System.currentTimeMillis()), userId);
        trackDAO.updateById(updated, id);

        Track result = trackDAO.getById(id);
        assertEquals("Updated", result.getTitle());
    }

    @Test
    void testDelete() {
        Track track = new Track(null, "To Delete", new Timestamp(System.currentTimeMillis()), userId);
        Integer id = trackDAO.insert(track);

        trackDAO.deleteById(id);

        // Dopo cancellazione, il getById dovrebbe restituire null oppure lanciare eccezione,
        // dipende dall'implementazione di getById.
        // Se lancia eccezione specifica, usala:
        assertThrows(RuntimeException.class, () -> trackDAO.getById(id));
    }

    @Test
    void testGetByTitle() {
        String title = "Unique Title " + System.nanoTime();
        Track track = new Track(null, title, new Timestamp(System.currentTimeMillis()), userId);
        trackDAO.insert(track);

        Track fetched = trackDAO.getByTitle(title);
        assertNotNull(fetched);
        assertEquals(title, fetched.getTitle());
    }

    @Test
    void testGetAll() {
        trackDAO.insert(new Track(null, "Track 1", new Timestamp(System.currentTimeMillis()), userId));
        trackDAO.insert(new Track(null, "Track 2", new Timestamp(System.currentTimeMillis()), userId));

        List<Track> all = trackDAO.getAll();
        assertTrue(all.size() >= 2);
    }

    @Test
    void testGetAllByAuthorId() {
        Track track = new Track(null, "By Author", new Timestamp(System.currentTimeMillis()), userId);
        Integer trackId = trackDAO.insert(track);

        trackAuthorDAO.insert(new TrackAuthor(trackId, authorId));

        List<Track> tracks = trackDAO.getAllByAuthorId(authorId);
        assertTrue(tracks.stream().anyMatch(t -> t.getId().equals(trackId)));
    }

    @Test
    void testGetAllByInstrumentId() {
        Track track = new Track(null, "By Instrument", new Timestamp(System.currentTimeMillis()), userId);
        Integer trackId = trackDAO.insert(track);

        trackInstrumentDAO.insert(new TrackInstrument(trackId, instrumentId));

        List<Track> tracks = trackDAO.getAllByInstrumentId(instrumentId);
        System.out.println("Tracks by instrumentId=" + instrumentId + ": " + tracks);;
        assertTrue(tracks.stream().anyMatch(t -> t.getId().equals(trackId)));
    }

    @Test
    void testGetTrackByResourceId() {
        Track track = new Track(null, "With Resource", new Timestamp(System.currentTimeMillis()), userId);
        Integer trackId = trackDAO.insert(track);

        Resource resource = new Resource(null, ResourceTypeEnum.pdf, new byte[]{1, 2},
                new Timestamp(System.currentTimeMillis()), true, false, trackId, userId);
        Integer resourceId = resourceDAO.insert(resource);

        Track fetched = trackDAO.getTrackByResourceId(resourceId);
        assertNotNull(fetched);
        assertEquals(trackId, fetched.getId());
    }

    @Test
    void testGetAllByTrackId() {
        Track track = new Track(null, "TrackID Method", new Timestamp(System.currentTimeMillis()), userId);
        Integer trackId = trackDAO.insert(track);

        List<Track> tracks = trackDAO.getAllByTrackId(trackId);
        assertNotNull(tracks);
        assertTrue(tracks.stream().anyMatch(t -> t.getId().equals(trackId)));
    }
}
