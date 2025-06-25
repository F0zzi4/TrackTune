package app.tracktune.controller.admin;

import app.tracktune.controller.Controller;
import app.tracktune.utils.DatabaseManager;
import app.tracktune.model.user.*;
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
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

public class RequestsController extends Controller implements Initializable {
    /**
     * Container for displaying the list of authorization requests in the UI.
     */
    @FXML
    private VBox requestsContainer;

    /**
     * Button to navigate to the previous page of requests.
     */
    @FXML
    private Button prevButton;

    /**
     * Button to navigate to the next page of requests.
     */
    @FXML
    private Button nextButton;

    /**
     * Tab pane for filtering requests based on their authorization status.
     */
    @FXML
    private TabPane filterTabPane;

    /**
     * Currently selected filter status for displaying authorization requests.
     */
    private AuthRequestStatusEnum currentFilter = AuthRequestStatusEnum.CREATED;

    /**
     * List of all pending authorization requests retrieved from the database.
     */
    private List<PendingUser> allRequests = new ArrayList<>();

    /**
     * List of authorization requests filtered by the current filter status.
     */
    private List<PendingUser> filteredRequests = new ArrayList<>();

    /**
     * Index of the current page being displayed (zero-based).
     */
    private int currentPage = 0;

    /**
     * Number of items (requests) displayed per page.
     */
    private final int itemsPerPage = 5;

    /**
     * Initializes the controller after the root element has been completely processed.
     * <p>
     * Sets up tabs for filtering requests based on their status.
     * Configures pagination buttons (previous and next) with event handlers.
     * Loads the initial page of filtered requests.
     *
     * @param location  The location used to resolve relative paths for the root object, or null if unknown.
     * @param resources The resources used to localize the root object, or null if not localized.
     */
    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        createTabsFromEnum();

        prevButton.setOnAction(_ -> {
            if (currentPage > 0) {
                currentPage--;
                updateRequests();
            }
        });

        nextButton.setOnAction(_ -> {
            if ((currentPage + 1) * itemsPerPage < filteredRequests.size()) {
                currentPage++;
                updateRequests();
            }
        });

        updateRequests();
    }

    /**
     * Creates filter tabs dynamically based on all possible values of AuthRequestStatusEnum.
     * <p>
     * Each tab corresponds to a status filter. When a tab is selected,
     * the filter is updated and the requests list is refreshed.
     * Automatically selects the first tab on creation.
     */
    private void createTabsFromEnum() {
        for (AuthRequestStatusEnum status : AuthRequestStatusEnum.values()) {
            Tab tab = new Tab(status.toString());
            tab.setUserData(status);
            filterTabPane.getTabs().add(tab);
        }

        filterTabPane.getSelectionModel().selectedItemProperty().addListener((_, _, newTab) -> {
            if (newTab != null) {
                currentFilter = (AuthRequestStatusEnum) newTab.getUserData();
                currentPage = 0;
                updateRequests();
            }
        });

        filterTabPane.getSelectionModel().selectFirst();
    }

    /**
     * Filters the list of all requests according to the current filter status.
     * <p>
     * The filtered list is sorted by the request date in ascending order.
     * The filtered results are stored in filteredRequests for further processing.
     */
    private void filterRequests() {
        filteredRequests = allRequests.stream()
                .filter(r -> r.getStatus() == currentFilter)
                .sorted(Comparator.comparing(PendingUser::getRequestDate))
                .collect(Collectors.toList());
    }

    /**
     * Updates the displayed list of authorization requests according to the current filter and pagination.
     * <p>
     * Fetches all pending user requests from the database, applies the current status filter,
     * and clears the UI container before repopulating it with the current page of filtered requests.
     * Handles enabling/disabling pagination buttons based on the current page and total requests.
     * <p>
     * If no requests are available after filtering, displays a placeholder label indicating an empty list.
     */
    private void updateRequests() {
        allRequests = new ArrayList<>(DatabaseManager.getDAOProvider().getPendingUserDAO().getAll());
        filterRequests();
        requestsContainer.getChildren().clear();

        int totalRequests = filteredRequests.size();
        int start = currentPage * itemsPerPage;
        int end = Math.min(start + itemsPerPage, totalRequests);

        prevButton.setDisable(currentPage == 0);
        nextButton.setDisable(end >= totalRequests);

        if (filteredRequests.isEmpty()) {
            Label emptyLabel = new Label(Strings.EMPTY_LIST);
            emptyLabel.getStyleClass().add("empty-list-label");

            HBox emptyBox = new HBox(emptyLabel);
            emptyBox.setAlignment(Pos.CENTER);
            requestsContainer.getChildren().add(emptyBox);
        } else {
            List<PendingUser> pageItems = filteredRequests.subList(start, end);
            for (PendingUser request : pageItems) {
                requestsContainer.getChildren().add(createRequestItem(request));
            }
        }
    }

    /**
     * Creates a UI component representing a single authorization request item.
     * <p>
     * Displays the username and full name of the requester, along with the formatted request date.
     * Provides action buttons to accept or reject the request, which vary depending on the current request status:
     * <ul>
     *     <li>If status is CREATED, shows both reject and accept buttons.</li>
     *     <li>If status is REJECTED, shows only the accept button.</li>
     *     <li>If status is ACCEPTED, shows no action buttons.</li>
     * </ul>
     * <p>
     * The layout consists of a horizontal box with requester info on the left, a spacer in the middle,
     * and action buttons aligned on the right.
     *
     * @param request the PendingUser object representing the authorization request to display
     * @return an HBox containing the formatted request item UI
     */
    private HBox createRequestItem(PendingUser request) {
        Label infoLabel = new Label(request.getUsername() + " - " + request.getName() + " " + request.getSurname());
        infoLabel.getStyleClass().add("request-item-title");

        Label dateLabel = new Label(getFormattedRequestDate(request.getRequestDate()));
        dateLabel.getStyleClass().add("request-item-date");

        VBox textBox = new VBox(5, infoLabel, dateLabel);
        textBox.setAlignment(Pos.CENTER_LEFT);

        Button acceptBtn = new Button(Strings.ACCEPT);
        acceptBtn.getStyleClass().add("accept-button");
        acceptBtn.setOnAction(_ -> acceptRequest(request));

        Button rejectBtn = new Button(Strings.REJECT);
        rejectBtn.getStyleClass().add("reject-button");
        rejectBtn.setOnAction(_ -> rejectRequest(request));

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        switch (request.getStatus()) {
            case CREATED -> buttonBox.getChildren().addAll(rejectBtn, acceptBtn);
            case REJECTED -> buttonBox.getChildren().add(acceptBtn);
            case ACCEPTED -> {}
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox box = new HBox(10, textBox, spacer, buttonBox);
        box.getStyleClass().add("request-item");
        box.setAlignment(Pos.CENTER_LEFT);

        return box;
    }

    /**
     * Accepts an authorization request by updating its status to ACCEPTED and
     * creating a new active authenticated user based on the request details.
     * <p>
     * Updates the request in the database and the local list, then inserts a new user
     * record with an active status and the current timestamp.
     * Finally, refreshes the UI to reflect changes.
     * <p>
     * If any exception occurs during this process, an error alert is shown and the error is logged.
     *
     * @param request the PendingUser authorization request to accept
     */
    private void acceptRequest(PendingUser request) {
        try {
            PendingUser updatedRequest = new PendingUser(
                    request.getUsername(),
                    request.getPassword(),
                    request.getName(),
                    request.getSurname(),
                    request.getRequestDate(),
                    AuthRequestStatusEnum.ACCEPTED
            );
            DatabaseManager.getDAOProvider().getPendingUserDAO().updateById(updatedRequest, request.getId());
            allRequests = allRequests.stream()
                    .map(r -> Objects.equals(r.getId(), request.getId()) ? updatedRequest : r)
                    .collect(Collectors.toList());

            AuthenticatedUser au = new AuthenticatedUser(
                    request.getUsername(),
                    request.getPassword(),
                    request.getName(),
                    request.getSurname(),
                    UserStatusEnum.ACTIVE,
                    new Timestamp(System.currentTimeMillis())
            );
            DatabaseManager.getDAOProvider().getUserDAO().insert(au);

            removeRequestAndUpdate();
        } catch (Exception ex) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
            System.err.println(ex.getMessage());
        }
    }

    /**
     * Rejects an authorization request by updating its status to REJECTED.
     * <p>
     * Updates the request in the database and the local list, then refreshes the UI
     * to reflect changes.
     * <p>
     * If any exception occurs during this process, an error alert is shown and the error is logged.
     *
     * @param request the PendingUser authorization request to reject
     */
    private void rejectRequest(PendingUser request) {
        try {
            PendingUser updatedRequest = new PendingUser(
                    request.getUsername(),
                    request.getPassword(),
                    request.getName(),
                    request.getSurname(),
                    request.getRequestDate(),
                    AuthRequestStatusEnum.REJECTED
            );
            DatabaseManager.getDAOProvider().getPendingUserDAO().updateById(updatedRequest, request.getId());

            allRequests = allRequests.stream()
                    .map(r -> Objects.equals(r.getId(), request.getId()) ? updatedRequest : r)
                    .collect(Collectors.toList());

            removeRequestAndUpdate();
        } catch (Exception ex) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
            System.err.println(ex.getMessage());
        }
    }

    /**
     * Adjusts the current page if needed and refreshes the request list display.
     * <p>
     * Ensures the current page is within valid bounds after modifications to the request list,
     * then calls {@link #updateRequests()} to refresh the UI.
     */
    private void removeRequestAndUpdate() {
        int maxPage = (int) Math.ceil((double) filteredRequests.size() / itemsPerPage) - 1;
        if (currentPage > maxPage) {
            currentPage = Math.max(0, maxPage);
        }
        updateRequests();
    }
}