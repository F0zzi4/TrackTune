package app.tracktune.controller.common;

import app.tracktune.Main;
import app.tracktune.controller.Controller;
import app.tracktune.controller.admin.AdminDashboardController;
import app.tracktune.controller.authenticatedUser.AuthenticatedUserDashboardController;
import app.tracktune.model.track.TrackGenre;
import app.tracktune.model.track.TrackInstrument;
import app.tracktune.utils.*;
import app.tracktune.model.author.Author;
import app.tracktune.model.resource.Resource;
import app.tracktune.model.resource.ResourceTypeEnum;
import app.tracktune.model.track.Track;
import app.tracktune.model.track.TrackAuthor;
import app.tracktune.model.user.Administrator;
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
import java.util.stream.Collectors;

public class TrackResourcesController extends Controller implements Initializable {
    /**
     * Container VBox to hold the list of resource items displayed in the UI.
     */
    @FXML
    private VBox resourcesContainer;

    /**
     * Button to navigate to the previous page of resources.
     */
    @FXML
    private Button btnPrev;

    /**
     * Button to navigate to the next page of resources.
     */
    @FXML
    private Button btnNext;

    /**
     * List of all resources currently managed/displayed.
     */
    private List<Resource> resources = new ArrayList<>();

    /**
     * The Track associated with the resources.
     */
    private final Track track;

    /**
     * Singleton instance managing resource-related operations and UI components.
     */
    private ResourceManager resourceManager;

    /**
     * Singleton instance to manage web browsing for link-type resources.
     */
    private BrowserManager browserManager;

    /**
     * Current page index for resource pagination (0-based).
     */
    private int currentPage = 0;

    /**
     * Number of resource items displayed per page.
     */
    private final int itemsPerPage = 5;

    /**
     * Constructs a TrackResourcesController for managing resources related to a specific track.
     *
     * @param track the Track whose resources will be managed and displayed
     */
    public TrackResourcesController(Track track) {
        this.track = track;
    }

    /**
     * Initializes the controller after its root element has been completely processed.
     * <p>
     * This method:
     * <ul>
     *   <li>Initializes singleton managers (BrowserManager and ResourceManager).</li>
     *   <li>Sets a close request handler to dispose of resources properly when the application closes.</li>
     *   <li>Loads all resources associated with the current track from the database.</li>
     *   <li>Sets up pagination controls (Previous and Next buttons) to navigate resource pages.</li>
     *   <li>Starts any required timers related to resource display updates.</li>
     *   <li>Populates the initial page of resources in the UI.</li>
     * </ul>
     *
     * @param location  The location used to resolve relative paths for the root object, or null if unknown.
     * @param res The resources used to localize the root object, or null if not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle res) {
        browserManager = BrowserManager.getInstance();
        resourceManager = ResourceManager.getInstance();
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

    /**
     * Handles the action of adding a new resource.
     * <p>
     * Depending on the type of the parent controller (AuthenticatedUserDashboardController or AdminDashboardController),
     * this method loads the "Add Resource" view into the main content area of the respective dashboard.
     * If an exception occurs, it shows a generic error alert.
     */
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

    /**
     * Displays the specified resource.
     * <p>
     * If the resource type is a link, opens the URL in the browser.
     * Otherwise, loads the resource file view and sets it in the main content area
     * of the parent dashboard controller.
     * <p>
     * Errors are caught and reported with a generic error alert.
     *
     * @param resource the resource to be viewed
     */
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
     * Opens the resource edit view for the specified resource.
     * <p>
     * Loads the edit resource FXML and sets a new EditResourceController initialized with the given resource.
     * The loaded view is then displayed in the main content area of the parent dashboard controller
     * (either AuthenticatedUserDashboardController or AdminDashboardController).
     * <p>
     * If an exception occurs during loading or displaying the view, a generic error alert is shown.
     *
     * @param resource the resource to be edited
     */
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
     * Updates the displayed list of resources in the UI according to the current page.
     * <p>
     * Clears the resources container and populates it with resource items for the current page,
     * applying pagination controls to enable/disable navigation buttons accordingly.
     * <p>
     * If there are no resources, displays a message indicating the list is empty.
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
     * Creates an HBox representing a single resource item with a media preview and details.
     * <p>
     * The preview is generated using the ResourceManager and sized to fixed dimensions.
     * The resource details and controls are created by the {@code createRequestItem} method.
     * <p>
     * The returned HBox arranges the preview and the details side by side with spacing,
     * and applies styling and layout constraints for proper resizing.
     *
     * @param resource the Resource object to represent visually
     * @return an HBox containing the media preview and resource details
     */
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

    /**
     * Creates an HBox containing detailed information and action buttons for a given resource.
     * <p>
     * The method fetches the associated Track and its Authors from the database to display their
     * titles and names. It constructs labels for the track title and authors, along with a formatted
     * creation date description.
     * <p>
     * It also creates buttons to view, edit, and delete the resource. The edit and delete buttons
     * are shown only if the current user is an Administrator or the owner of the resource.
     * <p>
     * The layout organizes the text information on the left and the buttons on the right, with a spacer
     * in between for proper alignment and resizing.
     *
     * @param resource the Resource object for which to create the UI item
     * @return an HBox containing the resource's information and control buttons
     */
    private HBox createRequestItem(Resource resource) {
        Track track = DatabaseManager.getDAOProvider().getTrackDAO().getById(resource.getTrackID());
        List<TrackAuthor> trackAuthors = DatabaseManager.getDAOProvider().getTrackAuthorDAO().getByTrackId(resource.getTrackID());
        List<TrackGenre> genres = DatabaseManager.getDAOProvider().getTrackGenreDAO().getByTrackId(track.getId());
        List<TrackInstrument> instruments = DatabaseManager.getDAOProvider().getTrackInstrumentDAO().getByTrackId(track.getId());

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

    /**
     * Deletes the specified resource after user confirmation.
     * <p>
     * Displays a confirmation dialog to the user. If confirmed, attempts to delete the resource
     * from the database and remove it from the local list. Adjusts the current page if necessary
     * to ensure the pagination remains valid, then updates the resource display.
     * <p>
     * If any exception occurs during deletion, an error alert is shown to the user.
     *
     * @param resource the Resource to be deleted
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
