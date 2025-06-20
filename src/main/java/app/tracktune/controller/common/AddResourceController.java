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
    @FXML private TextField txtFilePath;
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
    @FXML private MFXToggleButton btnIsLink;
    @FXML private HBox resourceLinkBox;
    @FXML private TextField txtResourceLink;
    @FXML private HBox filePathBox;
    @FXML private MFXToggleButton btnIsAuthor;

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
            setIsLinkListener();
        } catch (Exception e) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERR_GENERAL, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
            System.err.println(e.getMessage());
        }
    }

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

    private void setIsMultimediaListener() {
        btnIsMultimedia.selectedProperty().addListener((_, _, isSelected) -> setMultimediaAssociatedControls(isSelected));

        boolean isSelected = btnIsMultimedia.isSelected();
        setMultimediaAssociatedControls(isSelected);
    }

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

    private void setIsLinkListener() {
        btnIsLink.selectedProperty().addListener((_, _, isSelected) -> setLinkAssociatedControls(isSelected));

        boolean isSelected = btnIsLink.isSelected();
        setLinkAssociatedControls(isSelected);
    }

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
        if(btnIsLink.isSelected()){
            return ResourceTypeEnum.link;
        }else{
            String fileName = txtFilePath.getText();
            String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
            return ResourceTypeEnum.valueOf(extension.toLowerCase());
        }
    }

    private Integer manageResourceEntity(ResourceTypeEnum type, byte[] data, int trackId, boolean isMultimedia) {
        Resource resource;
        if (isMultimedia) {
            String location = txtLocation.getText();
            resource = new MultimediaResource(type, data, new Timestamp(System.currentTimeMillis()), true,
                    location, Date.valueOf(resourceDate.getValue()), btnIsAuthor.isSelected(), trackId, SessionManager.getInstance().getUser().getId());
        } else {
            resource = new Resource(type, data, new Timestamp(System.currentTimeMillis()), false, btnIsAuthor.isSelected(),trackId, SessionManager.getInstance().getUser().getId());
        }
        return DatabaseManager.getDAOProvider().getResourceDAO().insert(resource);
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
     * Handles the return button click, going back to the previous view.
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