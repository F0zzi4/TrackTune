package app.tracktune.controller.common;

import app.tracktune.controller.Controller;
import app.tracktune.controller.admin.AdminDashboardController;
import app.tracktune.controller.authenticatedUser.AuthenticatedUserDashboardController;
import app.tracktune.utils.SessionManager;
import app.tracktune.exceptions.AuthorAlreadyExistsException;
import app.tracktune.exceptions.TrackTuneException;
import app.tracktune.utils.DatabaseManager;
import app.tracktune.model.author.Author;
import app.tracktune.model.author.AuthorStatusEnum;
import app.tracktune.model.genre.Genre;
import app.tracktune.model.musicalInstrument.MusicalInstrument;
import app.tracktune.model.resource.*;
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
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AddResourceController extends Controller implements Initializable {
    /** Text field for displaying or entering the file path of the resource. */
    @FXML
    private TextField txtFilePath;

    /** Button to browse and select a file from the filesystem. */
    @FXML
    private Button btnBrowseFile;

    /** FlowPane containing the selected tracks. */
    @FXML
    private FlowPane selectedTrackPane;

    /** ComboBox for selecting an author from the list. */
    @FXML
    private ComboBox<Author> authorComboBox;

    /** FlowPane containing the selected authors. */
    @FXML
    private FlowPane selectedAuthorsPane;

    /** ComboBox for selecting a genre from the list. */
    @FXML
    private ComboBox<Genre> genreComboBox;

    /** FlowPane containing the selected genres. */
    @FXML
    private FlowPane selectedGenresPane;

    /** ComboBox for selecting a musical instrument from the list. */
    @FXML
    private ComboBox<MusicalInstrument> instrumentComboBox;

    /** FlowPane containing the selected musical instruments. */
    @FXML
    private FlowPane selectedInstrumentsPane;

    /** Toggle button indicating whether the resource is multimedia. */
    @FXML
    private MFXToggleButton btnIsMultimedia;

    /** Container HBox for location input elements. */
    @FXML
    private HBox locationBox;

    /** Text field for entering the location of the resource. */
    @FXML
    private TextField txtLocation;

    /** Date picker for selecting the date associated with the resource. */
    @FXML
    private DatePicker resourceDate;

    /** Container HBox for the resource date elements. */
    @FXML
    private HBox resourceDateBox;

    /** ComboBox for selecting a track related to the resource. */
    @FXML
    private ComboBox<Track> trackComboBox;

    /** Toggle button indicating whether the resource is a link. */
    @FXML
    private MFXToggleButton btnIsLink;

    /** Container HBox for resource link input elements. */
    @FXML
    private HBox resourceLinkBox;

    /** Text field for entering the resource link URL. */
    @FXML
    private TextField txtResourceLink;

    /** Container HBox for file path input elements. */
    @FXML
    private HBox filePathBox;

    /** Toggle button indicating whether the resource has an author. */
    @FXML
    private MFXToggleButton btnIsAuthor;

    /** Observable list containing all available tracks. */
    private final ObservableList<Track> allTracks = FXCollections.observableArrayList();

    /** Observable list containing all available authors. */
    private final ObservableList<Author> allAuthors = FXCollections.observableArrayList();

    /** Observable list containing all available genres. */
    private final ObservableList<Genre> allGenres = FXCollections.observableArrayList();

    /** Observable list containing all available musical instruments. */
    private final ObservableList<MusicalInstrument> allMusicalInstruments = FXCollections.observableArrayList();

    /** Observable list containing tracks currently selected by the user. */
    private final ObservableList<Track> selectedTracks = FXCollections.observableArrayList();

    /** Observable list containing authors currently selected by the user. */
    private final ObservableList<Author> selectedAuthors = FXCollections.observableArrayList();

    /** Observable list containing genres currently selected by the user. */
    private final ObservableList<Genre> selectedGenres = FXCollections.observableArrayList();

    /** Observable list containing musical instruments currently selected by the user. */
    private final ObservableList<MusicalInstrument> selectedInstruments = FXCollections.observableArrayList();

    /**
     * Initializes the controller by loading data and setting up UI components.
     * <p>
     * - Loads all tracks, authors, genres, and musical instruments from the database.
     * - Sets converters and enables editing on ComboBoxes for tracks, authors, genres, and instruments.
     * - Sets the items of each ComboBox with the respective data lists.
     * - Adds dynamic search listeners to allow filtering the ComboBoxes based on user input.
     * - Adds listeners to handle adding selected items to their corresponding panes and lists.
     * - Sets up listeners for file selection, multimedia toggle, and link toggle.
     * <p>
     * If an exception occurs during initialization, an error alert is shown and the error message is printed.
     *
     * @param url not used
     * @param resourceBundle not used
     */
    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            allTracks.addAll(DatabaseManager.getDAOProvider().getTrackDAO().getAll());
            allAuthors.addAll(DatabaseManager.getDAOProvider().getAuthorDAO().getAllActive());
            allGenres.addAll(DatabaseManager.getDAOProvider().getGenreDAO().getAll());
            allMusicalInstruments.addAll(DatabaseManager.getDAOProvider().getMusicalInstrumentDAO().getAll());

            trackComboBox.setConverter(new EntityToStringConverter<>());
            trackComboBox.setEditable(true);
            trackComboBox.setItems(allTracks);

            authorComboBox.setConverter(new EntityToStringConverter<>());
            authorComboBox.setEditable(true);
            authorComboBox.setItems(allAuthors);

            genreComboBox.setConverter(new EntityToStringConverter<>());
            genreComboBox.setEditable(true);
            genreComboBox.setItems(allGenres);

            instrumentComboBox.setConverter(new EntityToStringConverter<>());
            instrumentComboBox.setEditable(true);
            instrumentComboBox.setItems(allMusicalInstruments);

            setDynamicResearchListener(trackComboBox, allTracks);
            setDynamicResearchListener(authorComboBox, allAuthors);
            setDynamicResearchListener(genreComboBox, allGenres);
            setDynamicResearchListener(instrumentComboBox, allMusicalInstruments);

            setTrackAddingElementListener(trackComboBox, selectedTrackPane, selectedTracks);
            setAddingElementListener(authorComboBox, selectedAuthorsPane, selectedAuthors);
            setAddingElementListener(genreComboBox, selectedGenresPane, selectedGenres);
            setAddingElementListener(instrumentComboBox, selectedInstrumentsPane, selectedInstruments);

            setSearchFileListener();
            setIsMultimediaListener();
            setIsLinkListener();
        } catch (Exception e) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERR_GENERAL, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
            System.err.println(e.getMessage());
        }
    }

    /**
     * Adds a dynamic search listener to the ComboBox editor that filters the ComboBox items
     * based on the user's input after each key release.
     * <p>
     * The filtering matches any element whose toString() contains the typed substring (case-insensitive).
     * The ComboBox items are updated to the filtered list and the drop-down is shown.
     *
     * @param <T> the type of elements in the ComboBox and the list
     * @param comboBox the ComboBox to attach the listener to
     * @param allElements the complete list of all possible elements to filter from
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
     * Sets a listener for item selection on the ComboBox that updates the selected elements list and pane.
     * <p>
     * When a new element is selected, it clears the current selection and adds the new one,
     * updates the UI pane with the selected elements, and if the element is a Track,
     * loads additional track information.
     * After processing, it clears the ComboBox editor and resets the items list.
     *
     * @param <T> the type of elements in the ComboBox and the selected list
     * @param comboBox the ComboBox to monitor for selection changes
     * @param selectedElementsPane the UI container where selected elements are displayed
     * @param selectedElements the ObservableList storing currently selected elements
     */
    private <T> void setTrackAddingElementListener(ComboBox<T> comboBox, FlowPane selectedElementsPane, ObservableList<T> selectedElements) {
        comboBox.setOnAction(_ -> {
            T selected = comboBox.getValue();
            if (selected != null && !selectedElements.contains(selected)) {
                selectedElements.clear();
                selectedElements.add(selected);
                updateSelectedElements(selectedElementsPane, selectedElements);
                if(selected instanceof Track track)
                    loadTrackInfo(track);
            }
            comboBox.getEditor().clear();
            comboBox.setItems(comboBox.getItems());
        });
    }

    /**
     * Loads detailed information about the given track, updating the UI components accordingly.
     * <p>
     * Sets the track title in the trackComboBox editor, then fetches associated authors, genres,
     * and musical instruments from the database. Clears and updates the selected authors, genres,
     * and instruments lists and their corresponding UI panes.
     *
     * @param track the Track object whose information is to be loaded
     */
    private void loadTrackInfo(Track track){
        trackComboBox.getEditor().setText(track.getTitle());

        List<TrackAuthor> trackAuthors = DatabaseManager.getDAOProvider().getTrackAuthorDAO().getByTrackId(track.getId());
        List<TrackGenre> trackGenres = DatabaseManager.getDAOProvider().getTrackGenreDAO().getByTrackId(track.getId());
        List<TrackInstrument> trackInstruments = DatabaseManager.getDAOProvider().getTrackInstrumentDAO().getByTrackId(track.getId());

        selectedAuthors.clear();
        for(TrackAuthor trackAuthor : trackAuthors){
            selectedAuthors.add(DatabaseManager.getDAOProvider().getAuthorDAO().getById(trackAuthor.getAuthorId()));
        }
        updateSelectedElements(selectedAuthorsPane, selectedAuthors);

        selectedGenres.clear();
        for(TrackGenre trackGenre : trackGenres){
            selectedGenres.add(DatabaseManager.getDAOProvider().getGenreDAO().getById(trackGenre.getGenreId()));
        }
        updateSelectedElements(selectedGenresPane, selectedGenres);

        selectedInstruments.clear();
        for(TrackInstrument trackInstrument : trackInstruments){
            selectedInstruments.add(DatabaseManager.getDAOProvider().getMusicalInstrumentDAO().getById(trackInstrument.getInstrumentId()));
        }
        updateSelectedElements(selectedInstrumentsPane, selectedInstruments);
    }

    /**
     * Sets a listener on the ComboBox to add selected elements to the provided selected elements list.
     * <p>
     * When a new element is selected, and it is not already in the selected list, it is added.
     * The UI pane for selected elements is updated accordingly.
     * The ComboBox editor is cleared and its items list is reset after the selection.
     *
     * @param <T> the type of elements in the ComboBox and the selected list
     * @param comboBox the ComboBox to listen for selections
     * @param selectedElementsPane the UI container displaying the selected elements
     * @param selectedElements the list maintaining currently selected elements
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
     * Updates the given FlowPane to display buttons representing the currently selected elements.
     * <p>
     * Each element is represented as a Button with the element's string representation as text
     * and a close icon graphic. Clicking the button removes the element from the selected list
     * and updates the FlowPane accordingly.
     *
     * @param <T> the type of the elements in the selected list
     * @param selectedElementsPane the FlowPane container to update with buttons
     * @param selectedList the ObservableList containing currently selected elements
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
     * Sets up the action listener for the browse file button to open a file chooser dialog.
     * <p>
     * The file chooser filters for audio files and all files. When a file is selected,
     * its absolute path is set to the txtFilePath TextField.
     */
    private void setSearchFileListener() {
        btnBrowseFile.setOnAction(_ -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Audio File");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Audio Files", ResourceTypeEnum.getExtensions()),
                    new FileChooser.ExtensionFilter("All Files", "*.*")
            );
            File selectedFile = fileChooser.showOpenDialog(btnBrowseFile.getScene().getWindow());
            if (selectedFile != null) {
                txtFilePath.setText(selectedFile.getAbsolutePath());
            }
        });
    }

    /**
     * Initializes the listener for the multimedia toggle button.
     * <p>
     * When the toggle button's selected state changes, this method updates the visibility
     * or enablement of multimedia-related controls accordingly by calling
     * {@link #setMultimediaAssociatedControls(boolean)}.
     * It also sets the initial state of these controls based on the current toggle state.
     */
    private void setIsMultimediaListener() {
        btnIsMultimedia.selectedProperty().addListener((_, _, isSelected) -> setMultimediaAssociatedControls(isSelected));

        boolean isSelected = btnIsMultimedia.isSelected();
        setMultimediaAssociatedControls(isSelected);
    }

    /**
     * Updates the visibility and managed properties of controls associated with multimedia resources.
     *
     * @param isSelected true if the multimedia toggle button is selected, false otherwise.
     *                   When true, shows multimedia-related controls and hides link-related controls.
     *                   When false, does the opposite.
     */
    private void setMultimediaAssociatedControls(boolean isSelected) {
        resourceLinkBox.setVisible(btnIsLink.isSelected() && !isSelected);
        resourceLinkBox.setManaged(btnIsLink.isSelected() && !isSelected);
        locationBox.setVisible(isSelected);
        locationBox.setManaged(isSelected);
        resourceDateBox.setVisible(isSelected);
        resourceDateBox.setManaged(isSelected);
        btnIsLink.setVisible(!isSelected);
        btnIsLink.setManaged(!isSelected);
    }

    /**
     * Initializes the listener for the link toggle button.
     * <p>
     * When the toggle button's selected state changes, updates the visibility and managed properties
     * of link-related controls by calling {@link #setLinkAssociatedControls(boolean)}.
     * Also sets the initial state of those controls based on the current toggle state.
     */
    private void setIsLinkListener() {
        btnIsLink.selectedProperty().addListener((_, _, isSelected) -> setLinkAssociatedControls(isSelected));

        boolean isSelected = btnIsLink.isSelected();
        setLinkAssociatedControls(isSelected);
    }

    /**
     * Updates the visibility and managed properties of controls associated with resource links.
     *
     * @param isSelected true if the link toggle button is selected, false otherwise.
     *                   When true, shows link-related controls and hides multimedia-related controls.
     *                   When false, shows multimedia-related controls and hides link-related controls.
     */
    private void setLinkAssociatedControls(boolean isSelected){
        locationBox.setVisible(btnIsMultimedia.isSelected() && !isSelected);
        locationBox.setManaged(btnIsMultimedia.isSelected() && !isSelected);
        resourceDateBox.setVisible(btnIsMultimedia.isSelected() && !isSelected);
        resourceDateBox.setManaged(btnIsMultimedia.isSelected() && !isSelected);
        resourceLinkBox.setVisible(isSelected);
        resourceLinkBox.setManaged(isSelected);
        filePathBox.setVisible(!isSelected);
        filePathBox.setManaged(!isSelected);
        btnIsMultimedia.setVisible(!isSelected);
        btnIsMultimedia.setManaged(!isSelected);
    }

    /**
     * Handles the addition of a new resource.
     * <p>
     * Validates input fields, creates or retrieves the associated track,
     * gathers selected authors, genres, and instruments, and uploads the resource data.
     * Supports both file uploads and resource links.
     * Displays success or error alerts based on the operation outcome.
     * </p>
     *
     * @throws TrackTuneException if input validation fails or resource upload is unsuccessful.
     */
    @FXML
    private void handleAddResource() throws TrackTuneException{
        try {
            if (!checkInput()) {
                throw new TrackTuneException(Strings.FIELD_EMPTY);
            }
            String trackTitle = !trackComboBox.getEditor().getText().isEmpty() ? trackComboBox.getEditor().getText() : selectedTracks.getFirst().getTitle();
            ResourceTypeEnum type = getFileExtensionToEnum();

            Integer[] authorIds = selectedAuthors.stream()
                    .map(Author::getId)
                    .toArray(Integer[]::new);

            Integer[] genreIds = selectedGenres.stream()
                    .map(Genre::getId)
                    .toArray(Integer[]::new);

            Integer[] instrumentIds = selectedInstruments.stream()
                    .map(MusicalInstrument::getId)
                    .toArray(Integer[]::new);

            String filePath = txtFilePath.getText();

            byte[] data;
            if(btnIsLink.isSelected())
                data = txtResourceLink.getText().getBytes(StandardCharsets.UTF_8);
            else
                data = createBytesFromPath(filePath);

            int trackId;
            if(selectedTracks.isEmpty())
                trackId = DatabaseManager.getDAOProvider().getTrackDAO().insert(new Track(trackTitle,new Timestamp(System.currentTimeMillis()), ViewManager.getSessionUser().getId()));
            else
                trackId = selectedTracks.getFirst().getId();

            manageTrackEntity(trackId, authorIds, genreIds, instrumentIds);
            Integer result = manageResourceEntity(type, data, trackId, btnIsMultimedia.isSelected());

            if (result != null){
                ViewManager.setAndShowAlert(Strings.SUCCESS, Strings.RESULT, Strings.RESOURCE_UPLOADED, Alert.AlertType.INFORMATION);
                resetFields();
            }
            else
                throw new TrackTuneException(Strings.RESOURCE_NOT_UPLOADED);
        } catch (TrackTuneException e) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERR_GENERAL, e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
            System.err.println(e.getMessage());
        }
    }

    /**
     * Checks that all required fields for adding the resource are properly filled.
     * <p>
     * It verifies that:
     * - a track is either selected or entered,
     * - at least one author and one genre are selected,
     * - if the "link" toggle is selected, the link field is not empty,
     * - otherwise, the file path field is not empty.
     * </p>
     *
     * @return true if all fields are valid, false otherwise.
     */
    private boolean checkInput() {
        try {
            if (selectedTracks.isEmpty() && trackComboBox.getEditor().getText().isEmpty()) {
                return false;
            }

            if (selectedAuthors.isEmpty()) {
                return false;
            }

            if (selectedGenres.isEmpty()) {
                return false;
            }

            if(btnIsLink.isSelected())
                return txtFilePath.getText() != null && !txtResourceLink.getText().trim().isEmpty();
            else
                return txtFilePath.getText() != null && !txtFilePath.getText().trim().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Creates a byte array by reading the content of the file at the given path.
     *
     * @param filePath the full path of the file to read.
     * @return a byte array containing the file data.
     * @throws IOException if the file does not exist or is not a valid file.
     * @throws TrackTuneException if an error occurs while reading the file.
     */
    public byte[] createBytesFromPath(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            throw new IOException(Strings.ERR_FILE_NOT_FOUND);
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            return fis.readAllBytes();
        } catch (IOException e) {
            throw new TrackTuneException(Strings.ERR_LOAD_FILE);
        }
    }

    /**
     * A simple StringConverter implementation that converts an entity to its string representation.
     * Used for ComboBox editors to display the object's toString() value.
     * The fromString method returns null because editing by typing a new string is not supported here.
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
     * Determines the ResourceTypeEnum based on the current input.
     * If the "link" toggle is selected, returns ResourceTypeEnum.link.
     * Otherwise, extracts the file extension from the file path text field
     * and returns the corresponding ResourceTypeEnum value.
     *
     * @return the ResourceTypeEnum matching the current resource.
     */
    private ResourceTypeEnum getFileExtensionToEnum() {
        if(btnIsLink.isSelected()){
            return ResourceTypeEnum.link;
        }else{
            String fileName = txtFilePath.getText();
            String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
            return ResourceTypeEnum.valueOf(extension.toLowerCase());
        }
    }

    /**
     * Creates and inserts a Resource entity (either MultimediaResource or basic Resource) into the database.
     * Uses the provided type, data, associated trackId, and flags indicating if it's multimedia and if the current user is the author.
     *
     * @param type The type of resource (audio, video, link, etc.).
     * @param data The resource data in bytes.
     * @param trackId The ID of the associated track.
     * @param isMultimedia Whether the resource is multimedia (has location and date info).
     * @return The generated ID of the inserted resource, or null if insertion failed.
     */
    private Integer manageResourceEntity(ResourceTypeEnum type, byte[] data, int trackId, boolean isMultimedia) {
        Resource resource;
        if (isMultimedia) {
            String location = null;
            if(txtLocation.getText() != null && !txtLocation.getText().isEmpty())
                location = txtLocation.getText();
            Date date = resourceDate.getValue() != null ? Date.valueOf(resourceDate.getValue()) : null;
            resource = new MultimediaResource(type, data, new Timestamp(System.currentTimeMillis()), true,
                    location, date, btnIsAuthor.isSelected(), trackId, SessionManager.getInstance().getUser().getId());
        } else {
            resource = new Resource(type, data, new Timestamp(System.currentTimeMillis()), false, btnIsAuthor.isSelected(),trackId, SessionManager.getInstance().getUser().getId());
        }
        return DatabaseManager.getDAOProvider().getResourceDAO().insert(resource);
    }

    /**
     * Manages the associations between a track and its related authors, genres, and instruments.
     * Calls helper methods to insert missing relations in the database.
     *
     * @param trackId The ID of the track.
     * @param authorIds Array of author IDs related to the track.
     * @param genreIds Array of genre IDs related to the track.
     * @param instrumentIds Array of instrument IDs related to the track.
     */
    private void manageTrackEntity(int trackId, Integer[] authorIds, Integer[] genreIds, Integer[] instrumentIds) {
        manageTrackAuthorRelation(authorIds, trackId);
        manageTrackGenreRelation(genreIds, trackId);
        manageTrackInstrumentRelation(instrumentIds, trackId);
    }

    /**
     * Inserts relations between the track and authors if they do not already exist.
     *
     * @param authorIds Array of author IDs.
     * @param trackId The track ID.
     */
    private void manageTrackAuthorRelation(Integer[] authorIds, int trackId){
        for(int authorId : authorIds)
            if(DatabaseManager.getDAOProvider().getTrackAuthorDAO().getByTrackIdAndAuthorId(trackId, authorId) == null)
                DatabaseManager.getDAOProvider().getTrackAuthorDAO().insert(new TrackAuthor(trackId, authorId));
    }

    /**
     * Inserts relations between the track and genres if they do not already exist.
     *
     * @param genreIds Array of genre IDs.
     * @param trackId The track ID.
     */
    private void manageTrackGenreRelation(Integer[] genreIds, int trackId){
        for(int genreId: genreIds)
            if(DatabaseManager.getDAOProvider().getTrackGenreDAO().getByTrackIdAndGenreId(trackId, genreId) == null)
                DatabaseManager.getDAOProvider().getTrackGenreDAO().insert(new TrackGenre(trackId, genreId));
    }

    /**
     * Inserts relations between the track and instruments if they do not already exist.
     *
     * @param instrumentIds Array of instrument IDs.
     * @param trackId The track ID.
     */
    private void manageTrackInstrumentRelation(Integer[] instrumentIds, int trackId){
        for(int instrumentId : instrumentIds)
            if(DatabaseManager.getDAOProvider().getTrackInstrumentDAO().getByTrackIdAndInstrumentId(trackId, instrumentId) == null)
                DatabaseManager.getDAOProvider().getTrackInstrumentDAO().insert(new TrackInstrument(trackId, instrumentId));
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
        txtResourceLink.clear();
        trackComboBox.setValue(null);
        txtFilePath.clear();
        txtLocation.clear();
        resourceDate.setValue(null);
        selectedTracks.clear();
        selectedAuthors.clear();
        selectedGenres.clear();
        selectedInstruments.clear();
        authorComboBox.getEditor().clear();
        genreComboBox.getEditor().clear();
        instrumentComboBox.getEditor().clear();
        btnIsMultimedia.setSelected(false);
        btnIsLink.setSelected(false);
        btnIsAuthor.setSelected(false);
        selectedTrackPane.getChildren().clear();
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
    private void handleReturn() {
        try {
            if (parentController instanceof AuthenticatedUserDashboardController authController) {
                ViewManager.setMainContent(Frames.MY_RESOURCES_VIEW_PATH, authController.mainContent, parentController);
            }
            else if(parentController instanceof AdminDashboardController adminController){
                ViewManager.setMainContent(Frames.MY_RESOURCES_VIEW_PATH, adminController.mainContent, parentController);
            }
        } catch (Exception e) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
            System.err.println(e.getMessage());
        }
    }

    /**
     * Handles the addition of a new author from the authorComboBox editor input.
     * <p>
     * Validates the input to ensure it is not empty and free from SQL injection risks.
     * Checks if the author already exists in the database. If not, creates a new active author,
     * inserts it into the database, updates the selected and available authors lists,
     * and refreshes the UI accordingly.
     * <p>
     * Displays an error alert if validation fails, the author already exists, or database insertion fails.
     */
    @FXML
    private void handleAddAuthor(){
        try{
            if(authorComboBox.getEditor().getText().isEmpty())
                throw new TrackTuneException(Strings.INSERT_VALID_AUTHOR);

            String authorString = Controller.toTitleCase(authorComboBox.getEditor().getText());

            if(SQLiteScripts.checkForSQLInjection(authorString))
                throw new TrackTuneException(Strings.ERR_SQL_INJECTION);

            if(!DatabaseManager.getDAOProvider().getAuthorDAO().existByAuthorshipName(Controller.toTitleCase(authorString))){
                Author newAuthor = new Author(authorString, AuthorStatusEnum.ACTIVE);
                int id = DatabaseManager.getDAOProvider().getAuthorDAO().insert(newAuthor);
                if(id != 0){
                    newAuthor = new Author(id, authorString, AuthorStatusEnum.ACTIVE);
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