package app.tracktune.controller;

import app.tracktune.Main;
import app.tracktune.exceptions.TrackTuneException;
import app.tracktune.model.user.User;
import app.tracktune.model.user.UserDAO;
import app.tracktune.utils.Strings;
import app.tracktune.view.ViewManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
    private final UserDAO userDAO;

    /**
     * Default constructor to instance the user data access object
     */
    public LoginController() {
        userDAO = new UserDAO();
    }

    @FXML
    private TextField TxtEmail;
    @FXML
    private PasswordField TxtPassword;

    /**
     * Access button handler for login
     */
    @FXML
    private void handleLogin() {
        try{
            String username = TxtEmail.getText();
            String password = TxtPassword.getText();

            if(isInputValid(username, password)){
                User user = userDAO.getUser(username);
                if(user != null){
                    ViewManager.redirectView(Strings.DASHBOARD_VIEW);
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
     * Check input fields from the user
     * @param username : Username input from the user
     * @param password : Password input from the user
     * @return true or false
     */
    private boolean isInputValid(String username, String password){
        boolean result = true;
        if (username.isEmpty() || password.isEmpty()) {
            result = false;
        }
        return result;
    }
}