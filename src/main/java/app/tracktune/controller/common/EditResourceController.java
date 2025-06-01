package app.tracktune.controller.common;

import app.tracktune.controller.Controller;
import app.tracktune.controller.admin.AdminDashboardController;
import app.tracktune.controller.authenticatedUser.AuthenticatedUserDashboardController;
import app.tracktune.exceptions.AuthorAlreadyExixtsExeption;
import app.tracktune.exceptions.TrackTuneException;
import app.tracktune.model.DatabaseManager;
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
import javafx.util.StringConverter;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

public class EditResourceController extends Controller implements Initializable {
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
    @FXML private TextField trackComboBox;
    @FXML private MFXToggleButton btnIsAuthor;

    private final Resource resource;

    // Data set
    private final ObservableList<Author> allAuthors = FXCollections.observableArrayList();
    private final ObservableList<Genre> allGenres = FXCollections.observableArrayList();
    private final ObservableList<MusicalInstrument> allMusicalInstruments = FXCollections.observableArrayList();

    // Selected elements lists
    private final ObservableList<Author> selectedAuthors = FXCollections.observableArrayList();
    private final ObservableList<Genre> selectedGenres = FXCollections.observableArrayList();
    private final ObservableList<MusicalInstrument> selectedInstruments = FXCollections.observableArrayList();

    public EditResourceController(Resource resource) {this.resource = resource;}

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
            Button button = new Button(element.toString());
            button.getStyleClass().add("author-tag");
            button.setOnAction(e -> {
                selectedList.remove(element);
                updateSelectedElements(selectedElementsPane, selectedList);
            });
            FontIcon closeIcon = new FontIcon("fas-times");
            button.setGraphic(closeIcon);
            selectedElementsPane.getChildren().add(button);
        }
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
    private void handleEditResource() {
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

    private void manageResourceEntity(ResourceTypeEnum type, byte[] data, int trackId, boolean isMultimedia) {
        if (isMultimedia) {
            String location = txtLocation.getText();
            DatabaseManager.getDAOProvider().getResourceDAO().updateById(new MultimediaResource(type, data, new Timestamp(System.currentTimeMillis()), true,  location, Date.valueOf(resourceDate.getValue()), resource.isAuthor(), trackId, resource.getUserID()), resource.getId());
        } else {
            DatabaseManager.getDAOProvider().getResourceDAO().updateById(new Resource(type, data, new Timestamp(System.currentTimeMillis()), false, resource.isAuthor(), trackId, resource.getUserID()), resource.getId());
        }
    }

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

    @FXML
    private void handleReset(){
        resetFields();
    }

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
