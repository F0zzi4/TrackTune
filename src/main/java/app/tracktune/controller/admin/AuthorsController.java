package app.tracktune.controller.admin;

import app.tracktune.controller.Controller;
import app.tracktune.exceptions.AuthorAlreadyExistsException;
import app.tracktune.exceptions.TrackTuneException;
import app.tracktune.utils.DatabaseManager;
import app.tracktune.model.author.Author;
import app.tracktune.model.author.AuthorStatusEnum;
import app.tracktune.utils.SQLiteScripts;
import app.tracktune.utils.Strings;
import app.tracktune.view.ViewManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class AuthorsController extends Controller implements Initializable {
    /**
     * Container that holds the list of author items currently displayed in the UI.
     */
    @FXML
    private VBox authorsContainer;

    /**
     * Button to navigate to the previous page in the paginated author list.
     */
    @FXML
    private Button prevButton;

    /**
     * Button to navigate to the next page in the paginated author list.
     */
    @FXML
    private Button nextButton;

    /**
     * TabPane used to filter authors based on their {@link AuthorStatusEnum} (e.g., ACTIVE, REMOVED).
     */
    @FXML
    private TabPane filterTabPane;

    /**
     * TextField for entering the name of a new author to be added.
     */
    @FXML
    private TextField TxtName;

    /**
     * Currently selected author status filter.
     */
    private AuthorStatusEnum currentFilter = AuthorStatusEnum.ACTIVE;

    /**
     * Full list of authors retrieved from the database.
     */
    private final List<Author> authors = new ArrayList<>();

    /**
     * Filtered list of authors, based on the selected {@code currentFilter}.
     */
    private List<Author> filteredAuthors = new ArrayList<>();

    /**
     * The current page index used for paginating the author list.
     */
    private int currentPage = 0;

    /**
     * Number of authors displayed per page.
     */
    private final int itemsPerPage = 5;

    /**
     * Initializes the controller after its root element has been completely processed.
     * <p>
     * Loads all authors from the database, sets up the filter tabs,
     * configures the pagination buttons (previous and next) with their respective actions,
     * and updates the authors list for the first time.
     *
     * @param location  The location used to resolve relative paths for the root object, or null if unknown.
     * @param resources The resources used to localize the root object, or null if not localized.
     */
    @FXML
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        createTabsFromEnum();

        prevButton.setOnAction(_ -> {
            if (currentPage > 0) {
                currentPage--;
                updateAuthors();
            }
        });

        nextButton.setOnAction(_ -> {
            if ((currentPage + 1) * itemsPerPage < filteredAuthors.size()) {
                currentPage++;
                updateAuthors();
            }
        });

        updateAuthors();
    }

    /**
     * Initializes the filter tabs in the UI based on the {@link AuthorStatusEnum} values.
     * <p>
     * Each enum value creates a corresponding tab, which sets the current filter when selected.
     * When a tab is selected, the authors list is refreshed to show only authors matching the selected status,
     * and the pagination resets to the first page.
     * The first tab is selected by default.
     */
    private void createTabsFromEnum() {

        filterTabPane.getTabs().clear();

        for (AuthorStatusEnum status : AuthorStatusEnum.values()) {
            Tab tab = new Tab(status.toString());
            tab.setUserData(status);
            filterTabPane.getTabs().add(tab);
        }

        filterTabPane.getSelectionModel().selectedItemProperty().addListener((_, _, newTab) -> {
            if (newTab != null) {
                currentFilter = (AuthorStatusEnum) newTab.getUserData();
                currentPage = 0;
                updateAuthors();
            }
        });

        filterTabPane.getSelectionModel().selectFirst();
    }

    /**
     * Filters the complete list of authors based on the current status filter and sorts them alphabetically.
     * <p>
     * The filtered and sorted list is stored in {@code filteredAuthors}.
     * Only authors whose status matches {@code currentFilter} are included.
     */
    private void filterAuthors() {
        filteredAuthors = authors.stream()
                .filter(a -> a.getStatus() == currentFilter)
                .sorted(Comparator.comparing(Author::getAuthorshipName))
                .collect(Collectors.toList());
    }

    /**
     * Updates the list of authors displayed in the UI based on the current filter and pagination.
     * <p>
     * This method filters the authors according to the selected status, clears the current display,
     * and populates it with the authors corresponding to the current page.
     * It also updates the state of the pagination buttons (previous and next).
     * If no authors match the filter, an appropriate "empty list" message is shown.
     */
    private void updateAuthors() {
        authors.clear();
        authors.addAll(DatabaseManager.getDAOProvider().getAuthorDAO().getAll());
        filterAuthors();
        authorsContainer.getChildren().clear();

        int totalAuthors = filteredAuthors.size();
        int start = currentPage * itemsPerPage;
        int end = Math.min(start + itemsPerPage, totalAuthors);

        prevButton.setDisable(currentPage == 0);
        nextButton.setDisable(end >= totalAuthors);

        if (filteredAuthors.isEmpty()) {
            Label emptyLabel = new Label(Strings.EMPTY_LIST);
            emptyLabel.getStyleClass().add("empty-list-label");

            HBox emptyBox = new HBox(emptyLabel);
            emptyBox.setAlignment(Pos.CENTER);
            authorsContainer.getChildren().add(emptyBox);
        } else {
            List<Author> pageItems = filteredAuthors.subList(start, end);

            for (Author author : pageItems) {
                authorsContainer.getChildren().add(createAuthorItem(author));
            }
        }
    }

    /**
     * Creates a UI component representing a single author item for display in the authors list.
     * <p>
     * The component includes the author's name, a fixed label "Author", and buttons to either
     * remove or restore the author depending on their current status.
     * <ul>
     *     <li>If the author is active, a "Delete" button is shown.</li>
     *     <li>If the author is removed, a "Restore" button is shown.</li>
     * </ul>
     * The buttons are linked to their respective handlers for removal or restoration.
     *
     * @param author The {@link Author} to create the UI item for.
     * @return An {@link HBox} containing the formatted author item UI.
     */
    private HBox createAuthorItem(Author author) {
        Label infoLabel = new Label(author.getAuthorshipName());
        infoLabel.getStyleClass().add("author-item-title");

        Label nTrackLabel = new Label("Author");
        nTrackLabel.getStyleClass().add("author-item-date");

        VBox textBox = new VBox(5, infoLabel, nTrackLabel);
        textBox.setAlignment(Pos.CENTER_LEFT);

        Button restoreBtn = new Button(Strings.RESTORE);
        restoreBtn.getStyleClass().add("accept-button");
        restoreBtn.setOnAction(_ -> restoreAuthor(author));

        Button removeBtn = new Button(Strings.DELETE);
        removeBtn.getStyleClass().add("delete-button");
        removeBtn.setOnAction(_ -> removeAuthor(author));

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        switch (author.getStatus()) {
            case ACTIVE -> buttonBox.getChildren().add(removeBtn);
            case REMOVED -> buttonBox.getChildren().add(restoreBtn);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox box = new HBox(10, textBox, spacer, buttonBox);
        box.getStyleClass().add("author-item");
        box.setAlignment(Pos.CENTER_LEFT);

        return box;
    }

    /**
     * Restores a previously removed author by updating their status to {@code ACTIVE}.
     * <p>
     * A confirmation dialog is shown to the user before making any changes.
     * If confirmed, the author's status is updated in the database, the local list is updated,
     * and the UI is refreshed to reflect the changes.
     *
     * @param author The {@link Author} to be restored.
     */
    private void restoreAuthor(Author author) {
        boolean response = ViewManager.setAndGetConfirmAlert(Strings.CHANGE_PRIVILEGES, Strings.CHANGE_PRIVILEGES, Strings.ARE_YOU_SURE);
        if (response)
            try {
                Author updatedAuthor = new Author(author.getAuthorshipName(), AuthorStatusEnum.ACTIVE);
                DatabaseManager.getDAOProvider().getAuthorDAO().updateById(updatedAuthor, author.getId());
                int index = authors.indexOf(author);
                if (index >= 0) authors.set(index, updatedAuthor);
                adjustPageAfterUpdate();
            } catch (Exception ex) {
                ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
                System.err.println(ex.getMessage());
            }
    }

    /**
     * Marks the given author as removed by updating their status to {@code REMOVED}.
     * <p>
     * A confirmation alert is shown to the user before proceeding.
     * If confirmed, the author is updated in the database, the local list is updated accordingly,
     * and the UI is refreshed.
     *
     * @param author The {@link Author} to be marked as removed.
     */
    private void removeAuthor(Author author) {
        boolean response = ViewManager.setAndGetConfirmAlert(Strings.CHANGE_PRIVILEGES, Strings.CHANGE_PRIVILEGES, Strings.ARE_YOU_SURE);
        if (response)
            try {
                Author updatedAuthor = new Author(author.getAuthorshipName(), AuthorStatusEnum.REMOVED);
                DatabaseManager.getDAOProvider().getAuthorDAO().updateById(updatedAuthor, author.getId());
                int index = authors.indexOf(author);
                if (index >= 0) authors.set(index, updatedAuthor);
                adjustPageAfterUpdate();
            } catch (Exception ex) {
                ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
                System.err.println(ex.getMessage());
            }
    }

    /**
     * Ensures that the current page index is within valid bounds after updating the author list.
     * <p>
     * This method recalculates the maximum number of pages and adjusts {@code currentPage} if it exceeds the limit.
     * Then it triggers an update of the authors displayed in the UI.
     */
    private void adjustPageAfterUpdate() {
        int maxPage = (int) Math.ceil((double) filteredAuthors.size() / itemsPerPage) - 1;
        if (currentPage > maxPage) {
            currentPage = Math.max(0, maxPage);
        }
        updateAuthors();
    }

    /**
     * Handles the addition of a new author based on the name entered the input field.
     * <p>
     * The method performs several validations:
     * <ul>
     *   <li>Ensures the input is not empty.</li>
     *   <li>Checks for potential SQL injection patterns.</li>
     *   <li>Verifies that an author with the same name does not already exist (case-insensitive).</li>
     * </ul>
     * If all checks pass, the new author is created with an {@code ACTIVE} status, inserted into the database,
     * and the author list is refreshed in the UI. Any validation or database error results in an alert being shown.
     *
     * @throws TrackTuneException if the input is invalid or an author already exists.
     */
    @FXML
    public void addAuthor() throws TrackTuneException {
        String name = Controller.toTitleCase(TxtName.getText().trim());

        try {
            if (!name.isEmpty()) {
                if (SQLiteScripts.checkForSQLInjection(name)) {
                    throw new TrackTuneException(Strings.ERR_SQL_INJECTION);
                }

                boolean exists = authors.stream()
                        .anyMatch(a -> a.getAuthorshipName().equalsIgnoreCase(name));

                if (!exists) {
                    Author newAuthor = new Author(name, AuthorStatusEnum.ACTIVE);
                    DatabaseManager.getDAOProvider().getAuthorDAO().insert(newAuthor);
                    authors.clear();
                    authors.addAll(DatabaseManager.getDAOProvider().getAuthorDAO().getAll());
                    TxtName.clear();
                    updateAuthors();
                } else {
                    throw new AuthorAlreadyExistsException(Strings.ERR_AUTHOR_ALREADY_EXISTS);
                }
            } else {
                throw new TrackTuneException(Strings.FIELD_EMPTY);
            }
        } catch (TrackTuneException exception) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.AUTHOR_FAILED, exception.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
            System.err.println(e.getMessage());
        }
    }
}