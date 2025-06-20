package app.tracktune.utils;

import app.tracktune.exceptions.SQLiteException;
import app.tracktune.model.resource.Resource;
import app.tracktune.model.resource.ResourceDAO;
import app.tracktune.view.ViewManager;
import javafx.scene.control.Alert;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SQLiteScripts {
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

    public static void deleteTrack(DatabaseManager dbManager, int trackID) throws SQLException {
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
                    dbManager.executeUpdate(query, trackID, trackID);
                } else {
                    dbManager.executeUpdate(query, trackID);
                }
            }
        } catch (SQLiteException ex) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.DELETE, Strings.ERR_DELETE_TRACK, Alert.AlertType.ERROR);
            System.err.println(ex.getMessage());
        }
    }

    public static List<Resource> getMostRecentResources(DatabaseManager dbManager) {
        String query = """
            SELECT *
            FROM Resources
            ORDER BY CreationDate DESC
            LIMIT 5
        """;

        List<Resource> resources = new ArrayList<>();

        dbManager.executeQuery(
                query,
                rs -> {
                    while (rs.next()) {
                        resources.add(ResourceDAO.mapResultSetToEntity(rs));
                    }
                    return null;
                }, null
        );

        return resources;
    }

    public static List<Resource> getMostPopularResources(DatabaseManager dbManager) {
        String query = """
            SELECT R.*
            FROM Resources R
            WHERE R.trackID IN (
                SELECT trackID
                FROM Resources
                GROUP BY trackID
                ORDER BY COUNT(ID) DESC
                LIMIT 5
            )
            ORDER BY R.trackID
        """;

        List<Resource> resources = new ArrayList<>();

        dbManager.executeQuery(
                query,
                rs -> {
                    while (rs.next()) {
                        resources.add(ResourceDAO.mapResultSetToEntity(rs));
                    }
                    return null;
                },
                null
        );

        return resources;
    }

    public static List<Resource> getMostCommentedResources(DatabaseManager dbManager) {
        String query = """
            SELECT R.*
            FROM Resources R
            LEFT JOIN Comments C ON R.ID = C.resourceID
            GROUP BY R.ID
            ORDER BY COUNT(C.ID) DESC
            LIMIT 5
        """;

        List<Resource> resources = new ArrayList<>();

        dbManager.executeQuery(
                query,
                rs -> {
                    while (rs.next()) {
                        resources.add(ResourceDAO.mapResultSetToEntity(rs));
                    }
                    return null;
                },
                null
        );

        return resources;
    }
}
