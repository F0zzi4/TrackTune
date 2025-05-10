package app.tracktune.controller;

import app.tracktune.exceptions.PendingUserAlreadyExistsException;
import app.tracktune.exceptions.TrackTuneException;
import app.tracktune.model.user.*;
import app.tracktune.utils.Strings;
import app.tracktune.view.ViewManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.awt.*;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.Timestamp;

public class AccountRequestController {
    private final PendingUserDAO pendingUserDAO;

    @FXML
    private TextField TxtUsername;
    @FXML
    private PasswordField TxtPassword;
    @FXML
    private TextField TxtName;
    @FXML
    private TextField TxtSurname;

    public AccountRequestController() {
        pendingUserDAO = new PendingUserDAO();
    }

    /**
     * Access button handler for Account Request
     */
    @FXML
    private void handleRequest(){
        String username = TxtUsername.getText();
        String password = TxtPassword.getText();
        String name = TxtName.getText();
        String surname = TxtSurname.getText();

            // Check if the username has already requested an account
            try{
                if(isInputValid(username, password, name, surname)){
                    PendingUser existingUser = pendingUserDAO.getByKey(username);
                    if (existingUser == null) {
                        PendingUser newUser = new PendingUser(
                                username,
                                password,
                                name,
                                surname,
                                new Timestamp(System.currentTimeMillis()),
                                AuthRequestStatusEnum.CREATED
                        );
                        pendingUserDAO.insert(newUser);
                        ViewManager.setUser(newUser);
                        ViewManager.navigateToPendingDashboard();
                    }else
                        throw new PendingUserAlreadyExistsException(Strings.ERR_PENDING_USER_ALREADY_EXISTS);
                }
                else
                    throw new PendingUserAlreadyExistsException(Strings.FIELD_EMPTY);
            }catch(PendingUserAlreadyExistsException e) {
                ViewManager.setAndShowAlert(Strings.ERROR, Strings.LOGIN_FAILED, e.getMessage(), Alert.AlertType.ERROR);
            }
            catch(Exception e){
                System.err.println(e.getMessage());
            }
    }

    @FXML
    private void handleReturn(){
        ViewManager.navigateToLogin();
    }

    /**
     * Check input fields from the user
     * @param username : Username input from the user
     * @param password : Password input from the user
     * @return true or false
     */
    private boolean isInputValid(String username, String password, String name, String surname){
        return !username.isEmpty() && !password.isEmpty() && !name.isEmpty() && !surname.isEmpty();
    }
}
