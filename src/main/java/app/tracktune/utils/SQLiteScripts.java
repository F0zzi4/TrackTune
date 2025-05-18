package app.tracktune.utils;

import java.sql.Date;
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

    /**
     * Get the formatted request date, showing only up to minutes.
     * @param date date to convert into dd MMM yyyy, HH:mm format
     * @return Formatted request date
     */
    public static String getFormattedRequestDate(Timestamp date) {
        if (date == null) return "";
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy, HH:mm");
        return formatter.format(new Date(date.getTime()));
    }
}
