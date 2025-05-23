package app.tracktune.controller.authenticatedUser;

import app.tracktune.controller.Controller;
import app.tracktune.controller.authentication.SessionManager;
import app.tracktune.exceptions.SQLInjectionException;
import app.tracktune.exceptions.TrackTuneException;
import app.tracktune.model.user.AuthenticatedUser;
import app.tracktune.model.user.UserDAO;
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

    @FXML private Label lblRole;
    @FXML private TextField txtUsername;
    @FXML private TextField txtName;
    @FXML private TextField txtSurname;
    @FXML private Button editRole;
    @FXML private Label lblStatus;

    private final UserDAO userDAO = new UserDAO();
    private AuthenticatedUser user;
    private boolean editable = false;

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

                    AuthenticatedUser existingUser = (AuthenticatedUser) userDAO.getActiveUserByUsername(username);
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

                    userDAO.updateById(authenticatedUser, user.getId());

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
