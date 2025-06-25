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
import java.util.*;
import java.util.stream.Collectors;

public class UsersController extends Controller implements Initializable {
    /**
     * Container for displaying the list of authenticated users in the UI.
     */
    @FXML
    private VBox usersContainer;

    /**
     * Button to navigate to the previous page of users.
     */
    @FXML
    private Button prevButton;

    /**
     * Button to navigate to the next page of users.
     */
    @FXML
    private Button nextButton;

    /**
     * Tab pane for filtering users based on their status.
     */
    @FXML
    private TabPane filterTabPane;

    /**
     * Currently selected filter status for displaying users.
     */
    private Object currentFilter = UserStatusEnum.ACTIVE;

    /**
     * List of all authenticated users retrieved from the database.
     */
    private final List<AuthenticatedUser> users = new ArrayList<>();

    /**
     * List of users filtered by the current filter status.
     */
    private List<AuthenticatedUser> filteredUsers = new ArrayList<>();

    /**
     * Index of the current page being displayed (zero-based).
     */
    private int currentPage = 0;

    /**
     * Number of items (users) displayed per page.
     */
    private final int itemsPerPage = 5;

    // CONSTANTS
    private static final String ADMIN = "ADMIN";

    /**
     * Initializes the controller after its root element has been completely processed.
     * <p>
     * Loads all authenticated users from the database, sets up filter tabs based on user status,
     * and configures pagination controls (previous and next buttons) with their respective actions.
     * Finally, updates the user list display according to the current filter and page.
     * </p>
     *
     * @param location  The location used to resolve relative paths for the root object, or null if unknown.
     * @param resources The resources used to localize the root object, or null if not localized.
     */
    @FXML
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        users.clear();
        users.addAll(
                DatabaseManager.getDAOProvider().getUserDAO().getAll().stream()
                        .filter(u -> u instanceof AuthenticatedUser)
                        .map(u -> (AuthenticatedUser) u)
                        .toList()
        );
        createTabsFromEnum();

        prevButton.setOnAction(_ -> {
            if (currentPage > 0) {
                currentPage--;
                updateUsers();
            }
        });

        nextButton.setOnAction(_ -> {
            if ((currentPage + 1) * itemsPerPage < filteredUsers.size()) {
                currentPage++;
                updateUsers();
            }
        });

        updateUsers();
    }

    /**
     * Creates filter tabs dynamically based on the {@link UserStatusEnum} values
     * and adds a tab for administrators.
     * <p>
     * Each tab stores its corresponding filter data in its userData property.
     * When the selected tab changes, the current filter is updated, the page is reset,
     * and the user list is refreshed accordingly.
     * </p>
     */
    private void createTabsFromEnum() {
        filterTabPane.getTabs().clear();

        for (UserStatusEnum status : UserStatusEnum.values()){
            Tab tab = new Tab(status.toString());
            tab.setUserData(status);
            filterTabPane.getTabs().add(tab);
        }

        filterTabPane.getSelectionModel().selectedItemProperty().addListener((_, _, newTab) -> {
            if (newTab != null) {
                currentFilter = newTab.getUserData();
                currentPage = 0;
                updateUsers();
            }
        });

        Tab adminTab = new Tab(Strings.ADMIN);
        adminTab.setUserData(Strings.ADMIN);
        filterTabPane.getTabs().add(adminTab);

        filterTabPane.getSelectionModel().selectFirst();
    }

    /**
     * Filters the list of users based on the currently selected filter.
     * <p>
     * If the current filter is the special "ADMIN" tab, only active administrators are included.
     * Otherwise, users are filtered by the selected status excluding administrators.
     * The resulting list is sorted alphabetically by username.
     * </p>
     */
    private void filterUsers() {
        if (ADMIN.equals(currentFilter)) {
            filteredUsers = users.stream()
                    .filter(u -> u instanceof Administrator && u.getStatus() == UserStatusEnum.ACTIVE)
                    .sorted(Comparator.comparing(User::getUsername))
                    .collect(Collectors.toList());
        } else {
            filteredUsers = users.stream()
                    .filter(u -> u.getStatus() == currentFilter && !(u instanceof Administrator))
                    .sorted(Comparator.comparing(User::getUsername))
                    .collect(Collectors.toList());
        }
    }

    /**
     * Updates the displayed list of users according to the current filter and pagination.
     * <p>
     * Clears the user container and populates it with users on the current page.
     * Also handles the enabling/disabling of pagination buttons and shows a placeholder
     * message if no users are found for the current filter.
     * </p>
     */
    private void updateUsers() {
        filterUsers();
        usersContainer.getChildren().clear();

        int totalUsers = filteredUsers.size();
        int start = currentPage * itemsPerPage;
        int end = Math.min(start + itemsPerPage, totalUsers);

        prevButton.setDisable(currentPage == 0);
        nextButton.setDisable(end >= totalUsers);

        if (filteredUsers.isEmpty()) {
            Label emptyLabel = new Label(Strings.EMPTY_LIST);
            emptyLabel.getStyleClass().add("empty-list-label");

            HBox emptyBox = new HBox(emptyLabel);
            emptyBox.setAlignment(Pos.CENTER);
            usersContainer.getChildren().add(emptyBox);
        } else {
            List<AuthenticatedUser> pageItems = filteredUsers.subList(start, end);

            for (AuthenticatedUser user : pageItems) {
                usersContainer.getChildren().add(createUserItem(user));
            }
        }
    }

    /**
     * Creates a UI item (HBox) representing a user with info labels and action buttons.
     * <p>
     * Depending on the user's status and role, different buttons are shown for actions such as
     * suspend, restore, remove, or promote to administrator.
     * </p>
     *
     * @param user The AuthenticatedUser instance to display.
     * @return An HBox node containing user info and action buttons.
     */
    private HBox createUserItem(AuthenticatedUser user) {
        Label infoLabel = new Label(user.getUsername() + " - " + user.getName() + " " + user.getSurname());
        infoLabel.getStyleClass().add("user-item-title");

        Label nTrackLabel = new Label(getFormattedRequestDate(user.getCreationDate()));
        nTrackLabel.getStyleClass().add("user-item-date");

        VBox textBox = new VBox(5, infoLabel, nTrackLabel);
        textBox.setAlignment(Pos.CENTER_LEFT);

        Button restoreBtn = new Button(Strings.RESTORE);
        restoreBtn.getStyleClass().add("accept-button");
        restoreBtn.setOnAction(_ -> restoreUser(user));

        Button makeAdminButton = new Button(Strings.MAKE_ADMIN);
        makeAdminButton.getStyleClass().add("make-admin-button");
        makeAdminButton.setOnAction(_ -> makeAdmin(user));

        Button suspendButton = new Button(Strings.SUSPEND);
        suspendButton.getStyleClass().add("suspend-button");
        suspendButton.setOnAction(_ -> suspendUser(user));

        Button removeBtn = new Button(Strings.DELETE);
        removeBtn.getStyleClass().add("delete-button");
        removeBtn.setOnAction(_ -> removeUser(user));

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        if(!(user instanceof Administrator)) {
            if(user.getStatus() == UserStatusEnum.ACTIVE){
                buttonBox.getChildren().add(suspendButton);
                buttonBox.getChildren().add(removeBtn);
                buttonBox.getChildren().add(makeAdminButton);
            }

            else if(user.getStatus() == UserStatusEnum.SUSPENDED) {
                buttonBox.getChildren().add(removeBtn);
                buttonBox.getChildren().add(restoreBtn);
            }
            else if(user.getStatus() == UserStatusEnum.REMOVED) {
                buttonBox.getChildren().add(restoreBtn);
            }
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox box = new HBox(10, textBox, spacer, buttonBox);
        box.getStyleClass().add("user-item");
        box.setAlignment(Pos.CENTER_LEFT);

        return box;
    }

    /**
     * Restores a user by setting their status to ACTIVE,
     * updates the database, refreshes the local list,
     * and updates the UI accordingly.
     *
     * @param user the AuthenticatedUser to restore
     */
    private void restoreUser (AuthenticatedUser user) {
        try {
            user.setStatus(UserStatusEnum.ACTIVE);
            DatabaseManager.getDAOProvider().getUserDAO().updateById(user, user.getId());
            int index = users.indexOf(user);
            if (index >= 0) users.set(index, user);
            adjustPageAfterUpdate();
        } catch (Exception ex) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
            System.err.println(ex.getMessage());
        }
    }

    /**
     * Removes a user by setting their status to REMOVED,
     * updates the database, refreshes the local list,
     * and updates the UI accordingly.
     *
     * @param user the AuthenticatedUser to remove
     */
    private void removeUser(AuthenticatedUser user) {
        try {
            user.setStatus(UserStatusEnum.REMOVED);
            DatabaseManager.getDAOProvider().getUserDAO().updateById(user, user.getId());
            int index = users.indexOf(user);
            if (index >= 0) users.set(index, user);
            adjustPageAfterUpdate();
        } catch (Exception ex) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
            System.err.println(ex.getMessage());
        }
    }

    /**
     * Sets the user's status to SUSPENDED.
     *
     * @param user the user to suspend
     */
    private void suspendUser(AuthenticatedUser user) {
        try {
            user.setStatus(UserStatusEnum.SUSPENDED);
            DatabaseManager.getDAOProvider().getUserDAO().updateById(user, user.getId());
            int index = users.indexOf(user);
            if (index >= 0) users.set(index, user);
            adjustPageAfterUpdate();
        } catch (Exception ex) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
            System.err.println(ex.getMessage());
        }
    }

    /**
     * Promotes a user to administrator by replacing their instance with an Administrator object.
     *
     * @param user the user to promote
     */
    private void makeAdmin(AuthenticatedUser user) {
        try {
            Administrator ad = new Administrator(user.getId(), user.getUsername(), user.getPassword(), user.getName(), user.getSurname(), user.getStatus(), user.getCreationDate());
            DatabaseManager.getDAOProvider().getUserDAO().updateById(ad, user.getId());
            int index = users.indexOf(user);
            if (index >= 0) users.set(index, ad);
            adjustPageAfterUpdate();
        } catch (Exception ex) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
            System.err.println(ex.getMessage());
        }
    }


    /**
     * Adjusts the current page index if the number of filtered users has changed due to an update.
     * Ensures the current page is within valid bounds.
     */
    private void adjustPageAfterUpdate() {
        int maxPage = (int) Math.ceil((double) filteredUsers.size() / itemsPerPage) - 1;
        if (currentPage > maxPage) {
            currentPage = Math.max(0, maxPage);
        }
        updateUsers();
    }
}