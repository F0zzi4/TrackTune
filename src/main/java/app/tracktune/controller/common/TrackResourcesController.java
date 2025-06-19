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
import app.tracktune.model.user.Administrator;
import app.tracktune.utils.BrowserManager;
import app.tracktune.utils.Frames;
import app.tracktune.utils.ResourceManager;
import app.tracktune.utils.Strings;
import app.tracktune.view.ViewManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class TrackResourcesController extends Controller implements Initializable {
    @FXML private VBox resourcesContainer;
    @FXML private Button btnPrev;
    @FXML private Button btnNext;

    private final ResourceManager resourceManager = new ResourceManager();
    private List<Resource> resources = new ArrayList<>();
    private int currentPage = 0;
    private final int itemsPerPage = 6;
    private final Track track;

    public TrackResourcesController(Track track) {
        this.track = track;
    }

    @Override
    public void initialize(URL location, ResourceBundle res) {
        Platform.runLater(() -> Main.root.setOnCloseRequest(_ -> dispose(resourcesContainer)));
        resources = DatabaseManager.getDAOProvider().getResourceDAO().getAllByTrackID(track.getId());

        btnPrev.setOnAction(_ -> {
            if (currentPage > 0) {
                currentPage--;
                updateResources();
            }
        });

        btnNext.setOnAction(_ -> {
            if ((currentPage + 1) * itemsPerPage < resources.size()) {
                currentPage++;
                updateResources();
            }
        });
        startTimer(resourcesContainer, resources, resourceManager);
        updateResources();
    }

    @FXML
    private void handleAddResource() {
        try{
            if(parentController instanceof AuthenticatedUserDashboardController authController){
                ViewManager.setMainContent(Frames.ADD_RESOURCE_VIEW_PATH, authController.mainContent, parentController);
            }else if(parentController instanceof AdminDashboardController adminController){
                ViewManager.setMainContent(Frames.ADD_RESOURCE_VIEW_PATH, adminController.mainContent, parentController);
            }
        }catch(Exception e){
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
            System.err.println(e.getMessage());
        }
    }

    private void viewResource(Resource resource) {
        try{
            if(resource.getType().equals(ResourceTypeEnum.link)){
                String url = new String(resource.getData(), StandardCharsets.UTF_8);
                BrowserManager.browse(url);
            }else{
                FXMLLoader loader = new FXMLLoader(this.getClass().getResource(Frames.RESOURCE_FILE_VIEW_PATH));
                loader.setControllerFactory(_ -> new ResourceFileController(resource));
                Parent view = loader.load();

                Controller controller = loader.getController();
                controller.setParentController(parentController);

                if(parentController instanceof AuthenticatedUserDashboardController authController){
                    authController.mainContent.getChildren().setAll(view);
                }else if(parentController instanceof AdminDashboardController adminController){
                    adminController.mainContent.getChildren().setAll(view);
                }
            }
        }catch(Exception e){
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
            System.err.println(e.getMessage());
        }
    }

    private void editResource(Resource resource) {
        try{
            FXMLLoader loader = new FXMLLoader(this.getClass().getResource(Frames.EDIT_RESOURCE_VIEW_PATH));
            loader.setControllerFactory(_ -> new EditResourceController(resource));
            Parent view = loader.load();

            Controller controller = loader.getController();
            controller.setParentController(parentController);
            if(parentController instanceof AuthenticatedUserDashboardController authController){
                authController.mainContent.getChildren().setAll(view);
            }else if(parentController instanceof AdminDashboardController adminController){
                adminController.mainContent.getChildren().setAll(view);
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
                HBox itemBox = createResourceItemBox(resource);
                resourcesContainer.getChildren().add(itemBox);
            }
        }
    }

    private HBox createResourceItemBox(Resource resource) {
        int previewWidth = 140;
        int previewHeight = 120;

        resourceManager.setResource(resource);
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

        Button deleteBtn = new Button(Strings.DELETE);
        deleteBtn.getStyleClass().add("reject-button");
        deleteBtn.setOnAction(_ -> deleteResource(resource));
        deleteBtn.setMinWidth(80);

        Button editBtn = new Button(Strings.EDIT);
        editBtn.getStyleClass().add("switch-button");
        editBtn.setOnAction(_ -> editResource(resource));
        editBtn.setMinWidth(80);

        Button viewBtn = new Button(Strings.VIEW);
        viewBtn.getStyleClass().add("view-button");
        viewBtn.setOnAction(_ -> viewResource(resource));
        viewBtn.setMinWidth(80);

        HBox buttonBox;
        if(SessionManager.getInstance().getUser() instanceof Administrator || resource.getUserID() == SessionManager.getInstance().getUser().getId()){
            buttonBox = new HBox(10, viewBtn,editBtn, deleteBtn);
        }
        else{
            buttonBox = new HBox(10, viewBtn);
        }
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

    private void deleteResource(Resource resource) {
        boolean response = ViewManager.setAndGetConfirmAlert(Strings.CONFIRM_DELETION, Strings.CONFIRM_DELETION, Strings.ARE_YOU_SURE);
        if (response)
            try {
                DatabaseManager.getDAOProvider().getResourceDAO().deleteById(resource.getId());
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
}
