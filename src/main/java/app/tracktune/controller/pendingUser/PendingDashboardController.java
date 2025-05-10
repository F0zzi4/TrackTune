package app.tracktune.controller.pendingUser;

import app.tracktune.model.user.PendingUser;
import app.tracktune.view.ViewManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class PendingDashboardController implements Initializable {
    @FXML
    private Label LbStatusValue;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Object user = ViewManager.getUser();

        if (user instanceof PendingUser) {
            PendingUser pendingUser = (PendingUser) user;
            int status = pendingUser.getStatus().ordinal();
            switch (status) {
                case 0 -> LbStatusValue.setStyle("-fx-text-fill: orange;");
                case 2 -> LbStatusValue.setStyle("-fx-text-fill: #870505;");
            }

            LbStatusValue.setText(pendingUser.getStatus().toString());
        } else {
            LbStatusValue.setText("Utente non valido");
            LbStatusValue.setStyle("-fx-text-fill: gray;");
        }
    }

    @FXML
    public void handleLogout() {
        ViewManager.navigateToLogin();
    }
}