package app.tracktune.controller.common;

import app.tracktune.Main;
import app.tracktune.controller.Controller;
import app.tracktune.controller.admin.AdminDashboardController;
import app.tracktune.controller.authenticatedUser.AuthenticatedUserDashboardController;
import app.tracktune.controller.authentication.SessionManager;
import app.tracktune.model.DatabaseManager;
import app.tracktune.model.author.Author;
import app.tracktune.model.resource.Resource;
import app.tracktune.model.resource.ResourceTypeEnum;
import app.tracktune.model.track.Track;
import app.tracktune.model.track.TrackAuthor;
import app.tracktune.utils.*;
import app.tracktune.view.ViewManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.*;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ResourceBundle;

public class DiscoverController extends Controller implements Initializable {
    @FXML private Tab tabMostRecent;
    @FXML private Tab tabMostPopular;
    @FXML private Tab tabMostCommented;
    @FXML private Tab tabMostCommented2;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        populateTab(tabMostRecent, SQLiteScripts.getMostRecentResources(Main.dbManager));
        populateTab(tabMostPopular, SQLiteScripts.getMostPopularResources(Main.dbManager));
        populateTab(tabMostCommented, SQLiteScripts.getMostCommentedResources(Main.dbManager));
        populateTab(tabMostCommented2, DatabaseManager.getDAOProvider().getResourceDAO().getAllCommentedResourcesByUserID(SessionManager.getInstance().getUser().getId()));
    }

    private void populateTab(Tab tab, List<Resource> resources) {
        VBox contentBox = new VBox(10);
        VBox.setVgrow(contentBox, javafx.scene.layout.Priority.ALWAYS);
        contentBox.setStyle("-fx-padding: 20;");

        for (Resource resource : resources) {
            contentBox.getChildren().add(createResourceItemBox(resource));
        }

        AnchorPane anchor = new AnchorPane();
        anchor.getChildren().add(contentBox);
        AnchorPane.setTopAnchor(contentBox, 0.0);
        AnchorPane.setLeftAnchor(contentBox, 0.0);
        AnchorPane.setRightAnchor(contentBox, 0.0);
        AnchorPane.setBottomAnchor(contentBox, 0.0);

        tab.setContent(anchor);
    }

    private HBox createResourceItemBox(Resource resource) {
        int previewWidth = 100;
        int previewHeight = 100;

        ResourceManager resourceManager = new ResourceManager(resource);
        Node preview = resourceManager.createMediaNode(previewWidth, previewHeight, true);

        HBox requestItemBox = createRequestItem(resource);

        HBox container = new HBox(15, preview, requestItemBox);
        container.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(requestItemBox, Priority.ALWAYS);
        container.getStyleClass().add("request-item");

        requestItemBox.setMaxWidth(Double.MAX_VALUE);

        return container;
    }

    private HBox createRequestItem(Resource resource) {
        Track track = DatabaseManager.getDAOProvider().getTrackDAO().getById(resource.getTrackID());
        List<TrackAuthor> trackAuthors = DatabaseManager.getDAOProvider().getTrackAuthorDAO().getByTrackId(resource.getTrackID());

        Label trackLabel = new Label(track.getTitle());
        trackLabel.getStyleClass().add("request-item-title");

        StringBuilder authorNames = new StringBuilder();
        for (TrackAuthor trackAuthor : trackAuthors) {
            Author author = DatabaseManager.getDAOProvider().getAuthorDAO().getById(trackAuthor.getAuthorId());
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

        Label descLabel = new Label(getFormattedRequestDate(resource.getCreationDate()));
        descLabel.getStyleClass().add("request-item-description");
        descLabel.setWrapText(true);

        VBox textBox = new VBox(5, titleBox, descLabel);
        textBox.setAlignment(Pos.CENTER_LEFT);
        textBox.setStyle("-fx-padding: 0 0 0 10;");
        textBox.setSpacing(15);

        Button viewBtn = new Button(Strings.VIEW);
        viewBtn.getStyleClass().add("view-button");
        viewBtn.setOnAction(e -> viewResource(resource));
        viewBtn.setMinWidth(80);

        HBox buttonBox = new HBox(10, viewBtn);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox box = new HBox(10, textBox, spacer, buttonBox);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setMaxWidth(Double.MAX_VALUE);

        textBox.maxWidthProperty().bind(descLabel.maxWidthProperty());

        return box;
    }

    @FXML
    private void viewResource(Resource resource) {
        try{
            if(resource.getType().equals(ResourceTypeEnum.link)){
                String url = new String(resource.getData(), StandardCharsets.UTF_8);
                BrowserManager.browse(url);
            }else {
                FXMLLoader loader = new FXMLLoader(this.getClass().getResource(Frames.RESOURCE_FILE_VIEW_PATH));
                loader.setControllerFactory(param -> new ResourceFileController(resource));
                Parent view = loader.load();

                Controller controller = loader.getController();
                controller.setParentController(this);

                if (parentController instanceof AuthenticatedUserDashboardController authController) {
                    authController.mainContent.getChildren().setAll(view);
                } else if (parentController instanceof AdminDashboardController adminController) {
                    adminController.mainContent.getChildren().setAll(view);
                }
            }
        }catch(Exception e){
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
            System.err.println(e.getMessage());
        }
    }
}
