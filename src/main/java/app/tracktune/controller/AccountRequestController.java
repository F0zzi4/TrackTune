package app.tracktune.controller;

import app.tracktune.exceptions.SQLInjectionException;
import app.tracktune.exceptions.TrackTuneException;
import app.tracktune.model.user.*;
import app.tracktune.utils.SQLiteScripts;
import app.tracktune.utils.Strings;
import app.tracktune.view.ViewManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.sql.Timestamp;

public class AccountRequestController extends Controller {
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
     * Access button handler for account request
     */
    @FXML
    private void handleRequest(){
        try{
            String username = TxtUsername.getText();
            String password = TxtPassword.getText();
            String name = TxtName.getText();
            String surname = TxtSurname.getText();

            if(!isInputValid(username, password, name, surname))
                throw new TrackTuneException(Strings.FIELD_EMPTY);

            if(SQLiteScripts.checkForSQLInjection(username, password, name, surname))
                throw new SQLInjectionException(Strings.ERR_SQL_INJECTION);

            PendingUser pendingUser = new PendingUser(
                    username,
                    password,
                    name,
                    surname,
                    new Timestamp(System.currentTimeMillis()),
                    AuthRequestStatusEnum.CREATED
            );
            pendingUserDAO.insert(pendingUser);
            ViewManager.initSessionManager(pendingUser);
            ViewManager.navigateToPendingUserDashboard();
        }catch(TrackTuneException e) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERR_ACCOUNT_REQUEST, e.getMessage(), Alert.AlertType.ERROR);
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
     * @param name : Name input from the user
     * @param surname : Surname input from the user
     * @return true if all the fields are not blank, false otherwise
     */
    private boolean isInputValid(String username, String password, String name, String surname){
        return !username.isEmpty() && !password.isEmpty() && !name.isEmpty() && !surname.isEmpty();
    }
}
