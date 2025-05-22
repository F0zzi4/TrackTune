package app.tracktune.utils;

import app.tracktune.exceptions.SQLiteException;
import app.tracktune.model.DatabaseManager;
import app.tracktune.view.ViewManager;
import javafx.scene.control.Alert;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class SQLiteScripts {
    public static final String SELECT_PENDING_USERS_BY_USERNAME = "SELECT * FROM PendingUsers WHERE username = ?";
    public static final String SELECT_USER_BY_USERNAME = "SELECT * FROM Users WHERE username = ?";

    /**
     * Method to avoid SQL Injection
     * @param texts texts input from the user
     * @return true if some text contains SQL keyword, false otherwise
     */
    public static boolean checkForSQLInjection(String... texts) {
        String pattern = "(--|;|\\bDROP\\b|\\bSELECT\\b|\\bINSERT\\b|\\bUPDATE\\b|\\bDELETE\\b|\\bTRUNCATE\\b|\\bALTER\\b|\\bCREATE\\b|\\bEXEC\\b|\\bUNION\\b|\\bFROM\\b|\\bWHERE\\b|\\bJOIN\\b)";
        boolean result = false;

        for (String text : texts) {
            if (text == null || text.isBlank()) {
                result = true;
                break;
            }

            if (text.toUpperCase().matches(".*" + pattern + ".*")) {
                result = true;
            }
        }

        return result;
    }

    public static void deleteTrack(DatabaseManager db, int trackID) throws SQLException {
        String[] queries = {
                "DELETE FROM Interactions WHERE commentID IN (SELECT ID FROM Comments WHERE trackID = ?) OR replyID IN (SELECT ID FROM Comments WHERE trackID = ?)",
                "DELETE FROM Comments WHERE trackID = ?",
                "DELETE FROM ResourcesAuthors WHERE resourceID IN (SELECT ID FROM Resources WHERE trackID = ?)",
                "DELETE FROM Resources WHERE trackID = ?",
                "DELETE FROM TracksInstruments WHERE trackID = ?",
                "DELETE FROM TracksAuthors WHERE trackID = ?",
                "DELETE FROM TracksGenres WHERE trackID = ?",
                "DELETE FROM Tracks WHERE ID = ?"
        };

        try {
            for (String query : queries) {
                if (query.contains("commentID IN")) {
                    db.executeUpdate(query, trackID, trackID);
                } else {
                    db.executeUpdate(query, trackID);
                }
            }
        } catch (SQLiteException ex) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.DELETE, Strings.ERR_DELETE_TRACK, Alert.AlertType.ERROR);
            System.err.println(ex.getMessage());
        }
    }

}
