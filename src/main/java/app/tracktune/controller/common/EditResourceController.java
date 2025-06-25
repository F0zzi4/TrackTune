package app.tracktune.controller.common;

import app.tracktune.controller.Controller;
import app.tracktune.controller.admin.AdminDashboardController;
import app.tracktune.controller.authenticatedUser.AuthenticatedUserDashboardController;
import app.tracktune.exceptions.AuthorAlreadyExistsException;
import app.tracktune.exceptions.TrackTuneException;
import app.tracktune.utils.DatabaseManager;
import app.tracktune.model.author.Author;
import app.tracktune.model.author.AuthorStatusEnum;
import app.tracktune.model.genre.Genre;
import app.tracktune.model.musicalInstrument.MusicalInstrument;
import app.tracktune.model.resource.AudioVideoFileEnum;
import app.tracktune.model.resource.MultimediaResource;
import app.tracktune.model.resource.Resource;
import app.tracktune.model.resource.ResourceTypeEnum;
import app.tracktune.model.track.*;
import app.tracktune.utils.Frames;
import app.tracktune.utils.SQLiteScripts;
import app.tracktune.utils.Strings;
import app.tracktune.view.ViewManager;
import io.github.palexdev.materialfx.controls.MFXToggleButton;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

public class EditResourceController extends Controller implements Initializable {
    /** ComboBox for selecting an Author from the available list. */
    @FXML private ComboBox<Author> authorComboBox;

    /** FlowPane displaying the list of selected Authors as removable buttons. */
    @FXML private FlowPane selectedAuthorsPane;

    /** ComboBox for selecting a Genre from the available list. */
    @FXML private ComboBox<Genre> genreComboBox;

    /** FlowPane displaying the list of selected Genres as removable buttons. */
    @FXML private FlowPane selectedGenresPane;

    /** ComboBox for selecting a Musical Instrument from the available list. */
    @FXML private ComboBox<MusicalInstrument> instrumentComboBox;

    /** FlowPane displaying the list of selected Musical Instruments as removable buttons. */
    @FXML private FlowPane selectedInstrumentsPane;

    /** Toggle button indicating whether the resource is multimedia. */
    @FXML private MFXToggleButton btnIsMultimedia;

    /** Container HBox for resource location input controls, shown if multimedia is selected. */
    @FXML private HBox locationBox;

    /** TextField for entering the location information of a multimedia resource. */
    @FXML private TextField txtLocation;

    /** DatePicker for selecting the date associated with the resource. */
    @FXML private DatePicker resourceDate;

    /** Container HBox for resource date controls, shown if multimedia is selected. */
    @FXML private HBox resourceDateBox;

    /** TextField used as a ComboBox editor for entering or selecting the track title. */
    @FXML private TextField trackComboBox;

    /** Toggle button indicating whether the resource has an associated author. */
    @FXML private MFXToggleButton btnIsAuthor;

    /** The Resource instance being managed or edited by this controller. */
    private final Resource resource;

    /** Observable list containing all available Authors for selection. */
    private final ObservableList<Author> allAuthors = FXCollections.observableArrayList();

    /** Observable list containing all available Genres for selection. */
    private final ObservableList<Genre> allGenres = FXCollections.observableArrayList();

    /** Observable list containing all available Musical Instruments for selection. */
    private final ObservableList<MusicalInstrument> allMusicalInstruments = FXCollections.observableArrayList();

    /** Observable list of Authors currently selected by the user. */
    private final ObservableList<Author> selectedAuthors = FXCollections.observableArrayList();

    /** Observable list of Genres currently selected by the user. */
    private final ObservableList<Genre> selectedGenres = FXCollections.observableArrayList();

    /** Observable list of Musical Instruments currently selected by the user. */
    private final ObservableList<MusicalInstrument> selectedInstruments = FXCollections.observableArrayList();

    /**
     * Constructs an EditResourceController for the specified resource.
     *
     * @param resource the Resource instance to be edited
     */
    public EditResourceController(Resource resource) {this.resource = resource;}

    /**
     * Initializes the controller after its root element has been completely processed.
     * <p>
     * This method performs the following:
     * <ul>
     *   <li>Loads the given resource details into the UI.</li>
     *   <li>Shows or hides multimedia controls based on the resource type support.</li>
     *   <li>Fetches and populates lists of all Authors, Genres, and Musical Instruments.</li>
     *   <li>Sets up the ComboBoxes with appropriate converters and editable states.</li>
     *   <li>Attaches listeners to support dynamic search/filter and adding selected elements.</li>
     *   <li>Initializes listeners related to multimedia toggle button state changes.</li>
     * </ul>
     *
     * @param url the location used to resolve relative paths for the root object, or null if unknown
     * @param resourceBundle the resources used to localize the root object, or null if not localized
     */
    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            loadResource(resource);
            btnIsMultimedia.setVisible(AudioVideoFileEnum.isSupported(resource.getType().toString()));

            allAuthors.addAll(DatabaseManager.getDAOProvider().getAuthorDAO().getAll());
            allGenres.addAll(DatabaseManager.getDAOProvider().getGenreDAO().getAll());
            allMusicalInstruments.addAll(DatabaseManager.getDAOProvider().getMusicalInstrumentDAO().getAll());

            authorComboBox.setConverter(new EditResourceController.EntityToStringConverter<>());
            genreComboBox.setConverter(new EditResourceController.EntityToStringConverter<>());
            instrumentComboBox.setConverter(new EditResourceController.EntityToStringConverter<>());

            authorComboBox.setItems(allAuthors);
            authorComboBox.setEditable(true);
            genreComboBox.setItems(allGenres);
            genreComboBox.setEditable(true);
            instrumentComboBox.setItems(allMusicalInstruments);
            instrumentComboBox.setEditable(true);

            setDynamicResearchListener(authorComboBox, allAuthors);
            setDynamicResearchListener(genreComboBox, allGenres);
            setDynamicResearchListener(instrumentComboBox, allMusicalInstruments);

            setAddingElementListener(authorComboBox, selectedAuthorsPane, selectedAuthors);
            setAddingElementListener(genreComboBox, selectedGenresPane, selectedGenres);
            setAddingElementListener(instrumentComboBox, selectedInstrumentsPane, selectedInstruments);

            setIsMultimediaListener();
        } catch (Exception e) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERR_GENERAL, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
            System.err.println(e.getMessage());
        }
    }

    /**
     * Loads the given resource's details into the UI components.
     * <p>
     * This method performs the following:
     * <ul>
     *   <li>Retrieves and displays the associated track's title in the track input field.</li>
     *   <li>Fetches and populates the selected authors, genres, and musical instruments related to the track.</li>
     *   <li>Updates the corresponding UI panes to reflect the selected elements.</li>
     *   <li>If the resource is a multimedia resource, sets the multimedia toggle button,
     *       fills the location text field, and sets the resource date picker accordingly.</li>
     * </ul>
     *
     * @param resource the Resource instance whose details are loaded
     */
    private void loadResource(Resource resource){
        Track track = DatabaseManager.getDAOProvider().getTrackDAO().getById(resource.getTrackID());
        trackComboBox.setText(track.getTitle());

        List<TrackAuthor> trackAuthors = DatabaseManager.getDAOProvider().getTrackAuthorDAO().getByTrackId(resource.getTrackID());
        List<TrackGenre> trackGenres = DatabaseManager.getDAOProvider().getTrackGenreDAO().getByTrackId(resource.getTrackID());
        List<TrackInstrument> trackInstruments = DatabaseManager.getDAOProvider().getTrackInstrumentDAO().getByTrackId(resource.getTrackID());

        for(TrackAuthor trackAuthor : trackAuthors){
            selectedAuthors.add(DatabaseManager.getDAOProvider().getAuthorDAO().getById(trackAuthor.getAuthorId()));
        }
        updateSelectedElements(selectedAuthorsPane, selectedAuthors);

        for(TrackGenre trackGenre : trackGenres){
            selectedGenres.add(DatabaseManager.getDAOProvider().getGenreDAO().getById(trackGenre.getGenreId()));
        }
        updateSelectedElements(selectedGenresPane, selectedGenres);

        for(TrackInstrument trackInstrument : trackInstruments){
            selectedInstruments.add(DatabaseManager.getDAOProvider().getMusicalInstrumentDAO().getById(trackInstrument.getInstrumentId()));
        }
        updateSelectedElements(selectedInstrumentsPane, selectedInstruments);

        if(resource instanceof MultimediaResource multimediaResource){
            btnIsMultimedia.setSelected(true);
            txtLocation.setText(multimediaResource.getLocation());
            resourceDate.setValue(multimediaResource.getResourceDate().toLocalDate());
        }
    }

    /**
     * Adds a dynamic filtering listener to the ComboBox's editor.
     * <p>
     * Filters the items in the ComboBox based on user input, updating the visible
     * items to those whose string representation contains the typed substring
     * (case-insensitive). The filtered list is displayed immediately as the user types.
     *
     * @param <T>          the type of elements in the ComboBox
     * @param comboBox     the ComboBox to add the listener to
     * @param allElements  the complete list of all elements to filter from
     */
    private <T> void setDynamicResearchListener(ComboBox<T> comboBox, ObservableList<T> allElements) {
        comboBox.getEditor().addEventFilter(KeyEvent.KEY_RELEASED, _ -> {
            String input = comboBox.getEditor().getText().toLowerCase();
            List<T> filtered = allElements.stream()
                    .filter(obj -> obj.toString().toLowerCase().contains(input))
                    .collect(Collectors.toList());
            comboBox.setItems(FXCollections.observableArrayList(filtered));
            comboBox.show();
        });
    }

    /**
     * Adds a listener to the ComboBox to add the selected element to the given
     * ObservableList and update the UI pane with the new selection.
     * <p>
     * When an element is selected in the ComboBox and is not already in the
     * selected list, it is added to the list and the display pane is updated.
     * The ComboBox editor is then cleared.
     *
     * @param <T>                 the type of elements handled by the ComboBox
     * @param comboBox            the ComboBox with selectable items
     * @param selectedElementsPane the FlowPane where selected elements are displayed as buttons
     * @param selectedElements    the ObservableList tracking currently selected elements
     */
    private <T> void setAddingElementListener(ComboBox<T> comboBox, FlowPane selectedElementsPane, ObservableList<T> selectedElements) {
        comboBox.setOnAction(_ -> {
            T selected = comboBox.getValue();
            if (selected != null && !selectedElements.contains(selected)) {
                selectedElements.add(selected);
                updateSelectedElements(selectedElementsPane, selectedElements);
            }
            comboBox.getEditor().clear();
            comboBox.setItems(comboBox.getItems());
        });
    }

    /**
     * Updates the FlowPane displaying the selected elements.
     * <p>
     * Clears all current children of the FlowPane and repopulates it with buttons
     * representing each selected element. Each button has a close icon and removes
     * the element from the selected list when clicked, refreshing the UI accordingly.
     *
     * @param <T>                  the type of elements displayed and managed
     * @param selectedElementsPane the FlowPane container for selected element buttons
     * @param selectedList         the ObservableList of currently selected elements
     */
    private <T> void updateSelectedElements(FlowPane selectedElementsPane, ObservableList<T> selectedList) {
        selectedElementsPane.getChildren().clear();
        for (T element : selectedList) {
            Button button = new Button(element.toString());
            button.getStyleClass().add("author-tag");
            button.setOnAction(_ -> {
                selectedList.remove(element);
                updateSelectedElements(selectedElementsPane, selectedList);
            });
            FontIcon closeIcon = new FontIcon("fas-times");
            button.setGraphic(closeIcon);
            selectedElementsPane.getChildren().add(button);
        }
    }

    /**
     * Sets up a listener on the multimedia toggle button to control the visibility
     * and managed state of location and resource date UI components.
     * <p>
     * When the multimedia button is selected or deselected, the location input box
     * and the resource date picker are shown or hidden accordingly.
     * The initial visibility is also set based on the button's current state.
     */
    private void setIsMultimediaListener() {
        btnIsMultimedia.selectedProperty().addListener((_, _, isSelected) -> {
            locationBox.setVisible(isSelected);
            locationBox.setManaged(isSelected);
            resourceDateBox.setVisible(isSelected);
            resourceDateBox.setManaged(isSelected);
        });

        boolean isSelected = btnIsMultimedia.isSelected();
        locationBox.setVisible(isSelected);
        locationBox.setManaged(isSelected);
        resourceDateBox.setVisible(isSelected);
        resourceDateBox.setManaged(isSelected);
    }

    /**
     * Handles the event triggered when the user attempts to edit and save changes to a resource.
     * <p>
     * This method validates the input fields, extracts selected authors, genres, and instruments,
     * manages the associated track and resource entities in the database, and shows appropriate
     * success or error alerts based on the operation result.
     * <p>
     * It throws a {@link TrackTuneException} if validation fails or any input is missing.
     */
    @FXML
    private void handleEditResource() throws TrackTuneException{
        try {
            if (!checkInput()) {
                throw new TrackTuneException(Strings.FIELD_EMPTY);
            }
            String trackName = trackComboBox.getText();
            ResourceTypeEnum type = resource.getType();

            Integer[] authorIds = selectedAuthors.stream()
                    .map(Author::getId)
                    .toArray(Integer[]::new);

            Integer[] genreIds = selectedGenres.stream()
                    .map(Genre::getId)
                    .toArray(Integer[]::new);

            Integer[] instrumentIds = selectedInstruments.stream()
                    .map(MusicalInstrument::getId)
                    .toArray(Integer[]::new);

            byte[] data = resource.getData();

            boolean isMultimedia = btnIsMultimedia.isSelected();

            int trackId = manageTrackEntity(trackName, ViewManager.getSessionUser().getId(), authorIds, genreIds, instrumentIds);

            manageResourceEntity(type, data, trackId, isMultimedia);

            ViewManager.setAndShowAlert(Strings.SUCCESS, Strings.RESULT, Strings.RESOURCE_UPDATED, Alert.AlertType.INFORMATION);
        } catch (TrackTuneException e) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
            System.err.println(e.getMessage());
        }
    }

    /**
     * Validates the required input fields before saving the resource.
     *
     * @return {@code true} if the track name is not empty, and at least one author and one genre are selected;
     *         {@code false} otherwise or if an exception occurs.
     */
    private boolean checkInput() {
        try {
            if (trackComboBox.getText().isEmpty()) {
                return false;
            }

            if (selectedAuthors.isEmpty()) {
                return false;
            }

            return !selectedGenres.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * A generic StringConverter for entities used in ComboBoxes.
     * Converts an entity to its string representation for display.
     * The reverse conversion is unsupported and returns null.
     *
     * @param <T> the type of the entity
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

    /**
     * Updates the resource entity in the database, handling both multimedia and regular resources.
     *
     * @param type the resource type enum
     * @param data the resource data as a byte array
     * @param trackId the ID of the associated track
     * @param isMultimedia true if the resource is multimedia, false otherwise
     */
    private void manageResourceEntity(ResourceTypeEnum type, byte[] data, int trackId, boolean isMultimedia) {
        if (isMultimedia) {
            String location = txtLocation.getText();
            DatabaseManager.getDAOProvider().getResourceDAO().updateById(new MultimediaResource(type, data, new Timestamp(System.currentTimeMillis()), true,  location, Date.valueOf(resourceDate.getValue()), resource.isAuthor(), trackId, resource.getUserID()), resource.getId());
        } else {
            DatabaseManager.getDAOProvider().getResourceDAO().updateById(new Resource(type, data, new Timestamp(System.currentTimeMillis()), false, resource.isAuthor(), trackId, resource.getUserID()), resource.getId());
        }
    }

    /**
     * Manages the track entity by checking if a track with the given title exists.
     * If not, inserts a new track. Then manages the associations with authors, genres,
     * and instruments for the track.
     *
     * @param title the title of the track
     * @param userId the ID of the user creating or updating the track
     * @param authorIds array of author IDs associated with the track
     * @param genreIds array of genre IDs associated with the track
     * @param instrumentIds array of instrument IDs associated with the track
     * @return the ID of the existing or newly created track
     */
    private int manageTrackEntity(String title, int userId, Integer[] authorIds, Integer[] genreIds, Integer[] instrumentIds) {
        Track track = DatabaseManager.getDAOProvider().getTrackDAO().getByTitle(title);
        int trackId;
        if(track == null)
            trackId = DatabaseManager.getDAOProvider().getTrackDAO().insert(new Track(title, new Timestamp(System.currentTimeMillis()), userId));
        else
            trackId = track.getId();
        manageTrackAuthorRelation(authorIds, trackId);
        manageTrackGenreRelation(genreIds, trackId);
        manageTrackInstrumentRelation(instrumentIds, trackId);
        return trackId;
    }

    /**
     * Synchronizes the relation between the track and its authors.
     * Inserts new relations for authors not currently linked to the track,
     * and removes relations for authors no longer associated.
     *
     * @param authorIds array of author IDs to be linked with the track
     * @param trackId the ID of the track
     */
    private void manageTrackAuthorRelation(Integer[] authorIds, int trackId){
        List<TrackAuthor> trackAuthors = DatabaseManager.getDAOProvider().getTrackAuthorDAO().getByTrackId(trackId);

        for(int authorId : authorIds){
            if(DatabaseManager.getDAOProvider().getTrackAuthorDAO().getByTrackIdAndAuthorId(trackId, authorId) == null)
                DatabaseManager.getDAOProvider().getTrackAuthorDAO().insert(new TrackAuthor(trackId, authorId));
        }

        Set<Integer> newAuthorIdSet = new HashSet<>(Arrays.asList(authorIds));
        for (TrackAuthor ta : trackAuthors) {
            if (!newAuthorIdSet.contains(ta.getAuthorId())) {
                DatabaseManager.getDAOProvider().getTrackAuthorDAO().deleteById(ta.getId());
            }
        }
    }

    /**
     * Synchronizes the relation between the track and its genres.
     * Inserts new relations for genres not currently linked to the track,
     * and removes relations for genres no longer associated.
     *
     * @param genreIds array of genre IDs to be linked with the track
     * @param trackId the ID of the track
     */
    private void manageTrackGenreRelation(Integer[] genreIds, int trackId){
        List<TrackGenre> trackGenres = DatabaseManager.getDAOProvider().getTrackGenreDAO().getByTrackId(trackId);

        for(int genreId: genreIds){
            if(DatabaseManager.getDAOProvider().getTrackGenreDAO().getByTrackIdAndGenreId(trackId, genreId) == null)
                DatabaseManager.getDAOProvider().getTrackGenreDAO().insert(new TrackGenre(trackId, genreId));
        }

        Set<Integer> newAuthorIdSet = new HashSet<>(Arrays.asList(genreIds));
        for (TrackGenre tg : trackGenres) {
            if (!newAuthorIdSet.contains(tg.getGenreId())) {
                DatabaseManager.getDAOProvider().getTrackGenreDAO().deleteById(tg.getId());
            }
        }
    }

    /**
     * Synchronizes the relation between the track and its musical instruments.
     * Inserts new relations for instruments not currently linked to the track,
     * and removes relations for instruments no longer associated.
     *
     * @param instrumentIds array of instrument IDs to be linked with the track
     * @param trackId the ID of the track
     */
    private void manageTrackInstrumentRelation(Integer[] instrumentIds, int trackId){
        List<TrackInstrument> trackInstruments = DatabaseManager.getDAOProvider().getTrackInstrumentDAO().getByTrackId(trackId);

        for(int instrumentId : instrumentIds){
            if(DatabaseManager.getDAOProvider().getTrackInstrumentDAO().getByTrackIdAndInstrumentId(trackId, instrumentId) == null)
                DatabaseManager.getDAOProvider().getTrackInstrumentDAO().insert(new TrackInstrument(trackId, instrumentId));
        }

        Set<Integer> newAuthorIdSet = new HashSet<>(Arrays.asList(instrumentIds));
        for (TrackInstrument ti : trackInstruments) {
            if (!newAuthorIdSet.contains(ti.getInstrumentId())) {
                DatabaseManager.getDAOProvider().getTrackInstrumentDAO().deleteById(ti.getId());
            }
        }
    }

    /**
     * Call the method resetFields()
     */
    @FXML
    private void handleReset(){
        resetFields();
    }

    /**
     * Clears all input fields, resets selections and toggles to their default state.
     * This includes clearing text fields, combo boxes, toggle buttons,
     * and clearing all selected elements and their visual representations.
     */
    private void resetFields() {
        trackComboBox.clear();
        txtLocation.clear();
        resourceDate.setValue(null);
        selectedAuthors.clear();
        selectedGenres.clear();
        selectedInstruments.clear();
        authorComboBox.getEditor().clear();
        genreComboBox.getEditor().clear();
        instrumentComboBox.getEditor().clear();
        btnIsMultimedia.setSelected(false);
        btnIsAuthor.setSelected(false);
        selectedAuthorsPane.getChildren().clear();
        selectedGenresPane.getChildren().clear();
        selectedInstrumentsPane.getChildren().clear();
    }

    /**
     * Handles the return action to navigate back to the "My Resources" view.
     * <p>
     * Checks the type of the parent controller and sets the main content
     * accordingly to the "My Resources" view for authenticated users or admins.
     * <p>
     * If any exception occurs during navigation, an error alert is displayed
     * and the exception message is logged to the console.
     */
    @FXML
    private void handleReturn(){
        try{
            if(parentController instanceof AuthenticatedUserDashboardController authController){
                ViewManager.setMainContent(Frames.MY_RESOURCES_VIEW_PATH, authController.mainContent, parentController);
            }else if(parentController instanceof AdminDashboardController adminController){
                ViewManager.setMainContent(Frames.TRACKS_VIEW_PATH_VIEW_PATH, adminController.mainContent, parentController);
            }
        }catch(Exception e){
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
            System.err.println(e.getMessage());
        }
    }

    /**
     * Handles the event triggered by the add author button click.
     * <p>
     * Validates the input author name, checks for SQL injection,
     * verifies the author does not already exist, and inserts the new author into the database.
     * If the insertion is successful, the author is added to both the selected authors list
     * and the full authors list, and the UI is updated accordingly.
     * <p>
     * If the author name is empty, an invalid input, or already exists, or if a database error occurs,
     * an alert is shown with the appropriate error message.
     */
    @FXML
    private void handleAddAuthor(){
        try{
            if(authorComboBox.getEditor().getText().isEmpty())
                throw new TrackTuneException(Strings.INSERT_VALID_AUTHOR);

            String authorString = authorComboBox.getEditor().getText();

            if(SQLiteScripts.checkForSQLInjection(authorString))
                throw new TrackTuneException(Strings.ERR_SQL_INJECTION);

            if(!DatabaseManager.getDAOProvider().getAuthorDAO().existByAuthorshipName(Controller.toTitleCase(authorString))){
                Author newAuthor = new Author(Controller.toTitleCase(authorString), AuthorStatusEnum.ACTIVE);
                if(DatabaseManager.getDAOProvider().getAuthorDAO().insert(newAuthor) != null){
                    selectedAuthors.add(newAuthor);
                    allAuthors.add(newAuthor);
                    updateSelectedElements(selectedAuthorsPane, selectedAuthors);
                    authorComboBox.getEditor().clear();
                    authorComboBox.setItems(allAuthors);
                }
                else{
                    throw new TrackTuneException(Strings.ERR_DATABASE);
                }
            }
            else{
                throw new AuthorAlreadyExistsException(Strings.ERR_AUTHOR_ALREADY_EXISTS);
            }
        }catch (TrackTuneException e){
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.AUTHOR_FAILED, e.getMessage(), Alert.AlertType.ERROR);
        }
    }
}
