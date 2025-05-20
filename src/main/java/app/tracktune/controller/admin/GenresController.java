package app.tracktune.controller.admin;

import app.tracktune.controller.Controller;
import app.tracktune.exceptions.SQLInjectionException;
import app.tracktune.exceptions.TrackTuneException;
import app.tracktune.model.genre.Genre;
import app.tracktune.model.genre.GenreDAO;
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
    @FXML private VBox genresContainer;
    @FXML private Button btnPrev;
    @FXML private Button btnNext;
    @FXML private Button btnAddGenre;
    @FXML private TextField txtName;
    @FXML private TextArea txtDescription;
    @FXML private Label lblCharCount;

    private List<Genre> genres = new ArrayList<>();
    private int currentPage = 0;
    private final int itemsPerPage = 4;
    private final GenreDAO genreDAO = new GenreDAO();

    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        genres = genreDAO.getAll();

        btnPrev.setOnAction(e -> {
            if (currentPage > 0) {
                currentPage--;
                updateRequests();
            }
        });

        btnNext.setOnAction(e -> {
            if ((currentPage + 1) * itemsPerPage < genres.size()) {
                currentPage++;
                updateRequests();
            }
        });

        btnAddGenre.setOnAction(e -> {
            try {
                String name = txtName.getText().trim();
                String description = txtDescription.getText().trim();

                if (!name.isEmpty() && !description.isEmpty()) {
                    if (SQLiteScripts.checkForSQLInjection(name, description))
                        throw new SQLInjectionException(Strings.ERR_SQL_INJECTION);

                    boolean exists = genres.stream()
                            .anyMatch(g -> g.getName().equalsIgnoreCase(name));

                    if (!exists) {
                        Genre newGenre = new Genre(name, description);
                        genreDAO.insert(newGenre);
                        genres = genreDAO.getAll();
                        txtName.clear();
                        txtDescription.clear();
                        lblCharCount.setText("0/300");
                        currentPage = 0;
                        updateRequests();
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

        txtDescription.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 300) {
                txtDescription.setText(oldVal);
            } else {
                lblCharCount.setText(newVal.length() + "/300");
            }
        });

        updateRequests();
    }

    private void updateRequests() {
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

    private void deleteGenre(Genre genre) {
        boolean response = ViewManager.setAndGetConfirmAlert(Strings.CONFIRM_DELETION, Strings.CONFIRM_DELETION, Strings.ARE_YOU_SURE);
        if (response)
            try {
                genreDAO.deleteById(genre.getId());
                genres.remove(genre);
                int maxPage = (int) Math.ceil((double) genres.size() / itemsPerPage);
                if (currentPage >= maxPage && currentPage > 0) {
                    currentPage--;
                }
                updateRequests();
            } catch (Exception ex) {
                ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERR_GENERAL, ex.getMessage(), Alert.AlertType.ERROR);
            }
    }
}