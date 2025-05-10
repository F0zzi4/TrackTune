package app.tracktune.controller;

import app.tracktune.exceptions.TrackTuneException;
import app.tracktune.model.user.*;
import app.tracktune.utils.Strings;
import app.tracktune.view.ViewManager;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

public class LoginController {
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
        userDAO = new UserDAO();}

    /**
     * Access button handler for login
     */
    @FXML
    private void handleLogin() {
        String username = TxtUsername.getText();
        String password = TxtPassword.getText();

        try {
            if (!isInputValid(username, password)) {
                throw new TrackTuneException(Strings.USER_PWD_EMPTY);
            }

            // Check administrator / user
            User user = userDAO.getByKey(username);
            if (user != null && user.getPassword().equals(password)) {
                ViewManager.setUser(user);
                ViewManager.navigateToDashboard();
                return;
            }

            // Check pending user
            PendingUser pendingUser = pendingUserDAO.getByKey(username);
            if (pendingUser != null && pendingUser.getPassword().equals(password)) {
                ViewManager.setUser(pendingUser);
                ViewManager.navigateToPendingDashboard();
                return;
            }

            // No user found
            throw new TrackTuneException(Strings.ERR_USER_NOT_FOUND);

        } catch (TrackTuneException e) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.LOGIN_FAILED, e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.LOGIN_FAILED, "Error", Alert.AlertType.ERROR);
        }
    }

    /**
     * Access button handler for Acccount Request
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