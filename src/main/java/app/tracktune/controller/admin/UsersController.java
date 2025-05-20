package app.tracktune.controller.admin;

import app.tracktune.controller.Controller;
import app.tracktune.exceptions.AuthorAlreadyExixtsExeption;
import app.tracktune.exceptions.TrackTuneException;
import app.tracktune.model.author.Author;
import app.tracktune.model.author.AuthorStatusEnum;
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
import java.util.*;
import java.util.stream.Collectors;

public class UsersController extends Controller implements Initializable {

    @FXML
    private VBox usersContainer;
    @FXML
    private Button prevButton;
    @FXML
    private Button nextButton;
    @FXML
    private TabPane filterTabPane;

    private Object currentFilter = UserStatusEnum.ACTIVE;
    private final List<AuthenticatedUser> users = new ArrayList<>();
    private List<AuthenticatedUser> filteredUsers = new ArrayList<>();
    private int currentPage = 0;
    private final int itemsPerPage = 4;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        users.clear();
        users.addAll(
                userDAO.getAll().stream()
                        .filter(u -> u instanceof AuthenticatedUser)
                        .map(u -> (AuthenticatedUser) u)
                        .toList()
        );
        createTabsFromEnum();

        prevButton.setOnAction(e -> {
            if (currentPage > 0) {
                currentPage--;
                updateUsers();
            }
        });

        nextButton.setOnAction(e -> {
            if ((currentPage + 1) * itemsPerPage < filteredUsers.size()) {
                currentPage++;
                updateUsers();
            }
        });

        updateUsers();
    }

    private void createTabsFromEnum() {
        filterTabPane.getTabs().clear();

        for (UserStatusEnum status : UserStatusEnum.values()){
            Tab tab = new Tab(status.toString());
            tab.setUserData(status);
            filterTabPane.getTabs().add(tab);
        }

        filterTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
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

    private void filterUsers() {
        if ("ADMIN".equals(currentFilter)) {
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

    private HBox createUserItem(AuthenticatedUser user) {
        Label infoLabel = new Label(user.getUsername() + " - " + user.getName() + " " + user.getSurname());
        infoLabel.getStyleClass().add("user-item-title");

        Label nTrackLabel = new Label(getFormattedRequestDate(user.getCreationDate()));
        nTrackLabel.getStyleClass().add("user-item-date");

        VBox textBox = new VBox(5, infoLabel, nTrackLabel);
        textBox.setAlignment(Pos.CENTER_LEFT);

        Button restoreBtn = new Button(Strings.RESTORE);
        restoreBtn.getStyleClass().add("accept-button");
        restoreBtn.setOnAction(e -> restoreUser(user));

        Button makeAdminButton = new Button(Strings.MAKE_ADMIN);
        makeAdminButton.getStyleClass().add("make-admin-button");
        makeAdminButton.setOnAction(e -> makeAdmin(user));

        Button suspendButton = new Button(Strings.SUSPEND);
        suspendButton.getStyleClass().add("suspend-button");
        suspendButton.setOnAction(e -> suspendUser(user));

        Button removeBtn = new Button(Strings.DELETE);
        removeBtn.getStyleClass().add("delete-button");
        removeBtn.setOnAction(e -> removeUser(user));

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        if(user instanceof Administrator) {
            //
        }
        else{
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

    private void restoreUser (AuthenticatedUser user) {
        try {
            user.setStatus(UserStatusEnum.ACTIVE);
            userDAO.updateById(user, user.getId());
            int index = users.indexOf(user);
            if (index >= 0) users.set(index, user);
            adjustPageAfterUpdate();
        } catch (Exception ex) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
            System.err.println(ex.getMessage());
        }
    }

    private void removeUser(AuthenticatedUser user) {
        try {
            user.setStatus(UserStatusEnum.REMOVED);
            userDAO.updateById(user, user.getId());
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
            userDAO.updateById(user, user.getId());
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
            userDAO.updateById(ad, user.getId());
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