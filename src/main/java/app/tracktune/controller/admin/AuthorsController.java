package app.tracktune.controller.admin;

import app.tracktune.controller.Controller;
import app.tracktune.exceptions.AuthorAlreadyExixtsExeption;
import app.tracktune.exceptions.TrackTuneException;
import app.tracktune.model.author.Author;
import app.tracktune.model.author.AuthorDAO;
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

    @FXML
    private VBox authorsContainer;
    @FXML
    private Button prevButton;
    @FXML
    private Button nextButton;
    @FXML
    private TabPane filterTabPane;
    @FXML
    private TextField TxtName;

    private AuthorStatusEnum currentFilter = AuthorStatusEnum.ACTIVE;
    private final List<Author> authors = new ArrayList<>();
    private List<Author> filteredAuthors = new ArrayList<>();
    private int currentPage = 0;
    private final int itemsPerPage = 4;

    private final AuthorDAO authorDAO = new AuthorDAO();

    @FXML
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        authors.clear();
        authors.addAll(authorDAO.getAll());
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

    private void filterAuthors() {
        filteredAuthors = authors.stream()
                .filter(a -> a.getStatus() == currentFilter)
                .sorted(Comparator.comparing(Author::getAuthorshipName))
                .collect(Collectors.toList());
    }

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

    private HBox createAuthorItem(Author author) {
        Label infoLabel = new Label(author.getAuthorshipName());
        infoLabel.getStyleClass().add("author-item-title");

        Label nTrackLabel = new Label("Author");
        nTrackLabel.getStyleClass().add("author-item-date");

        VBox textBox = new VBox(5, infoLabel, nTrackLabel);
        textBox.setAlignment(Pos.CENTER_LEFT);

        Button restoreBtn = new Button(Strings.RESTORE);
        restoreBtn.getStyleClass().add("restore-button");
        restoreBtn.setOnAction(e -> restoreAuthor(author));

        Button removeBtn = new Button(Strings.DELETE);
        removeBtn.getStyleClass().add("delete-button");
        removeBtn.setOnAction(e -> removeAuthor(author));

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

    private void restoreAuthor(Author author) {
        boolean response = ViewManager.setAndGetConfirmAlert(Strings.CHANGE_PRIVILEGES, Strings.CHANGE_PRIVILEGES, Strings.ARE_YOU_SURE);
        if (response)
            try {
                Author updatedAuthor = new Author(author.getAuthorshipName(), AuthorStatusEnum.ACTIVE);
                authorDAO.updateById(updatedAuthor, author.getId());
                int index = authors.indexOf(author);
                if (index >= 0) authors.set(index, updatedAuthor);
                adjustPageAfterUpdate();
            } catch (Exception ex) {
                ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
                System.err.println(ex.getMessage());
            }
    }

    private void removeAuthor(Author author) {
        boolean response = ViewManager.setAndGetConfirmAlert(Strings.CHANGE_PRIVILEGES, Strings.CHANGE_PRIVILEGES, Strings.ARE_YOU_SURE);
        if (response)
            try {
                Author updatedAuthor = new Author(author.getAuthorshipName(), AuthorStatusEnum.REMOVED);
                authorDAO.updateById(updatedAuthor, author.getId());
                int index = authors.indexOf(author);
                if (index >= 0) authors.set(index, updatedAuthor);
                adjustPageAfterUpdate();
            } catch (Exception ex) {
                ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
                System.err.println(ex.getMessage());
            }
    }

    private void adjustPageAfterUpdate() {
        int maxPage = (int) Math.ceil((double) filteredAuthors.size() / itemsPerPage) - 1;
        if (currentPage > maxPage) {
            currentPage = Math.max(0, maxPage);
        }
        updateAuthors();
    }

    @FXML
    public void addAuthor() {
        String name = TxtName.getText().trim();

        try {
            if (!name.isEmpty()) {
                if (SQLiteScripts.checkForSQLInjection(name)) {
                    throw new TrackTuneException(Strings.ERR_SQL_INJECTION);
                }

                boolean exists = authors.stream()
                        .anyMatch(a -> a.getAuthorshipName().equalsIgnoreCase(name));

                if (!exists) {
                    Author newAuthor = new Author(name, AuthorStatusEnum.ACTIVE);
                    authorDAO.insert(newAuthor);
                    authors.clear();
                    authors.addAll(authorDAO.getAll());
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
        } catch (Exception e) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
            System.err.println(e.getMessage());
        }
    }
}