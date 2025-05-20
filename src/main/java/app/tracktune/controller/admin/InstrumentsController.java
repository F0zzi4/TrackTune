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

public class InstrumentsController extends Controller implements Initializable {

    @FXML private VBox instrumentsContainer;
    @FXML private Button btnPrev;
    @FXML private Button btnNext;
    @FXML private Button btnAddInstrument;
    @FXML private TextField txtName;
    @FXML private TextArea txtDescription;
    @FXML private Label lblCharCount;

    private List<MusicalInstrument> instruments = new ArrayList<>();
    private int currentPage = 0;
    private final int itemsPerPage = 4;
    private final MusicalInstrumentDAO instrumentDAO = new MusicalInstrumentDAO();

    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        instruments = instrumentDAO.getAll();

        btnPrev.setOnAction(e -> {
            if (currentPage > 0) {
                currentPage--;
                refreshInstrument();
            }
        });

        btnNext.setOnAction(e -> {
            if ((currentPage + 1) * itemsPerPage < instruments.size()) {
                currentPage++;
                refreshInstrument();
            }
        });

        btnAddInstrument.setOnAction(e -> {
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

                    MusicalInstrument newInstrument = new MusicalInstrument(name, description);
                    instrumentDAO.insert(newInstrument);
                    instruments = instrumentDAO.getAll();

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

        txtDescription.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 300) {
                txtDescription.setText(oldValue);
            } else {
                lblCharCount.setText(newValue.length() + "/300");
            }
        });

        refreshInstrument();
    }

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

    private void deleteMusicalInstrument(MusicalInstrument instrument) {
        boolean response = ViewManager.setAndGetConfirmAlert(Strings.CONFIRM_DELETION, Strings.CONFIRM_DELETION, Strings.ARE_YOU_SURE);
        if (response)
            try {
                instrumentDAO.deleteById(instrument.getId());
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