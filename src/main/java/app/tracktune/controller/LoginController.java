package app.tracktune.controller;

import app.tracktune.utils.Strings;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class LoginController {
    @FXML
    private TextField TxtEmail;
    @FXML
    private TextField TxtPassword;
    @FXML
    private void handleLogin() {
        String username = TxtEmail.getText();
        String password = TxtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(Strings.LOGIN_FAILED);
            alert.setHeaderText(Strings.ERROR);
            alert.setContentText(Strings.USER_PWD_EMPTY);
            alert.showAndWait();
        }

    }
}