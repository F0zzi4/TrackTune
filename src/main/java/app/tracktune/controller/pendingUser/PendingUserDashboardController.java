package app.tracktune.controller.pendingUser;

import app.tracktune.model.user.PendingUser;
import app.tracktune.utils.Strings;
import app.tracktune.view.ViewManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class PendingUserDashboardController implements Initializable {
    @FXML
    public Label LblStatusValue;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (ViewManager.getSessionUser() instanceof PendingUser pendingUser) {
            int status = pendingUser.getStatus().ordinal();
            switch (status) {
                case 0 -> LblStatusValue.setStyle("-fx-text-fill: #432d29;");
                case 2 -> LblStatusValue.setStyle("-fx-text-fill: #870505;");
            }

            LblStatusValue.setText(pendingUser.getStatus().toString());
        } else {
            LblStatusValue.setText(Strings.ERROR);
            LblStatusValue.setStyle("-fx-text-fill: #870505;");
        }
    }

    /**
     * Logs out the current user by calling the {@link ViewManager#logout()} method.
     * Displays an error alert if the logout process fails.
     */
    @FXML
    public void handleLogout() {
        try{
            ViewManager.logout();
        }catch(Exception e){
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
        }
    }
}