package app.tracktune.controller;

import app.tracktune.exceptions.UserAlreadyExistsException;
import app.tracktune.model.user.User;
import app.tracktune.model.user.UserDAO;
import app.tracktune.utils.Strings;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
    private final UserDAO userDAO;
    private Alert errorAlert;

    public LoginController() {
        userDAO = new UserDAO();
    }

    @FXML
    private TextField TxtEmail;
    @FXML
    private PasswordField TxtPassword;
    @FXML
    private void handleLogin() {
        try{
            String username = TxtEmail.getText();
            String password = TxtPassword.getText();

            if(isInputValid(username, password)){
                User user = userDAO.getUser(username);
                if(user != null){
                    userDAO.saveUser(User.create(username, password));
                }
                throw new UserAlreadyExistsException(Strings.ERR_USER_ALREADY_EXISTS);
            }
        }catch(RuntimeException e){
            setAlert(Strings.LOGIN_FAILED, Strings.ERROR, e.getMessage());
        }
    }

    private boolean isInputValid(String username, String password){
        boolean result = true;
        if (username.isEmpty() || password.isEmpty()) {
            setAlert(Strings.LOGIN_FAILED, Strings.ERROR, Strings.USER_PWD_EMPTY);
            result = false;
        }
        return result;
    }

    private void setAlert(String title, String header, String content){
        errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setTitle(title);
        errorAlert.setHeaderText(header);
        errorAlert.setContentText(content);
        errorAlert.showAndWait();
    }
}