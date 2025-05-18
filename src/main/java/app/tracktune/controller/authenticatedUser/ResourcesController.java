package app.tracktune.controller.authenticatedUser;

import app.tracktune.controller.Controller;
import app.tracktune.model.resource.Resource;
import app.tracktune.model.resource.ResourceDAO;
import app.tracktune.model.track.Track;
import app.tracktune.model.track.TrackDAO;
import app.tracktune.utils.Frames;
import app.tracktune.utils.SQLiteScripts;
import app.tracktune.utils.Strings;
import app.tracktune.view.ViewManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ResourcesController extends Controller implements Initializable {
    @FXML private VBox resourcesContainer;
    @FXML private Button btnPrev;
    @FXML private Button btnNext;

    private List<Resource> resources = new ArrayList<>();
    private int currentPage = 0;
    private final int itemsPerPage = 6;
    private final ResourceDAO resourceDAO = new ResourceDAO();
    private final TrackDAO trackDAO = new TrackDAO();

    @Override
    public void initialize(URL location, ResourceBundle res) {
        resources = resourceDAO.getAll();

        btnPrev.setOnAction(e -> {
            if (currentPage > 0) {
                currentPage--;
                updateResources();
            }
        });

        btnNext.setOnAction(e -> {
            if ((currentPage + 1) * itemsPerPage < resources.size()) {
                currentPage++;
                updateResources();
            }
        });

        updateResources();
    }

    @FXML
    private void handleAddResource() {
        try{
            if(parentController instanceof AuthenticatedUserDashboardController authController){
                ViewManager.setMainContent(Frames.ADD_RESOURCES_VIEW_PATH, authController.mainContent, this);
            }
        }catch(Exception e){
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
            System.err.println(e.getMessage());
        }
    }

    private void updateResources() {
        resourcesContainer.getChildren().clear();

        int total = resources.size();
        int start = currentPage * itemsPerPage;
        int end = Math.min(start + itemsPerPage, total);

        btnPrev.setDisable(currentPage == 0);
        btnNext.setDisable(end >= total);

        if (resources.isEmpty()) {
            Label emptyLabel = new Label(Strings.EMPTY_LIST);
            emptyLabel.getStyleClass().add("empty-list-label");

            HBox emptyBox = new HBox(emptyLabel);
            emptyBox.setAlignment(Pos.CENTER);
            resourcesContainer.getChildren().add(emptyBox);
        } else {
            List<Resource> pageItems = resources.subList(start, end);
            for (Resource resource : pageItems) {
                Node preview = createPreview(resource);
                resourcesContainer.getChildren().add(createRequestItem(resource));
                resourcesContainer.getChildren().add(preview);
            }
        }
    }

    private HBox createRequestItem(Resource resource) {
        Track track = trackDAO.getById(resource.getTrackID());
        Label nameLabel = new Label(track.getTitle());
        nameLabel.getStyleClass().add("request-item-title");

        Label descLabel = new Label(SQLiteScripts.getFormattedRequestDate(resource.getCreationDate()));
        descLabel.getStyleClass().add("request-item-description");
        descLabel.setWrapText(true);

        VBox textBox = new VBox(5, nameLabel, descLabel);
        textBox.setAlignment(Pos.CENTER_LEFT);

        Button deleteBtn = new Button(Strings.DELETE);
        deleteBtn.getStyleClass().add("reject-button");
        deleteBtn.setOnAction(e -> deleteResource(resource));
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

    private void deleteResource(Resource resource) {
        try {
            resourceDAO.deleteById(resource.getId());
            resources.remove(resource);
            int maxPage = (int) Math.ceil((double) resources.size() / itemsPerPage);
            if (currentPage >= maxPage && currentPage > 0) {
                currentPage--;
            }
            updateResources();
        } catch (Exception ex) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERR_GENERAL, ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private Node createPreview(Resource resource) {
        String mimeType = resource.getType().toString();

        if (mimeType.startsWith("png")) {
            ByteArrayInputStream bis = new ByteArrayInputStream(resource.getData());
            Image image = new Image(bis, 100, 100, true, true);
            return new ImageView(image);
        }

        if (mimeType.startsWith("video")) {
            FontIcon videoIcon = new FontIcon("mdi2v-video");
            videoIcon.setIconSize(60);
            return videoIcon;
        }

        if (mimeType.startsWith("audio")) {
            FontIcon audioIcon = new FontIcon("mdi2m-music");
            audioIcon.setIconSize(60);
            return audioIcon;
        }

        FontIcon fileIcon = new FontIcon("mdi2f-file");
        fileIcon.setIconSize(60);
        return fileIcon;
    }

}
