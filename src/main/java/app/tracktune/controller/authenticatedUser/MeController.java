package app.tracktune.controller.authenticatedUser;

import app.tracktune.controller.Controller;
import app.tracktune.controller.SessionManager;
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

    @FXML
    private Label LblRole;
    @FXML
    private TextField TxtUsername;
    @FXML
    private TextField TxtName;
    @FXML
    private TextField TxtSurname;
    @FXML
    private Button EditRole;

    private UserDAO userDAO = new UserDAO();
    private AuthenticatedUser user;
    private boolean editable = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (ViewManager.getSessionUser() instanceof AuthenticatedUser) {
            user = (AuthenticatedUser) ViewManager.getSessionUser();
            LblRole.setText("USER");
        } else {
            LblRole.setText("ADMIN");
        }

        TxtUsername.setText(user.getUsername());
        TxtName.setText(user.getName());
        TxtSurname.setText(user.getSurname());
    }

    @FXML
    public void handleEditButton() {
        if (editable) {
            try {
                String name = TxtName.getText().trim();
                String surname = TxtSurname.getText().trim();
                String username = TxtUsername.getText().trim();

                if(!name.equals(user.getName()) || !surname.equals(user.getSurname()) || !username.equals(user.getUsername())){
                    if (username.isEmpty() || name.isEmpty() || surname.isEmpty()) {
                        throw new TrackTuneException(Strings.USER_PWD_EMPTY);
                    }

                    if (SQLiteScripts.checkForSQLInjection(name, surname, username)) {
                        throw new SQLInjectionException(Strings.ERR_SQL_INJECTION);
                    }

                    AuthenticatedUser existingUser = (AuthenticatedUser) userDAO.getByUsername(username);
                    if (existingUser != null && !existingUser.getId().equals(user.getId())) {
                        throw new TrackTuneException(Strings.ERR_USER_ALREADY_EXISTS);
                    }

                    AuthenticatedUser updatedUser = new AuthenticatedUser(
                            username,
                            user.getPassword(),
                            name,
                            surname,
                            user.getStatus(),
                            user.getCreationDate()
                    );

                    userDAO.updateById(updatedUser, user.getId());
                    user = updatedUser;

                    SessionManager.reset();
                    ViewManager.initSessionManager(user);
                }

                TxtUsername.setText(user.getUsername());
                TxtName.setText(user.getName());
                TxtSurname.setText(user.getSurname());

                TxtUsername.setDisable(true);
                TxtName.setDisable(true);
                TxtSurname.setDisable(true);

                EditRole.setText("Edit");
                EditRole.getStyleClass().removeAll("accept-button");
                EditRole.getStyleClass().add("delete-button");

            } catch (TrackTuneException e) {
                ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERR_ME, e.getMessage(), Alert.AlertType.ERROR);
            } catch (Exception e) {
                ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERR_ME, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
            }
        } else {
            TxtUsername.setDisable(false);
            TxtName.setDisable(false);
            TxtSurname.setDisable(false);

            EditRole.setText("Save");
            EditRole.getStyleClass().removeAll("delete-button");
            EditRole.getStyleClass().add("accept-button");
        }

        editable = !editable;
    }
}
