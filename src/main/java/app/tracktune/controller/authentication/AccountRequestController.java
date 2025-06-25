package app.tracktune.controller.authentication;

import app.tracktune.controller.Controller;
import app.tracktune.exceptions.EntityAlreadyExistsException;
import app.tracktune.exceptions.SQLInjectionException;
import app.tracktune.exceptions.TrackTuneException;
import app.tracktune.utils.DatabaseManager;
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
    /**
     * Text field for entering the username of the user.
     */
    @FXML
    private TextField TxtUsername;

    /**
     * Password field for entering the user's password.
     */
    @FXML
    private PasswordField TxtPassword;

    /**
     * Text field for entering the user's first name.
     */
    @FXML
    private TextField TxtName;

    /**
     * Text field for entering the user's surname.
     */
    @FXML
    private TextField TxtSurname;

    /**
     * Handles the submission of a new account request.
     * <p>
     * This method collects user input from text fields (username, password, name, and surname),
     * validates it, checks for SQL injection attempts, and verifies that the username does not already
     * exist either as a pending request or as an active user. If all checks pass, a new {@link PendingUser}
     * is created and inserted into the database with status {@code CREATED}, and the session is initialized
     * with the pending user before navigating to the pending user dashboard.
     * </p>
     *
     * @throws TrackTuneException if any input is invalid or a user with the same username already exists.
     * @throws SQLInjectionException if potential SQL injection patterns are detected in the input.
     */
    @FXML
    private void handleRequest(){
        try{
            String username = TxtUsername.getText().trim();
            String password = TxtPassword.getText().trim();
            String name = TxtName.getText().trim();
            String surname = TxtSurname.getText().trim();

            if(!isInputValid(username, password, name, surname))
                throw new TrackTuneException(Strings.FIELD_EMPTY);

            if(SQLiteScripts.checkForSQLInjection(username, password, name, surname))
                throw new SQLInjectionException(Strings.ERR_SQL_INJECTION);

            if(DatabaseManager.getDAOProvider().getPendingUserDAO().getByUsername(username) != null)
                throw new EntityAlreadyExistsException(Strings.ERR_REQUEST_ALREADY_EXISTS);

            if(DatabaseManager.getDAOProvider().getUserDAO().getActiveUserByUsername(username) != null)
                throw new EntityAlreadyExistsException(Strings.ERR_USER_ALREADY_EXISTS);
            PendingUser pendingUser = new PendingUser(
                    username,
                    password,
                    Controller.toTitleCase(name),
                    Controller.toTitleCase(surname),
                    new Timestamp(System.currentTimeMillis()),
                    AuthRequestStatusEnum.CREATED
            );
            DatabaseManager.getDAOProvider().getPendingUserDAO().insert(pendingUser);
            ViewManager.initSessionManager(pendingUser);
            ViewManager.navigateToPendingUserDashboard();
        }catch(TrackTuneException e) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERR_ACCOUNT_REQUEST, e.getMessage(), Alert.AlertType.ERROR);
        }
        catch(Exception e){
            System.err.println(e.getMessage());
        }
    }

    /**
     * Handles the action of returning to the login screen.
     * <p>
     * This method is typically triggered when the user clicks a "Back" or "Return" button
     * on the registration or request screen. It navigates the application view back to the login screen.
     * </p>
     */
    @FXML
    private void handleReturn() {
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
