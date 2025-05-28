package app.tracktune.controller;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class Controller {
    public Controller parentController;

    public void setParentController(Controller parentController){
        this.parentController = parentController;
    }

    /**
     * Get the formatted request date, showing only up to minutes.
     * @param date date to convert into dd MMM yyyy, HH:mm format
     * @return Formatted request date
     */
    protected static String getFormattedRequestDate(Timestamp date) {
        if (date == null) return "";
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy, HH:mm");
        return formatter.format(new Date(date.getTime()));
    }

    /**
     * Converts a string to title case, capitalizing the first letter of each word.
     *
     * @param input the input string
     * @return the formatted string in title case
     */
    public static String toTitleCase(String input) {
        if (input == null || input.isEmpty()) return input;

        String[] words = input.toLowerCase().split(" ");
        StringBuilder titleCase = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                titleCase.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }

        return titleCase.toString().trim();
    }
}
