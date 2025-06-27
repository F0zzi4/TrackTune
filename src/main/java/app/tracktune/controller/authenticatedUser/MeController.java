package app.tracktune.controller.authenticatedUser;

import app.tracktune.controller.Controller;
import app.tracktune.model.user.PendingUser;
import app.tracktune.utils.SessionManager;
import app.tracktune.exceptions.SQLInjectionException;
import app.tracktune.exceptions.TrackTuneException;
import app.tracktune.utils.DatabaseManager;
import app.tracktune.model.user.AuthenticatedUser;
import app.tracktune.utils.SQLiteScripts;
import app.tracktune.utils.Strings;
import app.tracktune.view.ViewManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class MeController extends Controller implements Initializable {
    /**
     * Label displaying the user's role (e.g., Administrator, Regular User).
     */
    @FXML private Label lblRole;

    /**
     * TextField displaying the username of the current user.
     * Usually read-only unless in edit mode.
     */
    @FXML private TextField txtUsername;

    /**
     * TextField displaying the first name of the user.
     * Can be edited depending on the context.
     */
    @FXML private TextField txtName;

    /**
     * TextField displaying the surname (last name) of the user.
     * Editable if the user is allowed to modify personal data.
     */
    @FXML private TextField txtSurname;

    /**
     * Button that allows editing the user's role.
     * Typically available only to administrators.
     */
    @FXML private Button editRole;

    /**
     * Label displaying the current status of the user (e.g., ACTIVE, SUSPENDED).
     */
    @FXML private Label lblStatus;

    /**
     * The currently displayed or selected authenticated user whose details are being viewed or edited.
     */
    private AuthenticatedUser user;

    /**
     * Flag indicating whether the user details are currently editable in the UI.
     */
    private boolean editable = false;

    /**
     * Initializes the controller after its root element has been completely processed.
     * <p>
     * This method sets the UI fields based on the currently logged-in session user.
     * If the user is an {@link AuthenticatedUser}, it initializes the form with their details.
     * Otherwise, it assumes the user is an administrator and sets the role label accordingly.
     *
     * @param url the location used to resolve relative paths for the root object, or {@code null} if not known
     * @param resourceBundle the resources used to localize the root object, or {@code null} if not localized
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (ViewManager.getSessionUser() instanceof AuthenticatedUser) {
            user = (AuthenticatedUser) ViewManager.getSessionUser();
            lblRole.setText(Strings.USER);
        } else {
            lblRole.setText(Strings.ADMIN);
        }

        txtUsername.setText(user.getUsername());
        txtName.setText(user.getName());
        txtSurname.setText(user.getSurname());
        lblStatus.setText(user.getStatus().toString());
    }

    /**
     * Handles the edit/save button logic for the user profile.
     * <p>
     * When in editable mode, the method validates and saves the updated user information.
     * It checks for empty fields, possible SQL injection, and username conflicts.
     * If the data is valid and has changed, it updates the user in the database
     * and refreshes the session.
     * <p>
     * When not in editable mode, it enables the input fields for editing.
     * Exceptions during validation or database operations are caught and
     * an error alert is shown.
     */
    @FXML
    public void handleEditButton() {
        if (editable) {
            try {
                String name = txtName.getText().trim();
                String surname = txtSurname.getText().trim();
                String username = txtUsername.getText().trim();

                if(!name.equals(user.getName()) || !surname.equals(user.getSurname()) || !username.equals(user.getUsername())){
                    if (username.isEmpty() || name.isEmpty() || surname.isEmpty()) {
                        throw new TrackTuneException(Strings.USER_PWD_EMPTY);
                    }

                    if (SQLiteScripts.checkForSQLInjection(name, surname, username)) {
                        throw new SQLInjectionException(Strings.ERR_SQL_INJECTION);
                    }

                    AuthenticatedUser existingUser = (AuthenticatedUser) DatabaseManager.getDAOProvider().getUserDAO().getActiveUserByUsername(username);
                    if (existingUser != null && !existingUser.getId().equals(user.getId())) {
                        throw new TrackTuneException(Strings.ERR_USER_ALREADY_EXISTS);
                    }

                    AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                            user.getId(),
                            username,
                            user.getPassword(),
                            name,
                            surname,
                            user.getStatus(),
                            user.getCreationDate()
                    );

                    DatabaseManager.getDAOProvider().getUserDAO().updateById(authenticatedUser, user.getId());
                    DatabaseManager.getDAOProvider().getPendingUserDAO().updateUsername(authenticatedUser.getUsername(), SessionManager.getInstance().getUser().getUsername());
                    SessionManager.reset();
                    ViewManager.initSessionManager(authenticatedUser);
                }

                txtUsername.setText(username);
                txtName.setText(name);
                txtSurname.setText(surname);

                txtUsername.setDisable(true);
                txtName.setDisable(true);
                txtSurname.setDisable(true);

                editRole.setText(Strings.EDIT);

            } catch (TrackTuneException e) {
                ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, e.getMessage(), Alert.AlertType.ERROR);
            } catch (Exception e) {
                ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
            }
        } else {
            txtUsername.setDisable(false);
            txtName.setDisable(false);
            txtSurname.setDisable(false);

            editRole.setText(Strings.SAVE);
        }

        editable = !editable;
    }
}
