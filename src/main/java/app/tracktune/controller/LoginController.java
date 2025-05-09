package app.tracktune.controller;

import app.tracktune.exceptions.TrackTuneException;
import app.tracktune.model.user.User;
import app.tracktune.model.user.UserDAO;
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
    @FXML
    private TextField TxtUsername;
    @FXML
    private PasswordField TxtPassword;

    /**
     * Default constructor to instance the user data access object
     */
    public LoginController() {userDAO = new UserDAO();}

    /**
     * Access button handler for login
     */
    @FXML
    private void handleLogin() {
        try{
            String username = TxtUsername.getText();
            String password = TxtPassword.getText();

            if(isInputValid(username, password)){
                User user = userDAO.getUser(username);
                if(user != null){
                    ViewManager.navigateToDashboard();
                }else
                    throw new TrackTuneException(Strings.ERR_USER_NOT_FOUND);
            }else
                throw new TrackTuneException(Strings.USER_PWD_EMPTY);
        }catch(TrackTuneException e){
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.LOGIN_FAILED, e.getMessage(), Alert.AlertType.ERROR);
        }catch(Exception e){
            System.err.println(e.getMessage());
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