package app.tracktune.controller.authenticatedUser;

import app.tracktune.controller.Controller;
import app.tracktune.model.user.AuthenticatedUser;
import app.tracktune.utils.BrowserManager;
import app.tracktune.utils.Frames;
import app.tracktune.utils.ResourceManager;
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
    /**
     * The main container StackPane where the dashboard content is displayed.
     */
    @FXML
    public StackPane mainContent;

    /**
     * Label to display a welcome message, including the authenticated user's name.
     */
    @FXML
    private Label LblWelcome;

    /**
     * Reference to the initial dashboard content Node cached for later use.
     */
    private Node dashboardContent;

    /**
     * Initializes the controller after the root element has been
     * completely processed.
     * <p>
     * Initializes resource and browser managers, caches the initial
     * dashboard content node, and updates the welcome label with the
     * current authenticated user's full name if present in the session.
     * </p>
     * @param location  The location used to resolve relative paths for
     *                  the root object, or null if unknown.
     * @param resources The resources used to localize the root object,
     *                  or null if not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ResourceManager.initialize();
        BrowserManager.initialize();
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

    /**
     * Handles the action to display the "Me" view in the main content area.
     * It updates the mainContent pane by loading the view specified by ME_VIEW_PATH.
     * If an error occurs during loading, an error alert is shown and the exception message is logged.
     */
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