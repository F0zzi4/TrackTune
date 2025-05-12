package app.tracktune.controller.admin;

import app.tracktune.model.user.*;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for handling pending user requests in the admin panel.
 * Displays paginated user requests with options to accept or reject each request.
 */
public class RequestsController {

    @FXML private VBox requestsContainer;
    @FXML private Button prevButton;
    @FXML private Button nextButton;

    private SortedSet<PendingUser> pendingRequests = new TreeSet<>();
    private int currentPage = 0;
    private final int itemsPerPage = 5;
    private final PendingUserDAO pendingUserDAO = new PendingUserDAO();

    /**
     * Initializes the controller by loading pending requests
     * with status {@code CREATED}, sorted by request date.
     * Also sets up pagination buttons.
     */
    @FXML
    public void initialize() {
        pendingRequests = pendingUserDAO.getAll().stream()
                .filter(r -> r.getStatus() == AuthRequestStatusEnum.CREATED)
                .sorted(Comparator.comparing(PendingUser::getRequestDate))
                .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(PendingUser::getRequestDate))));

        prevButton.setOnAction(e -> {
            if (currentPage > 0) {
                currentPage--;
                updateRequests();
            }
        });

        nextButton.setOnAction(e -> {
            if ((currentPage + 1) * itemsPerPage < pendingRequests.size()) {
                currentPage++;
                updateRequests();
            }
        });

        updateRequests();
    }

    /**
     * Updates the request view by displaying only the requests
     * belonging to the current page.
     * Also enables or disables pagination buttons accordingly.
     */
    private void updateRequests() {
        requestsContainer.getChildren().clear();

        int totalRequests = pendingRequests.size();
        int start = currentPage * itemsPerPage;
        int end = Math.min(start + itemsPerPage, totalRequests);

        prevButton.setDisable(currentPage == 0);
        nextButton.setDisable(end >= totalRequests);

        List<PendingUser> pageItems = new ArrayList<>(pendingRequests).subList(start, end);

        for (PendingUser request : pageItems) {
            HBox requestBox = createRequestItem(request);
            requestsContainer.getChildren().add(requestBox);
        }
    }

    /**
     * Creates the GUI element (HBox) for a single user request.
     * Includes user info, request date, and accept/reject buttons.
     *
     * @param request the {@link PendingUser} to display
     * @return an {@link HBox} containing request information and actions
     */
    private HBox createRequestItem(PendingUser request) {
        Label infoLabel = new Label(
                request.getUsername() + " - " + request.getName() + " " + request.getSurname()
        );
        infoLabel.getStyleClass().add("request-item-title");

        Label dateLabel = new Label(request.getFormattedRequestDate());
        dateLabel.getStyleClass().add("request-item-date");

        VBox textBox = new VBox(5, infoLabel, dateLabel);
        textBox.setAlignment(Pos.CENTER_LEFT);

        Button acceptBtn = new Button("Accetta");
        acceptBtn.getStyleClass().add("accept-button");
        acceptBtn.setOnAction(e -> {
            acceptRequest(request);
        });

        Button rejectBtn = new Button("Rifiuta");
        rejectBtn.getStyleClass().add("reject-button");
        rejectBtn.setOnAction(e -> {
            rejectRequest(request);
        });

        HBox buttonBox = new HBox(10, rejectBtn, acceptBtn);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox box = new HBox(10, textBox, spacer, buttonBox);
        box.getStyleClass().add("request-item");
        box.setAlignment(Pos.CENTER_LEFT);

        return box;
    }

    /**
     * Accepts a pending user request, updates its status,
     * persists the update to the database, and inserts the new user
     * into the authenticated users table.
     *
     * @param request the {@link PendingUser} to accept
     */
    private void acceptRequest(PendingUser request){
        try {
            request.setStatus(AuthRequestStatusEnum.ACCEPTED);
            pendingUserDAO.update(request);

            AuthenticatedUser au = new AuthenticatedUser(
                    request.getUsername(),
                    request.getPassword(),
                    request.getName(),
                    request.getSurname(),
                    UserStatusEnum.ACTIVE,
                    new Timestamp(System.currentTimeMillis())
            );
            UserDAO userDAO = new UserDAO();
            userDAO.insert(au);

            removeRequestAndUpdate(request);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }

    /**
     * Rejects a pending user request, updates its status,
     * and removes it from the request view.
     *
     * @param request the {@link PendingUser} to reject
     */
    private void rejectRequest(PendingUser request){
        try {
            request.setStatus(AuthRequestStatusEnum.REJECTED);
            pendingUserDAO.update(request);

            removeRequestAndUpdate(request);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }

    /**
     * Removes the specified {@link PendingUser} request from the current list,
     * recalculates the maximum number of pages, adjusts the current page index
     * if necessary, and updates the view accordingly.
     *
     * This method is used after a request has been either accepted or rejected
     * to ensure the pagination and user interface remain consistent.
     *
     * @param request the {@link PendingUser} to remove from the displayed list
     */
    private void removeRequestAndUpdate(PendingUser request) {
        pendingRequests.remove(request);
        int maxPage = (int) Math.ceil((double) pendingRequests.size() / itemsPerPage);
        if (currentPage >= maxPage && currentPage > 0) {
            currentPage--;
        }
        updateRequests();
    }
}
