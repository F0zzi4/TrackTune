package app.tracktune.controller.admin;

import app.tracktune.controller.Controller;
import app.tracktune.model.user.Administrator;
import app.tracktune.utils.Frames;
import app.tracktune.utils.Strings;
import app.tracktune.view.ViewManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for managing the admin dashboard and their related functionalities.
 * The dashboard includes a media player to display a video, handles user logout,
 * and supports switching between different views in the main content
 */
public class AdminDashboardController extends Controller implements Initializable {
    @FXML public StackPane mainContent;
    private Administrator admin;
    private Node dashboardContent;

    /**
     * Initializes the controller by checking if the logged-in user is an {@link Administrator}
     * and assigns it to the {@code admin} variable.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dashboardContent = mainContent.getChildren().getFirst();
        if (ViewManager.getSessionUser() instanceof Administrator administrator) {
            admin = administrator;
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

    /*
     * Loads and displays the Genre view by updating the main content area of the dashboard
     */
    @FXML
    private void handleGenre(){
        try{
            ViewManager.setMainContent(Frames.GENRES_VIEW_PATH_VIEW_PATH, mainContent, this);
        } catch(Exception e) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
            System.err.println(e.getMessage());
        }
    }

    /**
     * Loads and displays the requests view by updating the main content area of the dashboard
     */
    @FXML
    public void handleRequests() {
        ViewManager.setMainContent(Frames.REQUESTS_VIEW_PATH, mainContent, this);
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

    /**
     * Loads and displays the authors view by updating the main content area of the dashboard
     */
    @FXML
    public void handleAuthors() {
        ViewManager.setMainContent(Frames.AUTHORS_PATH, mainContent, this);
    }

    /**
     * Loads and displays the musical instruments view by updating the main content area of the dashboard
     */
    @FXML
    public void handleInstruments() {
        ViewManager.setMainContent(Frames.INSTRUMENTS_PATH, mainContent, this);
    }

    /**
     * Loads and displays the user management view by updating the main content area of the dashboard
     */
    @FXML
    public void handleUserManagement() {
        ViewManager.setMainContent(Frames.USER_MANAGEMENT_VIEW_PATH, mainContent, this);
    }

    /**
     * Logs out the current user by calling the {@link ViewManager#logout()} method.
     * Displays an error alert if the logout process fails
     */
    @FXML
    public void handleLogout() {
        try{
            ViewManager.logout();
        } catch(Exception e) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
            System.err.println(e.getMessage());
        }
    }
}
