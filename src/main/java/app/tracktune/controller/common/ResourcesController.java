package app.tracktune.controller.common;

import app.tracktune.Main;
import app.tracktune.controller.Controller;
import app.tracktune.controller.admin.AdminDashboardController;
import app.tracktune.controller.authenticatedUser.AuthenticatedUserDashboardController;
import app.tracktune.model.track.TrackGenre;
import app.tracktune.model.track.TrackInstrument;
import app.tracktune.utils.SessionManager;
import app.tracktune.utils.DatabaseManager;
import app.tracktune.model.author.Author;
import app.tracktune.model.resource.Resource;
import app.tracktune.model.resource.ResourceTypeEnum;
import app.tracktune.model.track.Track;
import app.tracktune.model.track.TrackAuthor;
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
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ResourcesController extends Controller implements Initializable {
    /** Container VBox where resource UI items are displayed. */
    @FXML private VBox resourcesContainer;

    /** Button to navigate to the previous page of resources. */
    @FXML private Button btnPrev;

    /** Button to navigate to the next page of resources. */
    @FXML private Button btnNext;

    /** List holding all resources to be paginated and displayed. */
    private List<Resource> resources = new ArrayList<>();

    /** Manager responsible for browser navigation actions (e.g., opening URLs). */
    private BrowserManager browserManager;

    /** Manager responsible for resource-related operations (CRUD, etc.). */
    private ResourceManager resourceManager;

    /** The currently selected or active resource in context. */
    protected Resource resource;

    /** Number of resource items shown per page in the UI. */
    private final int itemsPerPage = 5;

    /** Current page index in the pagination (zero-based). */
    private int currentPage = 0;

    /**
     * Initializes the controller by loading user-specific resources, setting up navigation buttons,
     * starting the media readiness timer, and configuring the window close event.
     * <p>
     * Specifically:
     * <ul>
     *     <li>Retrieves all resources associated with the current user from the database.</li>
     *     <li>Initializes the {@link BrowserManager} and {@link ResourceManager} singletons.</li>
     *     <li>Configures the window to dispose of media players when closed.</li>
     *     <li>Sets up pagination controls to navigate between resource pages.</li>
     *     <li>Starts a timer to track the readiness of media resources and updates the displayed content.</li>
     * </ul>
     *
     * @param location the location used to resolve relative paths for the root object (unused in this method)
     * @param res      the resources used to localize the root object (unused in this method)
     */
    @Override
    public void initialize(URL location, ResourceBundle res) {
        browserManager = BrowserManager.getInstance();
        resourceManager = ResourceManager.getInstance();
        Platform.runLater(() -> Main.root.setOnCloseRequest(_ -> dispose(resourcesContainer)));
        resources = DatabaseManager.getDAOProvider().getResourceDAO().getAllByUserID(SessionManager.getInstance().getUser().getId());

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

    /**
     * Handles the action of adding a new resource.
     * <p>
     * Depending on the type of the parent controller, this method loads
     * the "Add Resource" view into the main content area of the corresponding dashboard.
     * If an error occurs during loading, an error alert is shown.
     */
    @FXML
    private void handleAddResource() {
        try{
            if(parentController instanceof AuthenticatedUserDashboardController authController){
                ViewManager.setMainContent(Frames.ADD_RESOURCE_VIEW_PATH, authController.mainContent, parentController);
            }
            else if(parentController instanceof AdminDashboardController adminController){
                ViewManager.setMainContent(Frames.ADD_RESOURCE_VIEW_PATH, adminController.mainContent, parentController);
            }
        }catch(Exception e){
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
            System.err.println(e.getMessage());
        }
    }

    /**
     * Displays the given resource.
     * <p>
     * If the resource is a link, it opens the URL in the default browser.
     * Otherwise, it loads the resource file view and sets it in the main content area
     * of the parent dashboard controller.
     *
     * @param resource the resource to view
     */
    @FXML
    private void viewResource(Resource resource) {
        try{
            if(resource.getType().equals(ResourceTypeEnum.link)){
                String url = new String(resource.getData(), StandardCharsets.UTF_8);
                browserManager.browse(url);
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

    /**
     * Opens the edit view for the specified resource.
     * <p>
     * Loads the edit resource UI, initializes its controller with the given resource,
     * and replaces the main content of the parent dashboard controller with this view.
     * If an error occurs during loading, an error alert is displayed.
     *
     * @param resource the resource to be edited
     */
    @FXML
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

    /**
     * Updates the resources view by displaying the current page of resource items.
     * <p>
     * Clears the current displayed resources, calculates the subset of resources
     * for the current page, updates pagination button states, and populates the container
     * with resource items. If the resource list is empty, displays a message indicating
     * the list is empty.
     */
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

    /**
     * Creates an HBox container representing a single resource item with a media preview and details.
     * <p>
     * This method sets the resource in the resource manager, generates a media preview node,
     * and combines it with the resource's detail view (created by {@code createRequestItem})
     * into an HBox with spacing and alignment configured.
     *
     * @param resource the resource to create the item box for
     * @return an HBox containing the media preview and resource details, styled and aligned properly
     */
    private HBox createResourceItemBox(Resource resource) {
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

    /**
     * Creates an HBox representing the detailed view of a resource item, including its track title,
     * authors, creation date, and action buttons for viewing, editing, and deleting.
     * <p>
     * The method queries the database for the track and authors associated with the resource,
     * formats the information into labels, and arranges them with buttons in a styled HBox.
     * The buttons trigger the respective actions on the resource.
     *
     * @param resource the resource to create the request item view for
     * @return an HBox containing the resource's metadata and action buttons, styled and aligned
     */
    private HBox createRequestItem(Resource resource) {
        Track track = DatabaseManager.getDAOProvider().getTrackDAO().getById(resource.getTrackID());
        List<TrackAuthor> authors = DatabaseManager.getDAOProvider().getTrackAuthorDAO().getByTrackId(resource.getTrackID());
        List<TrackGenre> genres = DatabaseManager.getDAOProvider().getTrackGenreDAO().getByTrackId(track.getId());
        List<TrackInstrument> instruments = DatabaseManager.getDAOProvider().getTrackInstrumentDAO().getByTrackId(track.getId());

        Label trackLabel = new Label(track.getTitle());
        trackLabel.getStyleClass().add("request-item-title");

        StringBuilder authorNames = new StringBuilder();
        for (TrackAuthor trackAuthor : authors) {
            Author author = DatabaseManager.getDAOProvider().getAuthorDAO().getById(trackAuthor.getAuthorId());
            authorNames.append(author.getAuthorshipName()).append(", ");
        }

        String genreNames = genres.stream()
                .map(tg -> DatabaseManager.getDAOProvider().getGenreDAO().getById(tg.getGenreId()).getName())
                .collect(Collectors.joining(", "));
        Label genresLabel = new Label("Genres: " + genreNames);
        genresLabel.getStyleClass().add("request-item-authors");

        Label instrumentsLabel = null;
        if(!instruments.isEmpty()){
            String instrumentsNames = instruments.stream()
                    .map(ti -> DatabaseManager.getDAOProvider().getMusicalInstrumentDAO().getById(ti.getInstrumentId()).getName())
                    .collect(Collectors.joining(", "));
            instrumentsLabel = new Label("Instruments: " + instrumentsNames);
            instrumentsLabel.getStyleClass().add("request-item-authors");
        }

        if (!authorNames.isEmpty()) {
            authorNames.setLength(authorNames.length() - 2);
        }

        Label authorsLabel = new Label("Authors: " + authorNames);
        authorsLabel.getStyleClass().add("request-item-authors");

        HBox titleBox = new HBox(10, trackLabel, authorsLabel, genresLabel, instrumentsLabel);
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

        HBox buttonBox = new HBox(10, viewBtn,editBtn, deleteBtn);
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

    /**
     * Deletes the specified resource after user confirmation.
     * <p>
     * Prompts the user with a confirmation dialog. If the user confirms,
     * deletes the resource from the database and removes it from the local resource list.
     * Adjusts the current page if necessary and refreshes the displayed resources.
     *
     * @param resource the resource to be deleted
     */
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
