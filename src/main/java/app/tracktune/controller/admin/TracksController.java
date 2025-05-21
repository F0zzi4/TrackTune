package app.tracktune.controller.admin;

import app.tracktune.controller.Controller;
import app.tracktune.controller.authenticatedUser.AuthenticatedUserDashboardController;
import app.tracktune.controller.authenticatedUser.EditResourceController;
import app.tracktune.controller.authenticatedUser.ResourceFileController;
import app.tracktune.model.author.Author;
import app.tracktune.model.author.AuthorDAO;
import app.tracktune.model.genre.Genre;
import app.tracktune.model.genre.GenreDAO;
import app.tracktune.model.musicalInstrument.MusicalInstrument;
import app.tracktune.model.musicalInstrument.MusicalInstrumentDAO;
import app.tracktune.model.resource.Resource;
import app.tracktune.model.resource.ResourceDAO;
import app.tracktune.model.track.*;
import app.tracktune.utils.Frames;
import app.tracktune.utils.Strings;
import app.tracktune.view.ViewManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.util.StringConverter;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;


public class TracksController extends Controller implements Initializable {
    @FXML private VBox resourcesContainer;
    @FXML private Button btnPrev;
    @FXML private Button btnNext;
    @FXML private ComboBox filterTypeComboBox;
    @FXML private HBox filterControlsContainer;

    private List<Track> allTracks = new ArrayList<>();
    private List<Track> filteredTracks = new ArrayList<>();
    private int currentPage = 0;
    private final int itemsPerPage = 6;
    private final ResourceDAO resourceDAO = new ResourceDAO();
    private final TrackDAO trackDAO = new TrackDAO();
    private final TrackGenreDAO trackGenreDAO = new TrackGenreDAO();
    private final MusicalInstrumentDAO instrumentDAO = new MusicalInstrumentDAO();
    private final TrackAuthorDAO trackAuthorDAO = new TrackAuthorDAO();
    private final AuthorDAO authorDAO = new AuthorDAO();
    private final GenreDAO genreDAO = new GenreDAO();

    protected Resource resource;

    private final Map<String, String> filterOptions = Map.ofEntries(
            Map.entry("All", "mdi2f-format-list-bulleted"),
            Map.entry("Author", "mdi2a-account-music"),
            Map.entry("Genre", "mdi2m-music-note"),
            Map.entry("Instrument", "mdi2g-guitar-electric"),
            Map.entry("Title", "mdi2f-format-title")
    );

    @Override
    public void initialize(URL location, ResourceBundle res) {

        allTracks = trackDAO.getAll();
        filteredTracks = new ArrayList<>(allTracks);

        setUpFilterComboBox();

        btnPrev.setOnAction(e -> {
            if (currentPage > 0) {
                currentPage--;
                updateTracks();
            }
        });

        btnNext.setOnAction(e -> {
            if ((currentPage + 1) * itemsPerPage < allTracks.size()) {
                currentPage++;
                updateTracks();
            }
        });

        updateTracks();
    }

    private void setUpFilterComboBox() {
        ObservableList<String> filterTypes = FXCollections.observableArrayList();
        filterTypes.add("All"); // "All" sempre in cima
        filterTypes.addAll(filterOptions.keySet());
        filterTypeComboBox.setItems(filterTypes);

        filterTypeComboBox.setCellFactory(cb -> new ListCell<String>() {
            private final FontIcon icon = new FontIcon();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    icon.setIconLiteral(filterOptions.getOrDefault(item, "mdi2f-filter"));
                    icon.setIconSize(16);
                    setText(item);
                    setGraphic(icon);
                }
            }
        });

        filterTypeComboBox.setButtonCell(new ListCell<String>() {
            private final FontIcon icon = new FontIcon();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    icon.setIconLiteral(filterOptions.getOrDefault(item, "mdi2f-filter"));
                    icon.setIconSize(16);
                    setText(item);
                    setGraphic(icon);
                }
            }
        });

        filterTypeComboBox.setValue("All");

        filterTypeComboBox.setOnAction(event -> {
            filteredTracks = new ArrayList<>(allTracks);
            filterResourcesBy(filterTypeComboBox.getValue().toString());
        });
    }

    private void filterResourcesBy(String filter) {
        currentPage = 0;

        filterControlsContainer.getChildren().remove(1, filterControlsContainer.getChildren().size());

        switch (filter) {
            case "Author" -> filterByAuthor();
            case "Genre" -> filterByGenre();
            case "Instrument" -> filterByInstrument();
            case "Title" -> filterByTitle();
            default -> filteredTracks = new ArrayList<>(allTracks);
        }

        updateTracks();
    }

    private void filterByTitle() {
        filterControlsContainer.getChildren().removeIf(node -> node instanceof TextField);

        TextField textField = new TextField();
        textField.setPromptText("Enter title...");
        textField.getStyleClass().add("textField");

        textField.setOnKeyReleased(e -> {
            String input = textField.getText().toLowerCase();
            filteredTracks = allTracks.stream()
                    .filter(t -> t.getTitle().toLowerCase().contains(input))
                    .collect(Collectors.toList());
            updateTracks();
        });

        filterControlsContainer.getChildren().add(textField);
    }

    private void filterByAuthor() {
        createComboBoxFilter(authorDAO.getAll());
    }

    private void filterByGenre() {
        createComboBoxFilter(genreDAO.getAllUsed());
    }

    private void filterByInstrument() {
        createComboBoxFilter(instrumentDAO.getAll());
    }

    private <T> void createComboBoxFilter(List<T> allItems) {
        int count = 0;
        for (Node node : filterControlsContainer.getChildren()) {
            if (node instanceof ComboBox) {
                count ++;
                if(count > 1)
                    return;
            }
        }

        ComboBox<T> comboBox = new ComboBox<>();
        comboBox.setEditable(true);
        ObservableList<T> observableItems = FXCollections.observableArrayList(allItems);
        comboBox.setItems(observableItems);
        comboBox.setConverter(new EntityToStringConverter<>());
        setDynamicResearchListener(comboBox, observableItems);
        setAddingElementListener(comboBox, observableItems);

        filterControlsContainer.getChildren().add(comboBox);
    }


    @FXML
    private void viewResource() {
        try{
            if(parentController instanceof AuthenticatedUserDashboardController authController){
                FXMLLoader loader = new FXMLLoader(this.getClass().getResource(Frames.RESOURCE_FILE_VIEW_PATH));
                loader.setControllerFactory(param -> new ResourceFileController(resource));
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

    @FXML
    private void editResource() {
        try{
            if(parentController instanceof AuthenticatedUserDashboardController authController){
                FXMLLoader loader = new FXMLLoader(this.getClass().getResource(Frames.EDIT_RESOURCES_VIEW_PATH));
                loader.setControllerFactory(param -> new EditResourceController(resource));
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

    private void updateTracks() {
        resourcesContainer.getChildren().clear();

        int total = filteredTracks.size();
        int start = currentPage * itemsPerPage;
        int end = Math.min(start + itemsPerPage, total);

        btnPrev.setDisable(currentPage == 0);
        btnNext.setDisable(end >= total);

        if (filteredTracks.isEmpty()) {
            Label emptyLabel = new Label(Strings.EMPTY_LIST);
            emptyLabel.getStyleClass().add("empty-list-label");

            HBox emptyBox = new HBox(emptyLabel);
            emptyBox.setAlignment(Pos.CENTER);
            resourcesContainer.getChildren().add(emptyBox);
        } else {
            List<Track> pageItems = filteredTracks.subList(start, end);
            for (Track track : pageItems) {
                HBox itemBox = createTrackItemBox(track);
                resourcesContainer.getChildren().add(itemBox);
            }
        }
    }

    private HBox createTrackItemBox(Track track) {
        HBox requestItemBox = createRequestItem(track);

        HBox container = new HBox(15,requestItemBox);
        container.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(requestItemBox, Priority.ALWAYS);
        container.getStyleClass().add("request-item");

        requestItemBox.setMaxWidth(Double.MAX_VALUE);

        return container;
    }

    private HBox createRequestItem(Track element) {
        List<TrackAuthor> trackAuthors = trackAuthorDAO.getByTrackId(element.getId());

        Label trackLabel = new Label(element.getTitle());
        trackLabel.getStyleClass().add("request-item-title");

        StringBuilder authorNames = new StringBuilder();
        for (TrackAuthor trackAuthor : trackAuthors) {
            Author author = authorDAO.getById(trackAuthor.getAuthorId());
            authorNames.append(author.getAuthorshipName()).append(", ");
        }

        if (!authorNames.isEmpty()) {
            authorNames.setLength(authorNames.length() - 2);
        }

        Label authorsLabel = new Label("Authors: " + authorNames);
        authorsLabel.getStyleClass().add("request-item-authors");

        HBox titleBox = new HBox(10, trackLabel, authorsLabel);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        titleBox.setStyle("-fx-padding: 0 0 0 15;");
        titleBox.setSpacing(15);

        Label descLabel = new Label(getFormattedRequestDate(element.getCreationDate()));
        descLabel.getStyleClass().add("request-item-description");
        descLabel.setWrapText(true);

        VBox textBox = new VBox(5, titleBox, descLabel);
        textBox.setAlignment(Pos.CENTER_LEFT);
        textBox.setStyle("-fx-padding: 0 0 0 10;");
        textBox.setSpacing(15);


        Button deleteBtn = new Button(Strings.DELETE);
        deleteBtn.getStyleClass().add("reject-button");
        deleteBtn.setOnAction(e -> deleteResource());
        deleteBtn.setMinWidth(80);


        Button viewBtn = new Button(Strings.VIEW);
        viewBtn.getStyleClass().add("view-button");
        viewBtn.setOnAction(e -> viewResource());
        viewBtn.setMinWidth(80);

        HBox buttonBox = new HBox(10, viewBtn, deleteBtn);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox box = new HBox(10, textBox, spacer, buttonBox);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setMaxWidth(Double.MAX_VALUE);

        descLabel.maxWidthProperty().bind(box.widthProperty().subtract(deleteBtn.widthProperty()).subtract(50));
        textBox.maxWidthProperty().bind(descLabel.maxWidthProperty());

        return box;
    }

    private void deleteResource() {
        boolean response = ViewManager.setAndGetConfirmAlert(Strings.CONFIRM_DELETION, Strings.CONFIRM_DELETION, Strings.ARE_YOU_SURE);
        if (response)
            try {
                resourceDAO.deleteById(resource.getId());
                allTracks.remove(resource);
                int maxPage = (int) Math.ceil((double) allTracks.size() / itemsPerPage);
                if (currentPage >= maxPage && currentPage > 0) {
                    currentPage--;
                }
                updateTracks();
            } catch (Exception ex) {
                ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERR_GENERAL, ex.getMessage(), Alert.AlertType.ERROR);
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

    private <T> void setAddingElementListener(ComboBox<T> comboBox, ObservableList<T> selectedElements) {
        comboBox.setOnAction(e -> {
            T selected = comboBox.getValue();
            if (selected != null) {
                if(selected instanceof Author author){
                    filteredTracks.clear();
                    filteredTracks.addAll(trackDAO.getAllByAuthorId(author.getId()));
                    updateTracks();
                }
                else if(selected instanceof Genre genre){
                    filteredTracks.clear();
                    filteredTracks.addAll(trackDAO.getAllByTrackId(genre.getId()));
                    updateTracks();
                }else if(selected instanceof MusicalInstrument instrument){
                    filteredTracks.clear();
                    filteredTracks.addAll(trackDAO.getAllByInstrumentId(instrument.getId()));
                    updateTracks();
                }


                /*
                    trackAuthorDAO.add(new TrackAuthor(trackDAO.getById(resource.getTrackID()).getId(), author.getId()));
                else if(selected instanceof Genre genre)
                    trackGenreDAO.add(new TrackGenre(trackDAO.getById(resource.getTrackID()).getId(), genre.getId()));
                else if(selected instanceof MusicalInstrument instrument)
                    instrumentDAO.add(new TrackInstrument(trackDAO.getById(resource.getTrackID()).getId(), instrument.getId()));
                selectedElements.add(selected);*/
            }
            comboBox.getEditor().clear();
            comboBox.setItems(comboBox.getItems());
        });
    }

}
