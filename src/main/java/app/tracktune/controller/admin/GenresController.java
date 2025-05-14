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
 * Provides functionality for the admin to add, view, and delete genres,
 * as well as manage pagination for the genres list.
 * <p>
 * The controller handles genre creation, input validation,
 * and interaction with the genre database through {@link GenreDAO}.
 * Pagination is handled to display genres in a manageable way.
 * </p>
 */
public class GenresController implements Serializable {

    @FXML
    private VBox requestsContainer;
    @FXML
    private Button BtnPrev;
    @FXML
    private Button BtnNext;
    @FXML
    private Button BtnAddGenre;
    @FXML
    private TextField TxtName;
    @FXML
    private TextArea TxtDescription;
    @FXML
    private Label LblCharCount;

    private SortedSet<Genre> genreList = new TreeSet<>();
    private int currentPage = 0;
    private final int itemsPerPage = 4;
    private final GenreDAO genreDAO = new GenreDAO();

    /**
     * Initializes the controller by loading all genres from the database,
     * setting up pagination buttons, and configuring the genre creation form.
     * <p>
     * This method sets up listeners for pagination buttons, handles genre creation,
     * and tracks changes in the genre description field to ensure that it does not exceed 300 characters.
     * </p>
     */
    @FXML
    public void initialize() {
        genreList = genreDAO.getAll();

        BtnPrev.setOnAction(e -> {
            if (currentPage > 0) {
                currentPage--;
                updateRequests();
            }
        });

        BtnNext.setOnAction(e -> {
            if ((currentPage + 1) * itemsPerPage < genreList.size()) {
                currentPage++;
                updateRequests();
            }
        });

        BtnAddGenre.setOnAction(e -> {
            String name = TxtName.getText().trim();
            String description = TxtDescription.getText().trim();

            try{
                if (!name.isEmpty() && !description.isEmpty()) {
                    if(SQLiteScripts.checkForSQLInjection(name, description))
                        throw new SQLInjectionException(Strings.ERR_SQL_INJECTION);

                    if (genreList.stream().noneMatch(g -> g.getName().equalsIgnoreCase(name))) {
                        Genre newGenre = new Genre(name, description);
                        genreDAO.insert(newGenre);
                        genreList.add(newGenre);
                        TxtName.clear();
                        TxtDescription.clear();
                        LblCharCount.setText("0/300");
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

        TxtDescription.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 300) {
                TxtDescription.setText(oldValue);
            } else {
                LblCharCount.setText(newValue.length() + "/300");
            }
        });

        updateRequests();
    }

    /**
     * Updates the displayed genres by showing only those on the current page.
     * <p>
     * This method is responsible for clearing the current list of displayed genres,
     * calculating which genres to show based on the current page, and updating the view.
     * It also enables or disables the pagination buttons based on the current page and total number of genres.
     * </p>
     */
    private void updateRequests() {
        requestsContainer.getChildren().clear();

        int totalRequests = genreList.size();
        int start = currentPage * itemsPerPage;
        int end = Math.min(start + itemsPerPage, totalRequests);

        BtnPrev.setDisable(currentPage == 0);
        BtnNext.setDisable(end >= totalRequests);

        List<Genre> pageItems = new ArrayList<>(genreList).subList(start, end);

        for (Genre genre : pageItems) {
            HBox requestBox = createRequestItem(genre);
            requestsContainer.getChildren().add(requestBox);
        }
    }

    /**
     * Creates an HBox representing a single genre item.
     * <p>
     * This method creates an HBox containing a label with the genre name, a label with the description,
     * and a button for deleting the genre. The HBox is then returned for display in the pagination list.
     * </p>
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

        Button deleteBtn = new Button(Strings.DELETE);
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
     * <p>
     * This method handles the deletion of a genre from the database,
     * and then updates the list of displayed genres to reflect the removal.
     * It also handles errors that may occur during the deletion process.
     * </p>
     *
     * @param genre the {@link Genre} to delete
     */
    private void deleteGenre(Genre genre){
        try {
            genreDAO.delete(genre);
            removeGenreAndUpdate(genre);
        } catch (Exception ex) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERR_GENERAL, ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Removes the specified genre from the local list,
     * adjusts pagination if necessary, and refreshes the view.
     * <p>
     * After a genre is removed, this method checks if the current page needs to be adjusted based on
     * the number of genres remaining, and then updates the view accordingly.
     * </p>
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
