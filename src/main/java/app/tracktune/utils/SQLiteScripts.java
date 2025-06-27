package app.tracktune.utils;

import app.tracktune.model.resource.Resource;
import app.tracktune.model.resource.ResourceDAO;

import java.util.ArrayList;
import java.util.List;

public class SQLiteScripts {
    /**
     * Checks if any of the provided input strings contain suspicious SQL keywords or characters
     * commonly used in SQL injection attacks.
     *
     * @param texts Variable number of input strings to check.
     * @return {@code true} if any text is null, blank, or contains SQL keywords potentially
     * indicating injection attempts; {@code false} otherwise.
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

    /**
     * Retrieves the 5 most recent resources based on their creation date.
     *
     * @param dbManager DatabaseManager instance to execute SQL queries.
     * @return A list of the 5 most recently created {@link Resource} objects.
     */
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

    /**
     * Retrieves resources associated with the top 5 tracks having the most resources.
     * Useful to find the most "popular" resources by track count.
     *
     * @param dbManager DatabaseManager instance to execute SQL queries.
     * @return A list of {@link Resource} objects belonging to the top 5 tracks with the most resources.
     */
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

        return resources.size() > 5 ? resources.subList(0, 5) : resources;
    }

    /**
     * Retrieves the top 5 resources with the highest number of comments.
     *
     * @param dbManager DatabaseManager instance to execute SQL queries.
     * @return A list of the 5 most commented {@link Resource} objects.
     */
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
