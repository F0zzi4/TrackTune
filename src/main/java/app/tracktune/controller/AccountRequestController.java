package app.tracktune.controller;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.awt.*;

public class AccountRequestController {
    @FXML
    private TextField TxtUsername;
    @FXML
    private PasswordField TxtPassword;
    @FXML
    private TextField TxtName;
    @FXML
    private TextField TxtSurname;

    /**
     * Access button handler for Account Request
     */
    @FXML
    private void handleRequest(){
        String username = TxtUsername.getText();
        String password = TxtPassword.getText();
        String name = TxtName.getText();
        String surname = TxtSurname.getText();

        if(isInputValid(username, password, name, surname)){

        }
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
