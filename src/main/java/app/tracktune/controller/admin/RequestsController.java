package app.tracktune.controller.admin;

import app.tracktune.controller.Controller;
import app.tracktune.model.DatabaseManager;
import app.tracktune.model.user.*;
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
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

public class RequestsController extends Controller implements Initializable {

    @FXML private VBox requestsContainer;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private TabPane filterTabPane;

    private AuthRequestStatusEnum currentFilter = AuthRequestStatusEnum.CREATED;
    private List<PendingUser> allRequests = new ArrayList<>();
    private List<PendingUser> filteredRequests = new ArrayList<>();
    private int currentPage = 0;
    private final int itemsPerPage = 5;


    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        allRequests = new ArrayList<>(DatabaseManager.getDAOProvider().getPendingUserDAO().getAll());
        createTabsFromEnum();

        prevButton.setOnAction(e -> {
            if (currentPage > 0) {
                currentPage--;
                updateRequests();
            }
        });

        nextButton.setOnAction(e -> {
            if ((currentPage + 1) * itemsPerPage < filteredRequests.size()) {
                currentPage++;
                updateRequests();
            }
        });

        updateRequests();
    }

    private void createTabsFromEnum() {
        for (AuthRequestStatusEnum status : AuthRequestStatusEnum.values()) {
            Tab tab = new Tab(status.toString());
            tab.setUserData(status);
            filterTabPane.getTabs().add(tab);
        }

        filterTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null) {
                currentFilter = (AuthRequestStatusEnum) newTab.getUserData();
                currentPage = 0;
                updateRequests();
            }
        });

        filterTabPane.getSelectionModel().selectFirst();
    }

    private void filterRequests() {
        filteredRequests = allRequests.stream()
                .filter(r -> r.getStatus() == currentFilter)
                .sorted(Comparator.comparing(PendingUser::getRequestDate))
                .collect(Collectors.toList());
    }

    private void updateRequests() {
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

    private HBox createRequestItem(PendingUser request) {
        Label infoLabel = new Label(request.getUsername() + " - " + request.getName() + " " + request.getSurname());
        infoLabel.getStyleClass().add("request-item-title");

        Label dateLabel = new Label(getFormattedRequestDate(request.getRequestDate()));
        dateLabel.getStyleClass().add("request-item-date");

        VBox textBox = new VBox(5, infoLabel, dateLabel);
        textBox.setAlignment(Pos.CENTER_LEFT);

        Button acceptBtn = new Button(Strings.ACCEPT);
        acceptBtn.getStyleClass().add("accept-button");
        acceptBtn.setOnAction(e -> acceptRequest(request));

        Button rejectBtn = new Button(Strings.REJECT);
        rejectBtn.getStyleClass().add("reject-button");
        rejectBtn.setOnAction(e -> rejectRequest(request));

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

    private void removeRequestAndUpdate() {
        int maxPage = (int) Math.ceil((double) filteredRequests.size() / itemsPerPage) - 1;
        if (currentPage > maxPage) {
            currentPage = Math.max(0, maxPage);
        }
        updateRequests();
    }
}