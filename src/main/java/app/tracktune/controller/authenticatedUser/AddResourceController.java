package app.tracktune.controller.authenticatedUser;

import app.tracktune.controller.Controller;
import app.tracktune.exceptions.TrackTuneException;
import app.tracktune.model.author.AuthorDAO;
import app.tracktune.model.genre.GenreDAO;
import app.tracktune.model.resource.MultimediaResource;
import app.tracktune.model.resource.Resource;
import app.tracktune.model.resource.ResourceDAO;
import app.tracktune.model.resource.ResourceTypeEnum;
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

import javax.sql.rowset.serial.SerialBlob;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.Blob;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AddResourceController extends Controller implements Initializable{
    @FXML private TextField txtFilePathField;
    @FXML private Button btnBrowseFile;
    @FXML private ComboBox<String> authorComboBox;
    @FXML private FlowPane selectedAuthorsPane;
    @FXML private ComboBox<String> genreComboBox;
    @FXML private FlowPane selectedGenresPane;
    @FXML private ComboBox<String> instrumentComboBox;
    @FXML private FlowPane selectedInstrumentsPane;
    @FXML private MFXToggleButton btnIsMultimedia;
    @FXML private HBox durationBox;
    @FXML private HBox locationBox;
    @FXML private TextField txtDuration;
    @FXML private TextField txtLocation;
    @FXML private DatePicker resourceDate;
    @FXML private HBox resourceDateBox;
    @FXML private TextField txtTrack;
    private ResourceDAO resourceDAO;
    private AuthorDAO authorDAO;
    private GenreDAO genreDAO;

    private final ObservableList<String> allAuthors = FXCollections.observableArrayList(
            "J.K. Rowling", "George R.R. Martin", "J.R.R. Tolkien", "Isaac Asimov", "Ursula K. Le Guin", "Brandon Sanderson"
    );

    private final ObservableList<String> allGenres = FXCollections.observableArrayList(
            "J.K. Rowling", "George R.R. Martin", "J.R.R. Tolkien", "Isaac Asimov", "Ursula K. Le Guin", "Brandon Sanderson"
    );

    private final ObservableList<String> allInstruments = FXCollections.observableArrayList(
            "J.K. Rowling", "George R.R. Martin", "J.R.R. Tolkien", "Isaac Asimov", "Ursula K. Le Guin", "Brandon Sanderson"
    );

    private final ObservableList<String> selectedElements = FXCollections.observableArrayList();

    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try{
            // Init DAOs
            resourceDAO = new ResourceDAO();
            authorDAO = new AuthorDAO();
            genreDAO = new GenreDAO();

            // Set list items
            authorComboBox.setItems(FXCollections.observableArrayList(allAuthors));
            authorComboBox.setEditable(true);
            genreComboBox.setItems(FXCollections.observableArrayList(allGenres));
            genreComboBox.setEditable(true);
            instrumentComboBox.setItems(FXCollections.observableArrayList(allInstruments));
            instrumentComboBox.setEditable(true);

            // Set all listeners
            setDynamicResearchListener(authorComboBox, allAuthors);
            setDynamicResearchListener(genreComboBox, allGenres);
            setDynamicResearchListener(instrumentComboBox, allInstruments);
            setAddingElementListener(authorComboBox, selectedAuthorsPane);
            setAddingElementListener(genreComboBox, selectedGenresPane);
            setAddingElementListener(instrumentComboBox, selectedInstrumentsPane);
            setSearchFileListener();
            setIsMultimediaListener();
        }catch(Exception e){
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERR_GENERAL, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
            System.err.println(e.getMessage());
        }
    }

    private void setDynamicResearchListener(ComboBox<String> comboBox, ObservableList<String> allElements){
        comboBox.getEditor().addEventFilter(KeyEvent.KEY_RELEASED, e -> {
            String input = comboBox.getEditor().getText().toLowerCase();
            List<String> filtered = allElements.stream()
                    .filter(author -> author.toLowerCase().contains(input))
                    .collect(Collectors.toList());
            comboBox.setItems(FXCollections.observableArrayList(filtered));
            comboBox.show();
        });
    }

    private void setAddingElementListener(ComboBox<String> comboBox, FlowPane selectedElementsPane){
        comboBox.setOnAction(e -> {
            String selected = comboBox.getValue();
            if (selected != null && !selected.isBlank() && !selectedElements.contains(selected)) {
                selectedElements.add(selected);
                updateSelectedElements(selectedElementsPane);
            }
            comboBox.getEditor().clear();
        });
    }

    private void setSearchFileListener(){
        btnBrowseFile.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Audio File");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav", "*.mp4","*.pdf","*.midi", "*.jpg", "*.png"),
                    new FileChooser.ExtensionFilter("All Files", "*.*")
            );
            File selectedFile = fileChooser.showOpenDialog(btnBrowseFile.getScene().getWindow());
            if (selectedFile != null) {
                txtFilePathField.setText(selectedFile.getAbsolutePath());
            }
        });
    }

    private void setIsMultimediaListener(){
        btnIsMultimedia.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            durationBox.setVisible(isSelected);
            durationBox.setManaged(isSelected);
            locationBox.setVisible(isSelected);
            locationBox.setManaged(isSelected);
            resourceDateBox.setVisible(isSelected);
            resourceDateBox.setManaged(isSelected);
        });

        durationBox.setVisible(btnIsMultimedia.isSelected());
        durationBox.setManaged(btnIsMultimedia.isSelected());
        locationBox.setVisible(btnIsMultimedia.isSelected());
        locationBox.setManaged(btnIsMultimedia.isSelected());
        resourceDateBox.setVisible(btnIsMultimedia.isSelected());
        resourceDateBox.setManaged(btnIsMultimedia.isSelected());
    }

    private void updateSelectedElements(FlowPane selectedElementsPane) {
        selectedElementsPane.getChildren().clear();
        for (String author : selectedElements) {
            Label tag = new Label(author);
            tag.getStyleClass().add("author-tag");
            selectedElementsPane.getChildren().add(tag);
        }
    }

    @FXML
    private void handleAddResource() {
        try {
            if(!checkInput()){
                throw new TrackTuneException(Strings.FIELD_EMPTY);
            }

            ResourceTypeEnum type = ResourceTypeEnum.mp3;

            ObservableList<String> authorsList = authorComboBox.getItems();
            String[] authors = authorsList.toArray(new String[0]);

            ObservableList<String> genresList = genreComboBox.getItems();
            String[] genres = genresList.toArray(new String[0]);

            ObservableList<String> instrumentsList = instrumentComboBox.getItems();
            String[] instruments = instrumentsList.toArray(new String[0]);

            String filePath = txtFilePathField.getText();
            Blob data = createBlobFromFile(filePath);

            boolean isMultimedia = btnIsMultimedia.isSelected();

            if (isMultimedia) {
                String duration = txtDuration.getText();
                String location = txtLocation.getText();

                resourceDAO.insert(new MultimediaResource(type, data, new Timestamp(System.currentTimeMillis()), true, Integer.parseInt(duration), location, Date.valueOf(resourceDate.getValue()), 1));
            }else
                resourceDAO.insert(new Resource(type, data, new Timestamp(System.currentTimeMillis()), false, 1));
        }
        catch(TrackTuneException e){
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, e.getMessage(), Alert.AlertType.ERROR);
        }
        catch (Exception e) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
            System.err.println(e.getMessage());
        }
    }

    private boolean checkInput() {
        try{
            if (authorComboBox.getItems().isEmpty()) {
                return false;
            }

            if (genreComboBox.getItems().isEmpty()) {
                return false;
            }
            return txtFilePathField.getText() != null && !txtFilePathField.getText().trim().isEmpty();
        }catch(Exception e){
            return false;
        }
    }

    public Blob createBlobFromFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            throw new IOException(Strings.ERR_FILE_NOT_FOUND);
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] fileBytes = fis.readAllBytes();
            return new SerialBlob(fileBytes);
        } catch (SQLException | IOException e) {
            throw new TrackTuneException(Strings.ERR_LOAD_FILE);
        }
    }
}
