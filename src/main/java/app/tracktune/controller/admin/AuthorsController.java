package app.tracktune.controller.admin;

import app.tracktune.exceptions.AuthorAlreadyExixtsExeption;
import app.tracktune.exceptions.TrackTuneException;
import app.tracktune.model.author.Author;
import app.tracktune.model.author.AuthorDAO;
import app.tracktune.model.author.AuthorStatusEnum;
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

import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller class for managing authors in the admin panel.
 * This class allows the admin to view, add, activate, and remove authors,
 * as well as apply filters to view active or removed authors, with pagination support.
 */
public class AuthorsController {
    @FXML private VBox authorsContainer;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private TabPane filterTabPane;
    @FXML private TextField TxtName;

    private AuthorStatusEnum currentFilter = AuthorStatusEnum.ACTIVE;
    private final SortedSet<Author> author = new TreeSet<>();
    private List<Author> filteredAuthors = new ArrayList<>();
    private int currentPage = 0;
    private final int itemsPerPage = 4;
    private final AuthorDAO authorDAO = new AuthorDAO();

    /**
     * Initializes the controller by loading all authors from the database,
     * setting up the filter tabs, and configuring pagination.
     */
    @FXML
    public void initialize() {
        author.addAll(authorDAO.getAll());

        createTabsFromEnum();

        prevButton.setOnAction(e -> {
            if (currentPage > 0) {
                currentPage--;
                updateAuthors();
            }
        });

        nextButton.setOnAction(e -> {
            if ((currentPage + 1) * itemsPerPage < filteredAuthors.size()) {
                currentPage++;
                updateAuthors();
            }
        });

        updateAuthors();
    }

    /**
     * Creates tabs for filtering authors by their status (ACTIVE, REMOVED).
     * Each tab is linked to a specific filter that updates the displayed authors.
     */
    private void createTabsFromEnum() {
        filterTabPane.getTabs().clear();

        for (AuthorStatusEnum status : AuthorStatusEnum.values()) {
            Tab tab = new Tab(status.toString());
            tab.setUserData(status);
            filterTabPane.getTabs().add(tab);
        }

        filterTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null) {
                currentFilter = (AuthorStatusEnum) newTab.getUserData();
                currentPage = 0;
                updateAuthors();
            }
        });

        filterTabPane.getSelectionModel().selectFirst();
    }

    /**
     * Filters the authors based on the current selected status filter.
     */
    private void filterAuthors() {
        filteredAuthors = author.stream()
                .filter(r -> r.getStatus() == currentFilter)
                .collect(Collectors.toList());
    }

    /**
     * Updates the authors list displayed on the UI, considering the current page and filter applied.
     * Also updates the state of the pagination buttons (prev/next).
     */
    private void updateAuthors() {
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
     * Creates a UI element (HBox) to display a single author item, including their name,
     * the number of tracks, and buttons to restore or remove them based on their current status.
     *
     * @param author the {@link Author} object to display
     * @return an {@link HBox} containing the author details and action buttons
     */
    private HBox createAuthorItem(Author author) {
        Label infoLabel = new Label(author.getAuthorshipName());
        infoLabel.getStyleClass().add("author-item-title");

        Label nTrackLabel = new Label("N tracks");
        nTrackLabel.getStyleClass().add("author-item-date");

        VBox textBox = new VBox(5, infoLabel, nTrackLabel);
        textBox.setAlignment(Pos.CENTER_LEFT);

        Button acceptBtn = new Button(Strings.RESTORE);
        acceptBtn.getStyleClass().add("accept-button");
        acceptBtn.setOnAction(e -> addAuthor(author));

        Button rejectBtn = new Button(Strings.DELETE);
        rejectBtn.getStyleClass().add("delete-button");
        rejectBtn.setOnAction(e -> removeAuthor(author));

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        switch (author.getStatus()) {
            case ACTIVE -> buttonBox.getChildren().addAll(rejectBtn);
            case REMOVED -> buttonBox.getChildren().add(acceptBtn);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox box = new HBox(10, textBox, spacer, buttonBox);
        box.getStyleClass().add("author-item");
        box.setAlignment(Pos.CENTER_LEFT);

        return box;
    }

    /**
     * Activates an author, changing their status to ACTIVE and updating the database.
     *
     * @param author the {@link Author} to activate
     */
    private void addAuthor(Author author) {
        try {
            authorDAO.update(new Author(author.getAuthorshipName(), AuthorStatusEnum.ACTIVE));
            removeAuthorAndUpdate(author);
        } catch (Exception ex) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
            ex.printStackTrace();
        }
    }

    /**
     * Removes an author, changing their status to REMOVED and updating the database.
     *
     * @param author the {@link Author} to remove
     */
    private void removeAuthor(Author author) {
        try {
            authorDAO.update(new Author(author.getAuthorshipName(), AuthorStatusEnum.REMOVED));
            removeAuthorAndUpdate(author);
        } catch (Exception ex) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
            ex.printStackTrace();
        }
    }

    /**
     * Removes an author from the list and updates the view, adjusting pagination if necessary.
     *
     * @param author the {@link Author} to remove
     */
    private void removeAuthorAndUpdate(Author author) {
        int maxPage = (int) Math.ceil((double) filteredAuthors.size() / itemsPerPage);
        if (currentPage >= maxPage && currentPage > 0) {
            currentPage--;
        }
        updateAuthors();
    }

    /**
     * Adds a new author to the database and updates the displayed list of authors.
     * Throws an exception if the author's name is empty or already exists.
     */
    @FXML
    public void addAuthor() {
        String name = TxtName.getText().trim();

        try {
            if (!name.isEmpty()) {
                if (SQLiteScripts.checkForSQLInjection(name)) {
                    throw new TrackTuneException(Strings.ERR_SQL_INJECTION);
                }

                if (author.stream().noneMatch(a -> a.getAuthorshipName().equalsIgnoreCase(name))) {
                    Author newAuthor = new Author(name);
                    authorDAO.insert(newAuthor);
                    author.add(newAuthor);
                    TxtName.clear();
                    updateAuthors();
                } else {
                    throw new AuthorAlreadyExixtsExeption(Strings.ERR_AUTHOR_ALREADY_EXISTS);
                }
            } else {
                throw new TrackTuneException(Strings.FIELD_EMPTY);
            }
        } catch (TrackTuneException exception) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.AUTHOR_FAILED, exception.getMessage(), Alert.AlertType.ERROR);
        }
    }
}

