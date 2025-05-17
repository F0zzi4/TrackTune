package app.tracktune.controller.admin;

import app.tracktune.controller.Controller;
import app.tracktune.exceptions.SQLInjectionException;
import app.tracktune.exceptions.TrackTuneException;
import app.tracktune.model.musicalInstrument.MusicalInstrument;
import app.tracktune.model.musicalInstrument.MusicalInstrumentDAO;
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

/**
 * Controller for managing musical instruments in the admin panel.
 * <p>
 * Provides functionality for administrators to:
 * <ul>
 *     <li>Add new musical instruments</li>
 *     <li>View instruments in a paginated list</li>
 *     <li>Delete instruments from the database</li>
 * </ul>
 * </p>
 * <p>
 * This controller interacts with {@link MusicalInstrumentDAO} for data operations
 * and ensures input validation and SQL injection protection.
 * </p>
 */
public class InstrumentsController extends Controller implements Initializable {

    @FXML private VBox requestsContainer;
    @FXML private Button btnPrev;
    @FXML private Button btnNext;
    @FXML private Button btnAddInstrument;
    @FXML private TextField txtName;
    @FXML private TextArea txtDescription;
    @FXML private Label lblCharCount;

    private SortedSet<MusicalInstrument> instrumentsList = new TreeSet<>();
    private int currentPage = 0;
    private final int itemsPerPage = 4;
    private final MusicalInstrumentDAO instrumentDAO = new MusicalInstrumentDAO();

    /**
     * Initializes the controller and loads all instruments from the database.
     * <p>
     * Sets up pagination controls, adds event handlers for button actions,
     * and configures the character counter for the description field (limited to 300 characters).
     * </p>
     */
    @FXML
    public void initialize(URL location, ResourceBundle resources) {

            instrumentsList.addAll(instrumentDAO.getAll());

        btnPrev.setOnAction(e -> {
            if (currentPage > 0) {
                currentPage--;
                refreshInstrument();
            }
        });

        btnNext.setOnAction(e -> {
            if ((currentPage + 1) * itemsPerPage < instrumentsList.size()) {
                currentPage++;
                refreshInstrument();
            }
        });

        btnAddInstrument.setOnAction(e -> {
            try{
                String name = txtName.getText().trim();
                String description = txtDescription.getText().trim();

                if (!name.isEmpty() && !description.isEmpty()) {
                    if(SQLiteScripts.checkForSQLInjection(name, description))
                        throw new SQLInjectionException(Strings.ERR_SQL_INJECTION);

                    if (instrumentsList.stream().noneMatch(g -> g.getName().equalsIgnoreCase(name))) {
                        MusicalInstrument newInstrument = new MusicalInstrument(name, description);
                        instrumentDAO.insert(newInstrument);
                        instrumentsList.add(newInstrument);
                        txtName.clear();
                        txtDescription.clear();
                        lblCharCount.setText("0/300");
                        refreshInstrument();
                    } else {
                        throw new TrackTuneException(Strings.ERR_MUSICAL_INSTRUMENT_ALREADY_EXISTS);
                    }
                }
                else
                    throw new TrackTuneException(Strings.FIELD_EMPTY);
            }catch (TrackTuneException exception){
                ViewManager.setAndShowAlert(Strings.ERROR, Strings.MUSICAL_INSTRUMENT_FAILED, exception.getMessage(), Alert.AlertType.ERROR);
            }catch(Exception ex){
                ViewManager.setAndShowAlert(Strings.ERROR, Strings.MUSICAL_INSTRUMENT_FAILED, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
                System.err.println(ex.getMessage());
            }
        });

        txtDescription.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 300) {
                txtDescription.setText(oldValue);
            } else {
                lblCharCount.setText(newValue.length() + "/300");
            }
        });

        refreshInstrument();
    }

    /**
     * Updates the instrument list display based on the current pagination state.
     * <p>
     * Clears the current view and loads a subset of instruments corresponding
     * to the current page. Handles enabling/disabling pagination buttons based on the list size.
     * If the list is empty, displays an appropriate message.
     * </p>
     */
    private void refreshInstrument() {
        requestsContainer.getChildren().clear();

        int totalRequests =  instrumentsList.size();
        int start = currentPage * itemsPerPage;
        int end = Math.min(start + itemsPerPage, totalRequests);

        btnPrev.setDisable(currentPage == 0);
        btnNext.setDisable(end >= totalRequests);

        List<MusicalInstrument> pageItems = new ArrayList<>(instrumentsList).subList(start, end);

        if(instrumentsList.isEmpty()) {
            Label emptyLabel = new Label(Strings.EMPTY_LIST);
            emptyLabel.getStyleClass().add("empty-list-label");

            HBox emptyBox = new HBox(emptyLabel);
            emptyBox.setAlignment(Pos.CENTER);
            requestsContainer.getChildren().add(emptyBox);
        }
        else{
            for (MusicalInstrument instrument : pageItems) {
                HBox requestBox = createInstrumentItemBox(instrument);
                requestsContainer.getChildren().add(requestBox);
            }
        }
    }

    /**
     * Creates a graphical representation (HBox) for a single musical instrument item.
     * <p>
     * Includes the instrument's name, description, and a delete button.
     * The layout and style classes are applied for consistent UI.
     * </p>
     *
     * @param instrument the {@link MusicalInstrument} to display
     * @return an {@link HBox} containing the instrument's name, description, and delete action
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
        deleteBtn.getStyleClass().add("reject-button");
        deleteBtn.setOnAction(e -> deleteMusicalInstrument(instrument));
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
     * Deletes a musical instrument from the database and updates the view.
     * <p>
     * Handles exceptions and shows alerts in case of failure. After deletion,
     * calls a method to refresh the view and update pagination.
     * </p>
     *
     * @param instrument the {@link MusicalInstrument} to delete
     */
    private void deleteMusicalInstrument(MusicalInstrument instrument){
        try {
            instrumentDAO.delete(instrument);
            removeInstrumentAndUpdate(instrument);
        } catch (Exception ex) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERR_GENERAL, ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Removes the specified instrument from the local list and updates pagination state.
     * <p>
     * If the current page becomes invalid due to the deletion (e.g., last item on the last page),
     * it adjusts the current page accordingly and refreshes the list.
     * </p>
     *
     * @param instrument the {@link MusicalInstrument} to remove
     */
    private void removeInstrumentAndUpdate(MusicalInstrument instrument) {
         instrumentsList.remove(instrument);
        int maxPage = (int) Math.ceil((double)  instrumentsList.size() / itemsPerPage);
        if (currentPage >= maxPage && currentPage > 0) {
            currentPage--;
        }
        refreshInstrument();
    }
}
