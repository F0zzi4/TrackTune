package app.tracktune.controller.admin;

import app.tracktune.controller.Controller;
import app.tracktune.exceptions.SQLInjectionException;
import app.tracktune.exceptions.TrackTuneException;
import app.tracktune.utils.DatabaseManager;
import app.tracktune.model.genre.Genre;
import app.tracktune.utils.SQLiteScripts;
import app.tracktune.utils.Strings;
import app.tracktune.view.ViewManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.net.URL;
import java.util.*;

public class GenresController extends Controller implements Initializable {
    /** Container for displaying the list of genres in the UI. */
    @FXML
    private VBox genresContainer;

    /** Button to navigate to the previous page of genres. */
    @FXML
    private Button btnPrev;

    /** Button to navigate to the next page of genres. */
    @FXML
    private Button btnNext;

    /** Button to add a new genre. */
    @FXML
    private Button btnAddGenre;

    /** TextField for entering the name of a new or edited genre. */
    @FXML
    private TextField txtName;

    /** TextArea for entering the description of a new or edited genre. */
    @FXML
    private TextArea txtDescription;

    /** Label to display the character count for the genre description. */
    @FXML
    private Label lblCharCount;

    /** Complete list of genres loaded from the database or data source. */
    private List<Genre> genres = new ArrayList<>();

    /** Current page index in the paginated genres list (0-based). */
    private int currentPage = 0;

    /** Number of genre items to display per page. */
    private final int itemsPerPage = 4;

    /**
     * Initializes the genres management UI.
     * <p>
     * Loads all genres from the database, sets up pagination buttons,
     * configures the add-genre button with validation and insertion logic,
     * and adds a listener to update the character count label for the description input.
     * Also initializes the display of genres for the first page.
     *</p>
     * @param location  The location used to resolve relative paths for the root object, or null if unknown.
     * @param resources The resources used to localize the root object, or null if not localized.
     */
    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        genres = DatabaseManager.getDAOProvider().getGenreDAO().getAll();

        btnPrev.setOnAction(_ -> {
            if (currentPage > 0) {
                currentPage--;
                updateGenres();
            }
        });

        btnNext.setOnAction(_ -> {
            if ((currentPage + 1) * itemsPerPage < genres.size()) {
                currentPage++;
                updateGenres();
            }
        });

        btnAddGenre.setOnAction(_ -> {
            try {
                String name = txtName.getText().trim();
                String description = txtDescription.getText().trim();

                if (!name.isEmpty() && !description.isEmpty()) {
                    if (SQLiteScripts.checkForSQLInjection(name, description))
                        throw new SQLInjectionException(Strings.ERR_SQL_INJECTION);

                    boolean exists = genres.stream()
                            .anyMatch(g -> g.getName().equalsIgnoreCase(name));

                    if (!exists) {
                        Genre newGenre = new Genre(Controller.toTitleCase(name), description);
                        DatabaseManager.getDAOProvider().getGenreDAO().insert(newGenre);
                        genres = DatabaseManager.getDAOProvider().getGenreDAO().getAll();
                        txtName.clear();
                        txtDescription.clear();
                        lblCharCount.setText("0/300");
                        currentPage = 0;
                        updateGenres();
                    } else {
                        throw new TrackTuneException(Strings.ERR_GENRE_ALREADY_EXISTS);
                    }
                } else {
                    throw new TrackTuneException(Strings.FIELD_EMPTY);
                }
            } catch (TrackTuneException exception) {
                ViewManager.setAndShowAlert(Strings.ERROR, Strings.GENRE_FAILED, exception.getMessage(), Alert.AlertType.ERROR);
            } catch (Exception ex) {
                ViewManager.setAndShowAlert(Strings.ERROR, Strings.GENRE_FAILED, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
                System.err.println(ex.getMessage());
            }
        });

        txtDescription.textProperty().addListener((_, oldVal, newVal) -> {
            if (newVal.length() > 300) {
                txtDescription.setText(oldVal);
            } else {
                lblCharCount.setText(newVal.length() + "/300");
            }
        });

        updateGenres();
    }

    /**
     * Updates the genres view by clearing the container and displaying
     * the genres corresponding to the current page with pagination controls.
     * <p>
     * It disables the previous and next buttons appropriately based on the current page
     * and the total number of genres. If there are no genres, it shows a placeholder label.
     * <p>
     * The genres are displayed using the {@code createRequestItem(Genre)} method for each item.
     */
    private void updateGenres() {
        genresContainer.getChildren().clear();

        int total = genres.size();
        int start = currentPage * itemsPerPage;
        int end = Math.min(start + itemsPerPage, total);

        btnPrev.setDisable(currentPage == 0);
        btnNext.setDisable(end >= total);

        if (genres.isEmpty()) {
            Label emptyLabel = new Label(Strings.EMPTY_LIST);
            emptyLabel.getStyleClass().add("empty-list-label");

            HBox emptyBox = new HBox(emptyLabel);
            emptyBox.setAlignment(Pos.CENTER);
            genresContainer.getChildren().add(emptyBox);
        } else {
            List<Genre> pageItems = genres.subList(start, end);
            for (Genre genre : pageItems) {
                genresContainer.getChildren().add(createRequestItem(genre));
            }
        }
    }

    /**
     * Creates a visual HBox component representing a single genre item.
     * <p>
     * The component includes the genre's name and description, styled labels,
     * and a delete button to remove the genre.
     * The description label wraps text and its maximum width is dynamically bound
     * to the available space minus the delete button width.
     * </p>
     * @param genre The Genre object to be represented in the UI.
     * @return An HBox node containing the styled genre information and action button.
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
        deleteBtn.getStyleClass().add("delete-button");
        deleteBtn.setOnAction(_ -> deleteGenre(genre));
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
     * Deletes the specified genre after confirming with the user.
     *
     * <p>If the user confirms, the genre is removed from the database and the local list.
     * The current page is adjusted if necessary to ensure it remains within valid bounds,
     * and the UI is updated accordingly.</p>
     *
     * @param genre The Genre object to be deleted.
     */
    private void deleteGenre(Genre genre) {
        boolean response = ViewManager.setAndGetConfirmAlert(Strings.CONFIRM_DELETION, Strings.CONFIRM_DELETION, Strings.ARE_YOU_SURE);
        if (response)
            try {
                DatabaseManager.getDAOProvider().getGenreDAO().deleteById(genre.getId());
                genres.remove(genre);
                int maxPage = (int) Math.ceil((double) genres.size() / itemsPerPage);
                if (currentPage >= maxPage && currentPage > 0) {
                    currentPage--;
                }
                updateGenres();
            } catch (Exception ex) {
                ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERR_GENERAL, ex.getMessage(), Alert.AlertType.ERROR);
            }
    }
}