package app.tracktune.controller.authenticatedUser;

import app.tracktune.controller.Controller;
import app.tracktune.model.user.AuthenticatedUser;
import app.tracktune.utils.Frames;
import app.tracktune.utils.Strings;
import app.tracktune.view.ViewManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

public class AuthenticatedUserDashboardController extends Controller implements Initializable {
    @FXML public StackPane mainContent;
    @FXML private Label LblWelcome;
    private Node dashboardContent;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dashboardContent = mainContent.getChildren().getFirst();
        if (ViewManager.getSessionUser() instanceof AuthenticatedUser authenticatedUser) {
            LblWelcome.setText(LblWelcome.getText() + " " + authenticatedUser.getName()+" "+authenticatedUser.getSurname());
        }
    }

    /**
     * Loads and displays the dashboard view by updating the main content area by initial content
     */
    @FXML
    private void handleDashboard(){
        try{
            mainContent.getChildren().setAll(dashboardContent);
        } catch(Exception e) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
            System.err.println(e.getMessage());
        }
    }

    /**
     * Loads and displays the discover view by updating the main content area
     */
    @FXML
    public void handleDiscover() {
        try{
            ViewManager.setMainContent(Frames.DISCOVER_VIEW_PATH, mainContent, this);
        }catch(Exception e){
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
            System.err.println(e.getMessage());
        }
    }

    /**
     * Loads and displays the tracks view by updating the main content area
     */
    @FXML
    public void handleResources(){
        try{
            ViewManager.setMainContent(Frames.MY_RESOURCES_VIEW_PATH, mainContent, this);
        }catch(Exception e){
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
            System.err.println(e.getMessage());
        }
    }

    /**
     * Loads and displays the dashboard view by updating the main content area by initial content
     */
    @FXML
    private void handleTracks(){
        try{
            ViewManager.setMainContent(Frames.TRACKS_VIEW_PATH_VIEW_PATH, mainContent, this);
        } catch(Exception e) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
            System.err.println(e.getMessage());
        }
    }

    @FXML
    public void handleMe(){
        try{
            ViewManager.setMainContent(Frames.ME_VIEW_PATH, mainContent, this);
        }catch(Exception e){
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
            System.err.println(e.getMessage());
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
            System.err.println(e.getMessage());
        }
    }
}