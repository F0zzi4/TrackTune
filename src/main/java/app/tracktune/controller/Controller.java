package app.tracktune.controller;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class Controller {
    protected Controller parentController;

    public void setParentController(Controller parentController){
        this.parentController = parentController;
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
