package app.tracktune.controller.common;

import app.tracktune.controller.Controller;
import app.tracktune.controller.admin.AdminDashboardController;
import app.tracktune.controller.authenticatedUser.AuthenticatedUserDashboardController;
import app.tracktune.controller.authentication.SessionManager;
import app.tracktune.exceptions.AuthorAlreadyExixtsExeption;
import app.tracktune.exceptions.TrackTuneException;
import app.tracktune.model.DatabaseManager;
import app.tracktune.model.author.Author;
import app.tracktune.model.author.AuthorStatusEnum;
import app.tracktune.model.genre.Genre;
import app.tracktune.model.musicalInstrument.MusicalInstrument;
import app.tracktune.model.resource.*;
import app.tracktune.model.track.*;
import app.tracktune.utils.Frames;
import app.tracktune.utils.ResourceManager;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AddResourceController extends Controller implements Initializable {
    @FXML private TextField txtFilePathField;
    @FXML private Button btnBrowseFile;
    @FXML private FlowPane selectedTrackPane;
    @FXML private ComboBox<Author> authorComboBox;
    @FXML private FlowPane selectedAuthorsPane;
    @FXML private ComboBox<Genre> genreComboBox;
    @FXML private FlowPane selectedGenresPane;
    @FXML private ComboBox<MusicalInstrument> instrumentComboBox;
    @FXML private FlowPane selectedInstrumentsPane;
    @FXML private MFXToggleButton btnIsMultimedia;
    @FXML private HBox locationBox;
    @FXML private TextField txtLocation;
    @FXML private DatePicker resourceDate;
    @FXML private HBox resourceDateBox;
    @FXML private ComboBox<Track> trackComboBox;

    // Data set
    private final ObservableList<Track> allTracks = FXCollections.observableArrayList();
    private final ObservableList<Author> allAuthors = FXCollections.observableArrayList();
    private final ObservableList<Genre> allGenres = FXCollections.observableArrayList();
    private final ObservableList<MusicalInstrument> allMusicalInstruments = FXCollections.observableArrayList();

    // Selected elements lists
    private final ObservableList<Author> selectedAuthors = FXCollections.observableArrayList();
    private final ObservableList<Genre> selectedGenres = FXCollections.observableArrayList();
    private final ObservableList<MusicalInstrument> selectedInstruments = FXCollections.observableArrayList();
    private final ObservableList<Track> selectedTracks = FXCollections.observableArrayList();

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
        } catch (Exception e) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERR_GENERAL, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
            System.err.println(e.getMessage());
        }
    }

    private <T> void setDynamicResearchListener(ComboBox<T> comboBox, ObservableList<T> allElements) {
        comboBox.getEditor().addEventFilter(KeyEvent.KEY_RELEASED, e -> {
            String input = comboBox.getEditor().getText().toLowerCase();
            List<T> filtered = allElements.stream()
                    .filter(obj -> obj.toString().toLowerCase().contains(input))
                    .collect(Collectors.toList());
            comboBox.setItems(FXCollections.observableArrayList(filtered));
            comboBox.show();
        });
    }

    private <T> void setTrackAddingElementListener(ComboBox<T> comboBox, FlowPane selectedElementsPane, ObservableList<T> selectedElements) {
        comboBox.setOnAction(e -> {
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

    private <T> void setAddingElementListener(ComboBox<T> comboBox, FlowPane selectedElementsPane, ObservableList<T> selectedElements) {
        comboBox.setOnAction(e -> {
            T selected = comboBox.getValue();
            if (selected != null && !selectedElements.contains(selected)) {
                selectedElements.add(selected);
                updateSelectedElements(selectedElementsPane, selectedElements);
            }
            comboBox.getEditor().clear();
            comboBox.setItems(comboBox.getItems());
        });
    }

    private <T> void updateSelectedElements(FlowPane selectedElementsPane, ObservableList<T> selectedList) {
        selectedElementsPane.getChildren().clear();
        for (T element : selectedList) {
            Label tag = new Label(element.toString());
            tag.getStyleClass().add("author-tag");
            selectedElementsPane.getChildren().add(tag);
        }
    }

    private void setSearchFileListener() {
        btnBrowseFile.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Audio File");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav", "*.mp4", "*.pdf", "*.midi", "*.jpg", "*.png"),
                    new FileChooser.ExtensionFilter("All Files", "*.*")
            );
            File selectedFile = fileChooser.showOpenDialog(btnBrowseFile.getScene().getWindow());
            if (selectedFile != null) {
                txtFilePathField.setText(selectedFile.getAbsolutePath());
            }
        });
    }

    private void setIsMultimediaListener() {
        btnIsMultimedia.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
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

    @FXML
    private void handleAddResource() {
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

            String filePath = txtFilePathField.getText();
            byte[] data = createFileFromPath(filePath);

            boolean isMultimedia = btnIsMultimedia.isSelected();

            int trackId;
            if(selectedTracks.isEmpty())
                trackId = DatabaseManager.getDAOProvider().getTrackDAO().insert(new Track(trackTitle,new Timestamp(System.currentTimeMillis()), ViewManager.getSessionUser().getId()));
            else
                trackId = selectedTracks.getFirst().getId();

            manageTrackEntity(trackId, authorIds, genreIds, instrumentIds);
            Integer result = manageResourceEntity(type, data, trackId, isMultimedia);

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

            return txtFilePathField.getText() != null && !txtFilePathField.getText().trim().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    public byte[] createFileFromPath(String filePath) throws IOException {
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

    // Converter for ComboBox from object T to String
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

    private ResourceTypeEnum getFileExtensionToEnum() {
        String fileName = txtFilePathField.getText();
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        return ResourceTypeEnum.valueOf(extension.toLowerCase());
    }

    private Integer manageResourceEntity(ResourceTypeEnum type, byte[] data, int trackId, boolean isMultimedia) {
        Integer result;
        if (isMultimedia) {
            Time duration = ResourceManager.calcMediaDuration(data, type.toString());
            String location = txtLocation.getText();
            result = DatabaseManager.getDAOProvider().getResourceDAO().insert(new MultimediaResource(type, data, new Timestamp(System.currentTimeMillis()), true,
                    duration, location, Date.valueOf(resourceDate.getValue()), trackId, SessionManager.getInstance().getUser().getId()));
        } else {
            result = DatabaseManager.getDAOProvider().getResourceDAO().insert(new Resource(type, data, new Timestamp(System.currentTimeMillis()), false, trackId, SessionManager.getInstance().getUser().getId()));
        }
        return result;
    }

    private void manageTrackEntity(int trackId, Integer[] authorIds, Integer[] genreIds, Integer[] instrumentIds) {
        manageTrackAuthorRelation(authorIds, trackId);
        manageTrackGenreRelation(genreIds, trackId);
        manageTrackInstrumentRelation(instrumentIds, trackId);
    }

    private void manageTrackAuthorRelation(Integer[] authorIds, int trackId){
        for(int authorId : authorIds)
            if(DatabaseManager.getDAOProvider().getTrackAuthorDAO().getByTrackIdAndAuthorId(trackId, authorId) == null)
                DatabaseManager.getDAOProvider().getTrackAuthorDAO().insert(new TrackAuthor(trackId, authorId));
    }

    private void manageTrackGenreRelation(Integer[] genreIds, int trackId){
        for(int genreId: genreIds)
            if(DatabaseManager.getDAOProvider().getTrackGenreDAO().getByTrackIdAndGenreId(trackId, genreId) == null)
                DatabaseManager.getDAOProvider().getTrackGenreDAO().insert(new TrackGenre(trackId, genreId));
    }

    private void manageTrackInstrumentRelation(Integer[] instrumentIds, int trackId){
        for(int instrumentId : instrumentIds)
            if(DatabaseManager.getDAOProvider().getTrackInstrumentDAO().getByTrackIdAndInstrumentId(trackId, instrumentId) == null)
                DatabaseManager.getDAOProvider().getTrackInstrumentDAO().insert(new TrackInstrument(trackId, instrumentId));
    }

    @FXML
    private void handleReset(){
        resetFields();
    }

    private void resetFields() {
        trackComboBox.setValue(null);
        txtFilePathField.clear();
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
        selectedTrackPane.getChildren().clear();
        selectedAuthorsPane.getChildren().clear();
        selectedGenresPane.getChildren().clear();
        selectedInstrumentsPane.getChildren().clear();
    }

    /**
     * Handles the return button click, going back to the previous view.
     */
    @FXML
    private void handleReturn() {
        try {
            if (parentController instanceof AuthenticatedUserDashboardController authController) {
                ViewManager.setMainContent(Frames.MY_RESOURCES_VIEW_PATH, authController.mainContent, parentController);
            }else if(parentController instanceof AdminDashboardController adminController){
                ViewManager.setMainContent(Frames.TRACKS_VIEW_PATH_VIEW_PATH, adminController.mainContent, parentController);
            }
        } catch (Exception e) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
            System.err.println(e.getMessage());
        }
    }

    /**
     * Handles the add author button click.
     */
    @FXML
    private void handleAddAuthor(){
        try{
            if(authorComboBox.getEditor().getText().isEmpty())
                throw new TrackTuneException(Strings.INSERT_VALID_AUTHOR);

            String authorString = authorComboBox.getEditor().getText();

            if(SQLiteScripts.checkForSQLInjection(authorString))
                throw new TrackTuneException(Strings.ERR_SQL_INJECTION);

            if(!DatabaseManager.getDAOProvider().getAuthorDAO().existByAutorShipname(Controller.toTitleCase(authorString))){
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
                throw new AuthorAlreadyExixtsExeption(Strings.ERR_AUTHOR_ALREADY_EXISTS);
            }
        }catch (TrackTuneException e){
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.AUTHOR_FAILED, e.getMessage(), Alert.AlertType.ERROR);
        }
    }
}