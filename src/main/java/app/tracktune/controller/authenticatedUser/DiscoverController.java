package app.tracktune.controller.authenticatedUser;

import app.tracktune.controller.Controller;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class DiscoverController extends Controller implements Initializable {
    @FXML private Tab tabMostRecent;
    @FXML private Tab tabPopular;
    @FXML private Tab tabUnpopular;
    @FXML private Button btnSearch;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        populateTab(tabMostRecent, "Most Recent", "These are the most recent posts.");
        populateTab(tabPopular, "Popular", "These are the most popular posts.");
        populateTab(tabUnpopular, "Unpopular", "These are the least liked posts.");
    }

    private void populateTab(Tab tab, String title, String description) {
        VBox contentBox = new VBox(10);
        VBox.setVgrow(contentBox, javafx.scene.layout.Priority.ALWAYS);
        contentBox.setStyle("-fx-padding: 20;");

        for (int i = 1; i <= 5; i++) {
            Label item = new Label(title + " item " + i + " - " + description);
            item.maxWidthProperty().bind(contentBox.widthProperty());
            item.getStyleClass().add("tab-item");
            contentBox.getChildren().add(item);
        }

        AnchorPane anchor = new AnchorPane();
        anchor.getChildren().add(contentBox);
        AnchorPane.setTopAnchor(contentBox, 0.0);
        AnchorPane.setLeftAnchor(contentBox, 0.0);
        AnchorPane.setRightAnchor(contentBox, 0.0);
        AnchorPane.setBottomAnchor(contentBox, 0.0);

        tab.setContent(anchor);
    }
}
