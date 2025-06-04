# Documentazione del Framework di Test per TrackTune

## Introduzione
Questo documento descrive il framework di test utilizzato nel progetto TrackTune, un'applicazione per la gestione di tracce musicali. Il documento fornisce una panoramica dell'approccio di testing adottato, dei framework utilizzati e della struttura dei test.

## Framework di Test Utilizzati

### JUnit 5 (Jupiter)
Il progetto utilizza JUnit 5 (noto anche come JUnit Jupiter) come framework principale per i test. JUnit 5 è un framework di testing moderno per Java che supporta diverse funzionalità avanzate rispetto alle versioni precedenti.

Caratteristiche principali utilizzate:
- **Annotazioni**: `@Test`, `@BeforeAll`, `@TestInstance`
- **Asserzioni**: `assertEquals`, `assertNotNull`, `assertTrue`, `assertThrows`
- **Ciclo di vita dei test**: Configurazione `PER_CLASS` per condividere lo stato tra i test

## Struttura dei Test

### Organizzazione dei Test
I test sono organizzati in package che rispecchiano la struttura del codice principale:
- `app.tracktune.model.author` - Test per le classi relative agli autori
- `app.tracktune.model.genre` - Test per le classi relative ai generi musicali
- `app.tracktune.model.track` - Test per le classi relative alle tracce musicali

### Pattern di Test
I test seguono un pattern comune:
1. **Setup**: Inizializzazione di un database SQLite in memoria per i test
2. **Test CRUD**: Test per le operazioni Create, Read, Update, Delete
3. **Test di relazioni**: Test per le relazioni tra entità (es. tracce-generi, tracce-autori)

## Database di Test

### Database In-Memory
Per i test viene utilizzato un database SQLite in memoria, che offre diversi vantaggi:
- Isolamento completo tra i test
- Velocità di esecuzione elevata
- Nessun effetto collaterale sui dati reali

### Inizializzazione del Database
Il database viene inizializzato prima dell'esecuzione dei test utilizzando la classe `DBInit` che contiene gli script SQL per la creazione delle tabelle.

```java
// Esempio di inizializzazione del database per i test
String url = "jdbc:sqlite::memory:";
Connection connection = DriverManager.getConnection(url);
Statement stmt = connection.createStatement();
stmt.execute("PRAGMA foreign_keys = ON;");
String[] ddl = DBInit.getDBInitStatement().split(";");
for (String query : ddl) {
    if (!query.trim().isEmpty()) stmt.execute(query.trim() + ";");
}
```

## Classi di Test Principali

### AuthorDAOTest
Test per la classe `AuthorDAO` che gestisce le operazioni CRUD per gli autori musicali.

### GenreDAOTest
Test per la classe `GenreDAO` che gestisce le operazioni CRUD per i generi musicali.

### TrackGenreDAOTest
Test per la classe `TrackGenreDAO` che gestisce le relazioni tra tracce e generi musicali.

### TrackDAOTest
Test per la classe `TrackDAO` che gestisce le operazioni CRUD per le tracce musicali.

### TrackAuthorDAOTest
Test per la classe `TrackAuthorDAO` che gestisce le relazioni tra tracce e autori.

### ResourceDAOTest
Test per la classe `ResourceDAO` che gestisce le operazioni CRUD per le risorse multimediali.

### CommentDAOTest
Test per la classe `CommentDAO` che gestisce le operazioni CRUD per i commenti.

### MusicalInstrumentDAOTest
Test per la classe `MusicalInstrumentDAO` che gestisce le operazioni CRUD per gli strumenti musicali.

### TrackInstrumentDAOTest
Test per la classe `TrackInstrumentDAO` che gestisce le relazioni tra tracce e strumenti musicali.

### PendingUserDAOTest
Test per la classe `PendingUserDAO` che gestisce le operazioni CRUD per gli utenti in attesa di approvazione.

### UserDAOTest
Test per la classe `UserDAO` che gestisce le operazioni CRUD per gli utenti autenticati e gli amministratori.

## Approccio al Testing

### Test di Integrazione
I test verificano l'integrazione tra le classi DAO e il database, assicurando che le operazioni di persistenza funzionino correttamente.

### Test Isolati
Ogni test è isolato dagli altri grazie all'uso di un database in memoria che viene inizializzato prima di ogni suite di test.

### Debug e Logging
I test utilizzano logging di debug per facilitare la diagnosi dei problemi:
```java
System.out.println("[DEBUG_LOG] Track ID: " + trackId);
```

## Esecuzione dei Test

### Runner di Test
La classe `AllTestsRunner` fornisce informazioni sui test disponibili nel progetto, anche se non esegue direttamente i test.

### Esecuzione tramite IDE o Maven
I test possono essere eseguiti tramite l'IDE o utilizzando Maven con il comando:
```
mvn test
```

## Conclusioni
Il framework di test utilizzato in TrackTune è ben strutturato e segue le best practice moderne per il testing in Java. L'uso di JUnit 5 e di un database in memoria per i test garantisce test affidabili, veloci e isolati.
