package app.tracktune.controller.authenticatedUser;

import app.tracktune.model.resource.ResourceDAO;
import io.github.palexdev.materialfx.controls.MFXToggleButton;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ResourcesController implements Initializable {
    @FXML private Button btnAddResource;
    @FXML private TextField filePathField;
    @FXML private Button btnBrowseFile;
    @FXML private ComboBox<String> authorComboBox;
    @FXML private FlowPane selectedAuthorsPane;
    @FXML private ComboBox<String> genreComboBox;
    @FXML private FlowPane selectedGenresPane;
    @FXML private MFXToggleButton btnIsMultimedia;
    @FXML private HBox durationBox;
    @FXML private HBox locationBox;
    @FXML private TextField txtDuration;
    @FXML private TextField txtLocation;

    private ResourceDAO resourceDAO;

    private final ObservableList<String> allAuthors = FXCollections.observableArrayList(
            "J.K. Rowling", "George R.R. Martin", "J.R.R. Tolkien", "Isaac Asimov", "Ursula K. Le Guin", "Brandon Sanderson"
    );

    private final ObservableList<String> allGenres = FXCollections.observableArrayList(
            "J.K. Rowling", "George R.R. Martin", "J.R.R. Tolkien", "Isaac Asimov", "Ursula K. Le Guin", "Brandon Sanderson"
    );

    private final ObservableList<String> selectedElements = FXCollections.observableArrayList();

    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {
        resourceDAO = new ResourceDAO();
        // Set list items
        authorComboBox.setItems(FXCollections.observableArrayList(allAuthors));
        authorComboBox.setEditable(true);
        genreComboBox.setItems(FXCollections.observableArrayList(allGenres));
        genreComboBox.setEditable(true);

        // Set all listeners
        setDynamicResearchListener(authorComboBox, allAuthors);
        setDynamicResearchListener(genreComboBox, allGenres);
        setAddingElementListener(authorComboBox, selectedAuthorsPane);
        setAddingElementListener(genreComboBox, selectedGenresPane);
        setSearchFileListener();
        setIsMultimediaListener();
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
                filePathField.setText(selectedFile.getAbsolutePath());
            }
        });
    }

    private void setIsMultimediaListener(){
        btnIsMultimedia.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            durationBox.setVisible(isSelected);
            durationBox.setManaged(isSelected);
            locationBox.setVisible(isSelected);
            locationBox.setManaged(isSelected);
        });

        durationBox.setVisible(btnIsMultimedia.isSelected());
        durationBox.setManaged(btnIsMultimedia.isSelected());
        locationBox.setVisible(btnIsMultimedia.isSelected());
        locationBox.setManaged(btnIsMultimedia.isSelected());
    }

    private void updateSelectedAuthors() {
        selectedAuthorsPane.getChildren().clear();
        for (String author : selectedElements) {
            Label tag = new Label(author);
            tag.getStyleClass().add("author-tag");
            selectedAuthorsPane.getChildren().add(tag);
        }
    }

    private void updateSelectedElements(FlowPane selectedElementsPane) {
        selectedElementsPane.getChildren().clear();
        for (String author : selectedElements) {
            Label tag = new Label(author);
            tag.getStyleClass().add("author-tag");
            selectedElementsPane.getChildren().add(tag);
        }
    }
}
