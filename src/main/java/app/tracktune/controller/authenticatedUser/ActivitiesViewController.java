package app.tracktune.controller.authenticatedUser;

import app.tracktune.controller.Controller;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class ActivitiesViewController extends Controller  implements Initializable {
    @FXML private VBox authorsContainer;
    @FXML private Button prevButton;
    @FXML private Button nextButton;

    public void initialize(URL location, ResourceBundle resources) {

    }
}
