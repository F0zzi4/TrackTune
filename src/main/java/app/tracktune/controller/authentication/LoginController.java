package app.tracktune.controller.authentication;

import app.tracktune.controller.Controller;
import app.tracktune.exceptions.SQLInjectionException;
import app.tracktune.exceptions.TrackTuneException;
import app.tracktune.exceptions.UserNotFoundException;
import app.tracktune.model.user.*;
import app.tracktune.utils.SQLiteScripts;
import app.tracktune.utils.Strings;
import app.tracktune.view.ViewManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController extends Controller {
    private final UserDAO userDAO;
    private final PendingUserDAO pendingUserDAO;
    @FXML
    private TextField TxtUsername;
    @FXML
    private PasswordField TxtPassword;

    /**
     * Default constructor to instance the user data access object
     */
    public LoginController() {
        pendingUserDAO = new PendingUserDAO();
        userDAO = new UserDAO();
    }

    /**
     * Access button handler for login
     */
    @FXML
    private void handleLogin() {
        try {
            String username = TxtUsername.getText();
            String password = TxtPassword.getText();

            if (!isInputValid(username, password)) {
                throw new TrackTuneException(Strings.USER_PWD_EMPTY);
            }

            if(SQLiteScripts.checkForSQLInjection(username, password)){
                throw new SQLInjectionException(Strings.ERR_SQL_INJECTION);
            }

            // Check if it's an admin or authenticated user
            User user = userDAO.getActiveUserByUsername(username);
            if (user != null && user.getPassword().equals(password)) {
                if(user instanceof Administrator admin){
                    ViewManager.initSessionManager(admin);
                    ViewManager.navigateToAdminDashboard();
                }else if(user instanceof  AuthenticatedUser authUser){
                    ViewManager.initSessionManager(authUser);
                    ViewManager.navigateToUserDashboard();
                }
                return;
            }

            // Check if it's a pending user
            PendingUser pendingUser = pendingUserDAO.getByUsername(username);
            if (pendingUser != null && pendingUser.getPassword().equals(password)) {
                ViewManager.initSessionManager(pendingUser);
                ViewManager.navigateToPendingUserDashboard();
                return;
            }

            // No user found
            throw new UserNotFoundException(Strings.ERR_USER_NOT_FOUND);
        } catch (TrackTuneException e) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.LOGIN_FAILED, e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.LOGIN_FAILED, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
        }
    }

    /**
     * Access button handler for the account request
     */
    @FXML
    private void handleAccountRequest(){
        ViewManager.navigateToAccountRequest();
    }

    /**
     * Check input fields from the user
     * @param username : Username input from the user
     * @param password : Password input from the user
     * @return true or false
     */
    private boolean isInputValid(String username, String password){
        return !username.isEmpty() && !password.isEmpty();
    }
}