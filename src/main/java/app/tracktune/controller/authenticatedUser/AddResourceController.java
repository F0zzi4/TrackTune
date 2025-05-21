package app.tracktune.controller.authenticatedUser;

import app.tracktune.controller.Controller;
import app.tracktune.exceptions.TrackTuneException;
import app.tracktune.model.author.Author;
import app.tracktune.model.author.AuthorDAO;
import app.tracktune.model.genre.Genre;
import app.tracktune.model.genre.GenreDAO;
import app.tracktune.model.musicalInstrument.MusicalInstrument;
import app.tracktune.model.musicalInstrument.MusicalInstrumentDAO;
import app.tracktune.model.resource.*;
import app.tracktune.model.track.*;
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
    @FXML private HBox durationBox;
    @FXML private HBox locationBox;
    @FXML private TextField txtDuration;
    @FXML private TextField txtLocation;
    @FXML private DatePicker resourceDate;
    @FXML private HBox resourceDateBox;
    @FXML private ComboBox<Track> trackComboBox;

    private final ResourceDAO resourceDAO = new ResourceDAO();
    private final AuthorDAO authorDAO = new AuthorDAO();
    private final GenreDAO genreDAO = new GenreDAO();
    private final MusicalInstrumentDAO musicalInstrumentDAO = new MusicalInstrumentDAO();
    private final TrackAuthorDAO trackAuthorDAO = new TrackAuthorDAO();
    private final TrackGenreDAO trackGenreDAO = new TrackGenreDAO();
    private final TrackInstrumentDAO trackInstrumentDAO = new TrackInstrumentDAO();
    private final TrackDAO trackDAO = new TrackDAO();

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
            allTracks.addAll(trackDAO.getAll());
            allAuthors.addAll(authorDAO.getAll());
            allGenres.addAll(genreDAO.getAll());
            allMusicalInstruments.addAll(musicalInstrumentDAO.getAll());

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

        List<TrackAuthor> trackAuthors = trackAuthorDAO.getByTrackId(track.getId());
        List<TrackGenre> trackGenres = trackGenreDAO.getByTrackId(track.getId());
        List<TrackInstrument> trackInstruments = trackInstrumentDAO.getByTrackId(track.getId());

        selectedAuthors.clear();
        for(TrackAuthor trackAuthor : trackAuthors){
            selectedAuthors.add(authorDAO.getById(trackAuthor.getAuthorId()));
        }
        updateSelectedElements(selectedAuthorsPane, selectedAuthors);

        selectedGenres.clear();
        for(TrackGenre trackGenre : trackGenres){
            selectedGenres.add(genreDAO.getById(trackGenre.getGenreId()));
        }
        updateSelectedElements(selectedGenresPane, selectedGenres);

        selectedInstruments.clear();
        for(TrackInstrument trackInstrument : trackInstruments){
            selectedInstruments.add(musicalInstrumentDAO.getById(trackInstrument.getInstrumentId()));
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
            durationBox.setVisible(isSelected);
            durationBox.setManaged(isSelected);
            locationBox.setVisible(isSelected);
            locationBox.setManaged(isSelected);
            resourceDateBox.setVisible(isSelected);
            resourceDateBox.setManaged(isSelected);
        });

        boolean isSelected = btnIsMultimedia.isSelected();
        durationBox.setVisible(isSelected);
        durationBox.setManaged(isSelected);
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
                trackId = trackDAO.insert(new Track(trackTitle,new Timestamp(System.currentTimeMillis()), ViewManager.getSessionUser().getId()));
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
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, e.getMessage(), Alert.AlertType.ERROR);
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
            String duration = txtDuration.getText();
            String location = txtLocation.getText();
            result = resourceDAO.insert(new MultimediaResource(type, data, new Timestamp(System.currentTimeMillis()), true,
                    Integer.parseInt(duration), location, Date.valueOf(resourceDate.getValue()), trackId));
        } else {
            result = resourceDAO.insert(new Resource(type, data, new Timestamp(System.currentTimeMillis()), false, trackId));
        }
        return result;
    }

    private void manageTrackEntity(int trackId, Integer[] authorIds, Integer[] genreIds, Integer[] instrumentIds) {
        Track track = trackDAO.getById(trackId);
        manageTrackAuthorRelation(authorIds, track.getId());
        manageTrackGenreRelation(genreIds, track.getId());
        manageTrackInstrumentRelation(instrumentIds, track.getId());
    }

    private void manageTrackAuthorRelation(Integer[] authorIds, int trackId){
        for(int authorId : authorIds)
            trackAuthorDAO.insert(new TrackAuthor(trackId, authorId));
    }

    private void manageTrackGenreRelation(Integer[] genreIds, int trackId){
        for(int genreId: genreIds)
            trackGenreDAO.insert(new TrackGenre(trackId, genreId));
    }

    private void manageTrackInstrumentRelation(Integer[] instrumentIds, int trackId){
        for(int instrumentId : instrumentIds)
            trackInstrumentDAO.insert(new TrackInstrument(trackId, instrumentId));
    }

    @FXML
    private void handleReset(){
        resetFields();
    }

    private void resetFields() {
        trackComboBox.setValue(null);
        txtFilePathField.clear();
        txtDuration.clear();
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
        durationBox.setVisible(false);
        durationBox.setManaged(false);
        selectedTrackPane.getChildren().clear();
        selectedAuthorsPane.getChildren().clear();
        selectedGenresPane.getChildren().clear();
        selectedInstrumentsPane.getChildren().clear();
    }
}