package app.tracktune.controller;

import app.tracktune.model.resource.Resource;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.ByteArrayInputStream;
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
    protected static String getFormattedRequestDate(Timestamp date) {
        if (date == null) return "";
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy, HH:mm");
        return formatter.format(new Date(date.getTime()));
    }

    protected Node createPreview(Resource resource, double width, double height) {
        String mimeType = resource.getType().toString();

        if (mimeType.startsWith("png")) {
            ByteArrayInputStream bis = new ByteArrayInputStream(resource.getData());
            Image image = new Image(bis, width, height, true, true);
            return new ImageView(image);
        }

        if (mimeType.startsWith("video")) {
            FontIcon videoIcon = new FontIcon("mdi2v-video");
            videoIcon.setIconSize(60);
            return videoIcon;
        }

        if (mimeType.startsWith("audio")) {
            FontIcon audioIcon = new FontIcon("mdi2m-music");
            audioIcon.setIconSize(60);
            return audioIcon;
        }

        FontIcon fileIcon = new FontIcon("mdi2f-file");
        fileIcon.setIconSize(60);
        return fileIcon;
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
