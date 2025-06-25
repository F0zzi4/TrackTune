package app.tracktune.controller.admin;

import app.tracktune.controller.Controller;
import app.tracktune.exceptions.SQLInjectionException;
import app.tracktune.exceptions.TrackTuneException;
import app.tracktune.utils.DatabaseManager;
import app.tracktune.model.musicalInstrument.MusicalInstrument;
import app.tracktune.utils.SQLiteScripts;
import app.tracktune.utils.Strings;
import app.tracktune.view.ViewManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.*;

public class InstrumentsController extends Controller implements Initializable {
    /** Container for displaying the list of instruments in the UI. */
    @FXML
    private VBox instrumentsContainer;

    /** Button to navigate to the previous page of instruments. */
    @FXML
    private Button btnPrev;

    /** Button to navigate to the next page of instruments. */
    @FXML
    private Button btnNext;

    /** Button to add a new instrument. */
    @FXML
    private Button btnAddInstrument;

    /** TextField for entering the instrument's name. */
    @FXML
    private TextField txtName;

    /** TextArea for entering the instrument's description. */
    @FXML
    private TextArea txtDescription;

    /** Label showing the character count of the description. */
    @FXML
    private Label lblCharCount;

    /** List holding all musical instruments currently loaded. */
    private List<MusicalInstrument> instruments = new ArrayList<>();

    /** Current page index in the paginated instruments list. */
    private int currentPage = 0;

    /** Number of instruments displayed per page. */
    private final int itemsPerPage = 5;

    /**
     * Initializes the controller after its root element has been completely processed.
     * <p>
     * Loads the list of musical instruments from the database and sets up event handlers for UI controls:
     * - Previous and Next buttons for pagination.
     * - Add button to insert a new musical instrument after validation.
     * - Description text area listener to limit character count to 300.
     * </p>
     * <p>
     * Resets the form and refreshes the displayed list of instruments.
     * </p>
     *
     * @param location  The location used to resolve relative paths for the root object, or null if unknown.
     * @param resources The resources used to localize the root object, or null if not localized.
     */
    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        instruments = DatabaseManager.getDAOProvider().getMusicalInstrumentDAO().getAll();

        btnPrev.setOnAction(_ -> {
            if (currentPage > 0) {
                currentPage--;
                refreshInstrument();
            }
        });

        btnNext.setOnAction(_ -> {
            if ((currentPage + 1) * itemsPerPage < instruments.size()) {
                currentPage++;
                refreshInstrument();
            }
        });

        btnAddInstrument.setOnAction(_ -> {
            try {
                String name = txtName.getText().trim();
                String description = txtDescription.getText().trim();

                if (!name.isEmpty() && !description.isEmpty()) {
                    if (SQLiteScripts.checkForSQLInjection(name, description)) {
                        throw new SQLInjectionException(Strings.ERR_SQL_INJECTION);
                    }

                    boolean alreadyExists = instruments.stream()
                            .anyMatch(instr -> instr.getName().equalsIgnoreCase(name));
                    if (alreadyExists) {
                        throw new TrackTuneException(Strings.ERR_MUSICAL_INSTRUMENT_ALREADY_EXISTS);
                    }

                    MusicalInstrument newInstrument = new MusicalInstrument(Controller.toTitleCase(name), description);
                    DatabaseManager.getDAOProvider().getMusicalInstrumentDAO().insert(newInstrument);
                    instruments = DatabaseManager.getDAOProvider().getMusicalInstrumentDAO().getAll();

                    txtName.clear();
                    txtDescription.clear();
                    lblCharCount.setText("0/300");
                    currentPage = 0;
                    refreshInstrument();
                } else {
                    throw new TrackTuneException(Strings.FIELD_EMPTY);
                }
            } catch (TrackTuneException exception) {
                ViewManager.setAndShowAlert(Strings.ERROR, Strings.MUSICAL_INSTRUMENT_FAILED, exception.getMessage(), Alert.AlertType.ERROR);
            } catch (Exception ex) {
                ViewManager.setAndShowAlert(Strings.ERROR, Strings.MUSICAL_INSTRUMENT_FAILED, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
                System.err.println(ex.getMessage());
            }
        });

        txtDescription.textProperty().addListener((_, oldValue, newValue) -> {
            if (newValue.length() > 300) {
                txtDescription.setText(oldValue);
            } else {
                lblCharCount.setText(newValue.length() + "/300");
            }
        });

        refreshInstrument();
    }

    /**
     * Refreshes the list of musical instruments displayed in the UI.
     * <p>
     * Clears the current instrument container and repopulates it with the instruments on the current page,
     * respecting the pagination settings. Disables/enables the Previous and Next buttons accordingly.
     * <p>
     * If no instruments exist, shows a placeholder label indicating the list is empty.
     */
    private void refreshInstrument() {
        instrumentsContainer.getChildren().clear();

        int total = instruments.size();
        int start = currentPage * itemsPerPage;
        int end = Math.min(start + itemsPerPage, total);

        btnPrev.setDisable(currentPage == 0);
        btnNext.setDisable(end >= total);

        if (instruments.isEmpty()) {
            Label emptyLabel = new Label(Strings.EMPTY_LIST);
            emptyLabel.getStyleClass().add("empty-list-label");

            HBox emptyBox = new HBox(emptyLabel);
            emptyBox.setAlignment(Pos.CENTER);
            instrumentsContainer.getChildren().add(emptyBox);
        } else {
            List<MusicalInstrument> pageItems = instruments.subList(start, end);

            for (MusicalInstrument instrument : pageItems) {
                instrumentsContainer.getChildren().add(createInstrumentItemBox(instrument));
            }
        }
    }

    /**
     * Creates a UI component representing a single musical instrument entry.
     * <p>
     * The component consists of the instrument's name and description with a delete button.
     * The description is wrapped and sized dynamically according to available space.
     * <p>
     * The delete button triggers the deletion of the instrument from the database and UI.
     *
     * @param instrument the MusicalInstrument object to represent
     * @return an HBox containing the formatted instrument details and delete button
     */
    private HBox createInstrumentItemBox(MusicalInstrument instrument) {
        Label nameLabel = new Label(instrument.getName());
        nameLabel.getStyleClass().add("request-item-title");

        Label descLabel = new Label(instrument.getDescription());
        descLabel.getStyleClass().add("request-item-description");
        descLabel.setWrapText(true);

        VBox textBox = new VBox(5, nameLabel, descLabel);
        textBox.setAlignment(Pos.CENTER_LEFT);

        Button deleteBtn = new Button(Strings.DELETE);
        deleteBtn.getStyleClass().add("delete-button");
        deleteBtn.setOnAction(_ -> deleteMusicalInstrument(instrument));
        deleteBtn.setMinWidth(80);

        HBox buttonBox = new HBox(deleteBtn);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox box = new HBox(10, textBox, spacer, buttonBox);
        box.getStyleClass().add("request-item");
        box.setAlignment(Pos.CENTER_LEFT);

        descLabel.maxWidthProperty().bind(box.widthProperty().subtract(deleteBtn.widthProperty()).subtract(100));
        textBox.maxWidthProperty().bind(descLabel.maxWidthProperty());

        return box;
    }

    /**
     * Deletes a given musical instrument after user confirmation.
     * <p>
     * Prompts the user for confirmation before deleting the instrument from the database and removing it from the local list.
     * Adjusts the current page if necessary and refreshes the UI list of instruments.
     * <p>
     * Shows an error alert if the deletion fails.
     *
     * @param instrument the MusicalInstrument to delete
     */
    private void deleteMusicalInstrument(MusicalInstrument instrument) {
        boolean response = ViewManager.setAndGetConfirmAlert(Strings.CONFIRM_DELETION, Strings.CONFIRM_DELETION, Strings.ARE_YOU_SURE);
        if (response)
            try {
                DatabaseManager.getDAOProvider().getMusicalInstrumentDAO().deleteById(instrument.getId());
                instruments.remove(instrument);
                int maxPage = (int) Math.ceil((double) instruments.size() / itemsPerPage);
                if (currentPage >= maxPage && currentPage > 0) {
                    currentPage--;
                }
                refreshInstrument();
            } catch (Exception ex) {
                ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERR_GENERAL, ex.getMessage(), Alert.AlertType.ERROR);
            }
    }
}