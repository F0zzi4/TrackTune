package app.tracktune.controller.common;

import app.tracktune.controller.Controller;
import app.tracktune.controller.admin.AdminDashboardController;
import app.tracktune.controller.authenticatedUser.AuthenticatedUserDashboardController;
import app.tracktune.exceptions.SQLiteException;
import app.tracktune.utils.DatabaseManager;
import app.tracktune.model.author.Author;
import app.tracktune.model.author.AuthorDAO;
import app.tracktune.model.genre.Genre;
import app.tracktune.model.genre.GenreDAO;
import app.tracktune.model.musicalInstrument.MusicalInstrument;
import app.tracktune.model.musicalInstrument.MusicalInstrumentDAO;
import app.tracktune.model.resource.Resource;
import app.tracktune.model.track.*;
import app.tracktune.model.user.Administrator;
import app.tracktune.utils.Frames;
import app.tracktune.utils.Strings;
import app.tracktune.view.ViewManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.util.StringConverter;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JavaFX controller for managing the music tracks view in the admin panel.
 * <p>
 * Supports paginated display of tracks and dynamic filtering by author, genre,
 * instrument, and title. Also provides (future) actions for viewing and deleting tracks.
 * <p>
 * Interacts with {@link TrackDAO}, {@link TrackAuthorDAO}, {@link AuthorDAO},
 * {@link GenreDAO}, and {@link MusicalInstrumentDAO} to fetch related data.
 */
public class TracksController extends Controller implements Initializable {
    /**
     * The container that holds the visual elements for displaying individual track items.
     */
    @FXML private VBox tracksContainer;

    /**
     * Button to navigate to the previous page of tracks.
     */
    @FXML private Button btnPrev;

    /**
     * Button to navigate to the next page of tracks.
     */
    @FXML private Button btnNext;

    /**
     * ComboBox used to select the filtering type (e.g., by name, category, etc.) for the track list.
     */
    @FXML private ComboBox<String> filterTypeComboBox;

    /**
     * Container holding the UI controls related to filtering (e.g., text fields, ComboBoxes).
     */
    @FXML private HBox filterControlsContainer;

    /**
     * A list containing all available {@link Track} instances, usually loaded from the database.
     */
    private List<Track> allTracks = new ArrayList<>();

    /**
     * A list of {@link Track} instances that match the current filter criteria.
     */
    private List<Track> filteredTracks = new ArrayList<>();

    /**
     * Index of the current page based on the pagination logic
     */
    private int currentPage = 0;
    // CONSTANTS
    private static final int ITEMS_PER_PAGE = 6;


    protected Resource resource;

    /** Available filter types and their associated Material Design Icons */
    private static final Map<String, String> FILTER_OPTIONS = Map.of(
            "All", "mdi2f-format-list-bulleted",
            "Author", "mdi2a-account-music",
            "Genre", "mdi2m-music-note",
            "Instrument", "mdi2g-guitar-electric",
            "Title", "mdi2f-format-title"
    );

    /**
     * Called automatically after FXML loading is complete.
     * Initializes the controller by loading tracks, setting up filters, and pagination.
     *
     * @param location  the location used to resolve relative paths for the root object, or null if not known
     * @param resources the resources used to localize the root object, or null if not localized
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        allTracks = DatabaseManager.getDAOProvider().getTrackDAO().getAll();
        filteredTracks = new ArrayList<>(allTracks);
        setupFilterComboBox();
        setupPaginationButtons();
        updateTracks();
    }

    /**
     * Initializes the filter ComboBox with filter options and custom icons.
     */
    private void setupFilterComboBox() {
        ObservableList<String> filterTypes = FXCollections.observableArrayList(FILTER_OPTIONS.keySet());
        filterTypeComboBox.setItems(filterTypes);
        filterTypeComboBox.setValue("All");

        filterTypeComboBox.setCellFactory(_ -> createFilterListCell());
        filterTypeComboBox.setButtonCell(createFilterListCell());

        filterTypeComboBox.setOnAction(_ -> {
            filteredTracks = new ArrayList<>(allTracks);
            applyFilter(filterTypeComboBox.getValue());
        });
    }

    /**
     * Creates a custom ListCell with an icon and label for each filter option.
     *
     * @return a customized ListCell
     */
    private ListCell<String> createFilterListCell() {
        return new ListCell<>() {
            private final FontIcon icon = new FontIcon();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    icon.setIconLiteral(FILTER_OPTIONS.getOrDefault(item, "mdi2f-filter"));
                    icon.setIconSize(16);
                    setText(item);
                    setGraphic(icon);
                }
            }
        };
    }

    /**
     * Applies the selected filter type and updates the filtering controls accordingly.
     *
     * @param filter the selected filter type
     */
    private void applyFilter(String filter) {
        currentPage = 0;
        filterControlsContainer.getChildren().remove(1, filterControlsContainer.getChildren().size());

        switch (filter) {
            case "Author" -> setupEntityFilter(DatabaseManager.getDAOProvider().getAuthorDAO().getAll());
            case "Genre" -> setupEntityFilter(DatabaseManager.getDAOProvider().getGenreDAO().getAll());
            case "Instrument" -> setupEntityFilter(DatabaseManager.getDAOProvider().getMusicalInstrumentDAO().getAll());
            case "Title" -> setupTitleFilter();
            default -> filteredTracks = new ArrayList<>(allTracks);
        }

        updateTracks();
    }

    /**
     * Sets up the filter control for filtering by track title.
     */
    private void setupTitleFilter() {
        TextField titleField = new TextField();
        titleField.setPromptText("Enter title...");
        titleField.getStyleClass().add("textField");

        titleField.setOnKeyReleased(_ -> {
            String input = titleField.getText().toLowerCase();
            filteredTracks = allTracks.stream()
                    .filter(t -> t.getTitle().toLowerCase().contains(input))
                    .collect(Collectors.toList());
            updateTracks();
        });

        filterControlsContainer.getChildren().add(titleField);
    }

    /**
     * Sets up the filter control for entity-based filtering (Author, Genre, Instrument).
     *
     * @param items the list of available entities to populate the filter ComboBox
     * @param <T>   the type of the entity
     */
    private <T> void setupEntityFilter(List<T> items) {
        ComboBox<T> comboBox = new ComboBox<>(FXCollections.observableArrayList(items));
        comboBox.setEditable(true);
        comboBox.setConverter(new EntityToStringConverter<>());

        setupSearchableComboBox(comboBox);
        setupComboBoxAction(comboBox);

        filterControlsContainer.getChildren().add(comboBox);
    }

    /**
     * Makes a ComboBox searchable by dynamically filtering its items based on user input.
     *
     * @param comboBox the ComboBox to make searchable
     * @param <T>      the type of elements in the ComboBox
     */
    private <T> void setupSearchableComboBox(ComboBox<T> comboBox) {
        comboBox.getEditor().addEventFilter(KeyEvent.KEY_RELEASED, _ -> {
            String input = comboBox.getEditor().getText().toLowerCase();
            ObservableList<T> items = comboBox.getItems().filtered(item ->
                    item.toString().toLowerCase().contains(input));
            comboBox.setItems(FXCollections.observableArrayList(items));
            comboBox.show();
        });
    }

    /**
     * Sets up the action to apply when an entity is selected in the ComboBox.
     *
     * @param comboBox the ComboBox to monitor
     * @param <T>      the type of the selected entity
     */
    private <T> void setupComboBoxAction(ComboBox<T> comboBox) {
        comboBox.setOnAction(_ -> {
            T selected = comboBox.getValue();
            if (selected instanceof Author author) {
                filteredTracks = DatabaseManager.getDAOProvider().getTrackDAO().getAllByAuthorId(author.getId());
            } else if (selected instanceof Genre genre) {
                filteredTracks = DatabaseManager.getDAOProvider().getTrackDAO().getAllByGenreId(genre.getId());
            } else if (selected instanceof MusicalInstrument instrument) {
                filteredTracks = DatabaseManager.getDAOProvider().getTrackDAO().getAllByInstrumentId(instrument.getId());
            }
            updateTracks();
        });
    }

    /**
     * Configures the event handlers for the "Previous" and "Next" pagination buttons.
     */
    private void setupPaginationButtons() {
        btnPrev.setOnAction(_ -> {
            if (currentPage > 0) {
                currentPage--;
                updateTracks();
            }
        });

        btnNext.setOnAction(_ -> {
            if ((currentPage + 1) * ITEMS_PER_PAGE < filteredTracks.size()) {
                currentPage++;
                updateTracks();
            }
        });
    }

    /**
     * Updates the displayed list of tracks based on the current page and applied filters.
     */
    private void updateTracks() {
        tracksContainer.getChildren().clear();

        int start = currentPage * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, filteredTracks.size());

        btnPrev.setDisable(currentPage == 0);
        btnNext.setDisable(end >= filteredTracks.size());

        if (filteredTracks.isEmpty()) {
            Label emptyLabel = new Label(Strings.EMPTY_LIST);
            emptyLabel.getStyleClass().add("empty-list-label");
            HBox emptyBox = new HBox(emptyLabel);
            emptyBox.setAlignment(Pos.CENTER);
            tracksContainer.getChildren().add(emptyBox);
        } else {
            for (Track track : filteredTracks.subList(start, end)) {
                tracksContainer.getChildren().add(createTrackItemBox(track));
            }
        }
    }

    /**
     * Creates a visual HBox component for a single track, showing basic information and action buttons.
     *
     * @param track the track to display
     * @return the HBox representing the track item
     */
    private HBox createTrackItemBox(Track track) {
        List<TrackAuthor> authors = DatabaseManager.getDAOProvider().getTrackAuthorDAO().getByTrackId(track.getId());
        List<TrackGenre> genres = DatabaseManager.getDAOProvider().getTrackGenreDAO().getByTrackId(track.getId());
        List<TrackInstrument> instruments = DatabaseManager.getDAOProvider().getTrackInstrumentDAO().getByTrackId(track.getId());

        Label titleLabel = new Label(track.getTitle());
        titleLabel.getStyleClass().add("request-item-title");

        String authorNames = authors.stream()
                .map(ta -> DatabaseManager.getDAOProvider().getAuthorDAO().getById(ta.getAuthorId()).getAuthorshipName())
                .collect(Collectors.joining(", "));

        Label authorsLabel = new Label("Authors: " + authorNames);
        authorsLabel.getStyleClass().add("request-item-authors");

        String genreNames = genres.stream()
                .map(tg -> DatabaseManager.getDAOProvider().getGenreDAO().getById(tg.getGenreId()).getName())
                .collect(Collectors.joining(", "));
        Label genresLabel = new Label("Genres: " + genreNames);
        genresLabel.getStyleClass().add("request-item-authors");

        Label instrumentsLabel = null;
        if(!instruments.isEmpty()){
            String instrumentsNames = instruments.stream()
                    .map(ti -> DatabaseManager.getDAOProvider().getMusicalInstrumentDAO().getById(ti.getInstrumentId()).getName())
                    .collect(Collectors.joining(", "));
            instrumentsLabel = new Label("Instruments: " + instrumentsNames);
            instrumentsLabel.getStyleClass().add("request-item-authors");
        }

        Label dateLabel = new Label(getFormattedRequestDate(track.getCreationDate()));
        dateLabel.getStyleClass().add("request-item-description");
        dateLabel.setWrapText(true);

        VBox textBox = new VBox(5, new HBox(10, titleLabel, authorsLabel, genresLabel, instrumentsLabel), dateLabel);
        textBox.setAlignment(Pos.CENTER_LEFT);
        textBox.setStyle("-fx-padding: 0 0 0 10;");

        Button viewBtn = new Button(Strings.LINKED_RESOURCES);
        viewBtn.getStyleClass().add("view-button");
        viewBtn.setOnAction(_ -> viewTrackResources(track));
        HBox buttonBox;
        if(ViewManager.getSessionUser() instanceof Administrator || track.getUserID() == ViewManager.getSessionUser().getId()) {
            Button deleteBtn = new Button(Strings.DELETE);
            deleteBtn.getStyleClass().add("reject-button");
            deleteBtn.setOnAction(_ -> deleteTrack(track));

            buttonBox = new HBox(10, viewBtn, deleteBtn);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
        }else{
            buttonBox = new HBox(10, viewBtn);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox box = new HBox(10, textBox, spacer, buttonBox);
        box.getStyleClass().add("request-item");
        box.setAlignment(Pos.CENTER_LEFT);
        box.setMaxWidth(Double.MAX_VALUE);

        return box;
    }

    /**
     * Loads and displays the resource view associated with the given {@link Track}.
     * <p>
     * This method dynamically loads the resource view FXML file and sets up a new
     * {@link TrackResourcesController} using the provided track. It checks whether the
     * parent controller is an instance of {@link AdminDashboardController} or
     * {@link AuthenticatedUserDashboardController} to insert the view into the appropriate
     * main content container.
     * <p>
     * If an error occurs during loading, an error alert is shown and the exception message
     * is printed to the console.
     *
     * @param track the {@link Track} whose associated resources should be displayed
     */
    private void viewTrackResources(Track track) {
        try{
            if(parentController instanceof AdminDashboardController adminController){
                FXMLLoader loader = new FXMLLoader(this.getClass().getResource(Frames.RESOURCES_COMMON_VIEW_PATH));
                loader.setControllerFactory(_ -> new TrackResourcesController(track));
                Parent view = loader.load();

                Controller controller = loader.getController();
                controller.setParentController(parentController);

                adminController.mainContent.getChildren().setAll(view);
            }else if(parentController instanceof AuthenticatedUserDashboardController authController){
                FXMLLoader loader = new FXMLLoader(this.getClass().getResource(Frames.RESOURCES_COMMON_VIEW_PATH));
                loader.setControllerFactory(_ -> new TrackResourcesController(track));
                Parent view = loader.load();

                Controller controller = loader.getController();
                controller.setParentController(parentController);

                authController.mainContent.getChildren().setAll(view);
            }
        }catch(Exception e){
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
            System.err.println(e.getMessage());
        }
    }

    /**
     * Deletes the specified {@link Track} from the database and the UI lists, after user confirmation.
     * <p>
     * This method first prompts the user with a confirmation alert. If the user confirms,
     * it attempts to delete the track from the database using its ID. If the deletion is successful,
     * the track is also removed from the `allTracks` and `filteredTracks` lists, and the
     * track list is updated in the UI. If an error occurs during the deletion process,
     * an error alert is shown and the exception is logged to the console.
     *
     * @param track the {@link Track} object to be deleted
     */
    private void deleteTrack(Track track) {
        boolean response = ViewManager.setAndGetConfirmAlert(Strings.CONFIRM_DELETION, Strings.CONFIRM_DELETION, Strings.ARE_YOU_SURE);
        if(response){
            try{
                DatabaseManager.getDAOProvider().getTrackDAO().deleteById(track.getId());
            }catch (SQLiteException ex) {
                ViewManager.setAndShowAlert(Strings.ERROR, Strings.DELETE, Strings.ERR_DELETE_TRACK, Alert.AlertType.ERROR);
                System.err.println(ex.getMessage());
            }

            allTracks.remove(track);
            filteredTracks.remove(track);
            updateTracks();
        }
    }

    /**
     * A simple converter to display entities in ComboBoxes using their toString() representation.
     *
     * @param <T> the type of entity
     */
    private static class EntityToStringConverter<T> extends StringConverter<T> {
        @Override
        public String toString(T object) {
            return (object == null) ? "" : object.toString();
        }

        @Override
        public T fromString(String string) {
            return null;
        }
    }
}