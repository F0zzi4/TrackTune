package app.tracktune.controller.pendingUser;

import app.tracktune.model.user.PendingUser;
import app.tracktune.utils.Strings;
import app.tracktune.view.ViewManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class PendingUserDashboardController implements Initializable {
    @FXML
    private Label LbStatusValue;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (ViewManager.getSessionUser() instanceof PendingUser pendingUser) {
            int status = pendingUser.getStatus().ordinal();
            switch (status) {
                case 0 -> LbStatusValue.setStyle("-fx-text-fill: #784d14;");
                case 2 -> LbStatusValue.setStyle("-fx-text-fill: #870505;");
            }

            LbStatusValue.setText(pendingUser.getStatus().toString());
        } else {
            LbStatusValue.setText(Strings.ERR_USER_NOT_ALLOWED);
            LbStatusValue.setStyle("-fx-text-fill: #504645;");
        }
    }

    @FXML
    public void handleLogout() {
        ViewManager.navigateToLogin();
    }
}