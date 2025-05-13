package app.tracktune.controller.admin;

import app.tracktune.exceptions.SQLInjectionException;
import app.tracktune.exceptions.TrackTuneException;
import app.tracktune.model.genre.Genre;
import app.tracktune.model.genre.GenreDAO;
import app.tracktune.utils.SQLiteScripts;
import app.tracktune.utils.Strings;
import app.tracktune.view.ViewManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import java.io.Serializable;
import java.util.*;

/**
 * Controller for managing musical genres in the admin panel.
 * Allows the admin to add new genres, view them with pagination,
 * and delete them through a JavaFX interface.
 */
public class GenreController implements Serializable {
    @FXML
    private VBox requestsContainer;
    @FXML
    private Button prevButton;
    @FXML
    private Button nextButton;
    @FXML
    private Button addGenreButton;
    @FXML
    private TextField nameField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private Label charCountLabel;

    private SortedSet<Genre> genreList = new TreeSet<>();
    private int currentPage = 0;
    private final int itemsPerPage = 5;
    private final GenreDAO genreDAO = new GenreDAO();

    /**
     * Initializes the controller by loading all genres from the database,
     * setting up pagination buttons, and configuring the genre creation form.
     */
    @FXML
    public void initialize() {
        genreList = genreDAO.getAll();

        prevButton.setOnAction(e -> {
            if (currentPage > 0) {
                currentPage--;
                updateRequests();
            }
        });

        nextButton.setOnAction(e -> {
            if ((currentPage + 1) * itemsPerPage < genreList.size()) {
                currentPage++;
                updateRequests();
            }
        });

        addGenreButton.setOnAction(e -> {
            String name = nameField.getText().trim();
            String description = descriptionField.getText().trim();

            try{
                if (!name.isEmpty() && !description.isEmpty()) {
                    if(SQLiteScripts.checkForSQLInjection(name, description))
                        throw new SQLInjectionException(Strings.ERR_SQL_INJECTION);

                    if (genreList.stream().noneMatch(g -> g.getName().equalsIgnoreCase(name))) {
                        Genre newGenre = new Genre(name, description);
                        genreDAO.insert(newGenre);
                        genreList.add(newGenre);
                        nameField.clear();
                        descriptionField.clear();
                        charCountLabel.setText("0/300");
                        updateRequests();
                    } else {
                        throw new TrackTuneException(Strings.ERR_GENRE_ALREADY_EXISTS);
                    }
                }
                else
                    throw new TrackTuneException(Strings.FIELD_EMPTY);
            }catch (TrackTuneException exception){
                ViewManager.setAndShowAlert(Strings.ERROR, Strings.GENRE_FAILED, exception.getMessage(), Alert.AlertType.ERROR);
            }
        });

        descriptionField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 300) {
                descriptionField.setText(oldValue);
            } else {
                charCountLabel.setText(newValue.length() + "/300");
            }
        });

        updateRequests();
    }

    /**
     * Updates the displayed genres by showing only those on the current page.
     * Also enables or disables the pagination buttons accordingly.
     */
    private void updateRequests() {
        requestsContainer.getChildren().clear();

        int totalRequests = genreList.size();
        int start = currentPage * itemsPerPage;
        int end = Math.min(start + itemsPerPage, totalRequests);

        prevButton.setDisable(currentPage == 0);
        nextButton.setDisable(end >= totalRequests);

        List<Genre> pageItems = new ArrayList<>(genreList).subList(start, end);

        for (Genre genre : pageItems) {
            HBox requestBox = createRequestItem(genre);
            requestsContainer.getChildren().add(requestBox);
        }
    }

    /**
     * Creates an HBox representing a single genre item.
     * Includes the genre name, description, and a delete button.
     *
     * @param genre the {@link Genre} to display
     * @return an {@link HBox} containing the genre's details and actions
     */
    private HBox createRequestItem(Genre genre) {
        Label nameLabel = new Label(genre.getName());
        nameLabel.getStyleClass().add("request-item-title");

        Label descLabel = new Label(genre.getDescription());
        descLabel.getStyleClass().add("request-item-description");
        descLabel.setWrapText(true);

        VBox textBox = new VBox(5, nameLabel, descLabel);
        textBox.setAlignment(Pos.CENTER_LEFT);

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("reject-button");
        deleteBtn.setOnAction(e -> deleteGenre(genre));
        deleteBtn.setMinWidth(80);

        HBox buttonBox = new HBox(deleteBtn);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox box = new HBox(10, textBox, spacer, buttonBox);
        box.getStyleClass().add("request-item");
        box.setAlignment(Pos.CENTER_LEFT);

        descLabel.maxWidthProperty().bind(box.widthProperty().subtract(deleteBtn.widthProperty()).subtract(100));
        textBox.maxWidthProperty().bind(descLabel.maxWidthProperty());

        return box;
    }


    /**
     * Deletes a genre from the database and updates the displayed list.
     *
     * @param genre the {@link Genre} to delete
     */
    private void deleteGenre(Genre genre){
        try {
            genreDAO.delete(genre);

            removeGenreAndUpdate(genre);
        } catch (Exception ex) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERR_GENRE_DELETE_ERROR, ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Removes the specified genre from the local list,
     * adjusts pagination if necessary, and refreshes the view.
     *
     * @param genre the {@link Genre} to remove
     */
    private void removeGenreAndUpdate(Genre genre) {
        genreList.remove(genre);
        int maxPage = (int) Math.ceil((double) genreList.size() / itemsPerPage);
        if (currentPage >= maxPage && currentPage > 0) {
            currentPage--;
        }
        updateRequests();
    }
}
