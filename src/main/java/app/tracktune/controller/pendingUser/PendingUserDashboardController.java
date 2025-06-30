package app.tracktune.controller.pendingUser;

import app.tracktune.controller.Controller;
import app.tracktune.model.user.AuthRequestStatusEnum;
import app.tracktune.model.user.AuthenticatedUser;
import app.tracktune.model.user.PendingUser;
import app.tracktune.utils.DatabaseManager;
import app.tracktune.utils.Strings;
import app.tracktune.view.ViewManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the dashboard of a pending user.
 * <p>
 * Manages the UI display of the pending user's status and provides logout functionality.
 */
public class PendingUserDashboardController extends Controller implements Initializable {
    @FXML
    public Label LblStatusValue;

    /**
     * Initializes the controller.
     * <p>
     * Retrieves the current session user and if it is a PendingUser, displays the user's status with
     * an appropriate text color. If the session user is not a PendingUser, displays an error message.
     *
     * @param location  The location used to resolve relative paths for the root object, or null if unknown.
     * @param resources The resources used to localize the root object, or null if not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (ViewManager.getSessionUser() instanceof PendingUser pendingUser) {
            int status = pendingUser.getStatus().ordinal();
            switch (status) {
                case 0 -> LblStatusValue.setStyle("-fx-text-fill: #432d29;");
                case 2 -> LblStatusValue.setStyle("-fx-text-fill: #870505;");
            }

            LblStatusValue.setText(pendingUser.getStatus().toString());
            if(pendingUser.getStatus().equals(AuthRequestStatusEnum.ACCEPTED)){
                AuthenticatedUser u = (AuthenticatedUser) DatabaseManager.getDAOProvider().getUserDAO().getSingleActiveUserByUsername((pendingUser.getUsername()));
                LblStatusValue.setText(u.getStatus().toString());
            }
        } else {
            LblStatusValue.setText(Strings.ERROR);
            LblStatusValue.setStyle("-fx-text-fill: #870505;");
        }
    }

    /**
     * Handles the logout action.
     * <p>
     * Attempts to log out the current user by calling {@link ViewManager#logout()}.
     * If the logout process fails, displays an error alert to the user.
     */
    @FXML
    public void handleLogout() {
        try {
            ViewManager.logout();
        } catch (Exception e) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
        }
    }
}
