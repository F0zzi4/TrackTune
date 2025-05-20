package app.tracktune.view;

import app.tracktune.Main;
import app.tracktune.config.AppConfig;
import app.tracktune.controller.Controller;
import app.tracktune.controller.authentication.SessionManager;
import app.tracktune.model.user.User;
import app.tracktune.utils.Frames;
import app.tracktune.utils.Strings;
import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;

import static app.tracktune.Main.root;

/**
 * Dedicated class to manage view section in MVC pattern
 */
public class ViewManager {
    private static SessionManager sessionManager;

    /**
     * Load basic configuration for the given root
     * @param viewPath : path to the desired view
     * @throws IOException : Input / Output Exception
     */
    public static void initView(String viewPath) throws IOException{
        FXMLLoader viewLoader = new FXMLLoader(Main.class.getResource(viewPath));
        Scene scene = new Scene(viewLoader.load(), 700, 550);
        setStageOnCurrentScreen(root, Frames.LOGIN_FRAME_WIDTH, Frames.LOGIN_FRAME_HEIGHT);
        root.setTitle(AppConfig.APP_TITLE);
        root.setResizable(false);
        root.getIcons().addAll(
                new Image(Main.class.getResource(Frames.MAIN_ICON_192_PATH ).toExternalForm()),
                new Image(Main.class.getResource(Frames.MAIN_ICON_256_PATH ).toExternalForm()),
                new Image(Main.class.getResource(Frames.MAIN_ICON_PATH).toExternalForm())
        );
        root.setScene(scene);
        root.show();
    }

    /**
     * Redirect the current view to a new one with a fade transition
     * @param viewPath : view path to be redirected
     */
    public static void redirectView(String viewPath, double frameWidth, double frameHeight) {
        // Fade out current scene
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), root.getScene().getRoot());
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        fadeOut.setOnFinished(e -> {
            try {
                // Load the new scene
                FXMLLoader loader = new FXMLLoader(Main.class.getResource(viewPath));
                Parent newRoot = loader.load();

                // Fade in new scene
                FadeTransition fadeIn = new FadeTransition(Duration.millis(500), newRoot);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);

                // Set up the new scene
                Scene newScene = new Scene(newRoot);
                root.setScene(newScene);
                setStageOnCurrentScreen(root, frameWidth, frameHeight);
                fadeIn.play();
            } catch (IOException ex) {
                System.err.println(ex.getMessage()+"\n"+ Arrays.toString(ex.getCause().getStackTrace()));
            }
        });

        fadeOut.play();
    }

    /**
     * Set the given stage on the current screen
     * @param stage : Stage that will be set
     * @param width : Frame width
     * @param height : Frame height
     */
    private static void setStageOnCurrentScreen(Stage stage, double width, double height){
        // Get current mouse position
        Point mousePoint = MouseInfo.getPointerInfo().getLocation();
        // Get the mouse containing screen
        Screen targetScreen = Screen.getScreens()
                .stream()
                .filter(screen -> screen.getBounds().contains(mousePoint.x, mousePoint.y))
                .findFirst()
                .orElse(Screen.getPrimary());
        // Set the root window at the exact center of the screen
        Rectangle2D bounds = targetScreen.getVisualBounds();
        stage.setX(bounds.getMinX() + (bounds.getWidth() - width) / 2);
        stage.setY(bounds.getMinY() + (bounds.getHeight() - height) / 2);
    }

    /**
     * Set and show a specific alert
     * @param title : Title to be shown
     * @param header : Header to be shown
     * @param content : Content to be shown
     * @param type : Type of alert
     */
    public static void setAndShowAlert(String title, String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.initOwner(root);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(Main.class.getResource("/style/alert-style.css").toExternalForm());
        dialogPane.getStyleClass().add("custom-alert");

        alert.showAndWait();
    }

    public static boolean setAndGetConfirmAlert(String title, String header, String content) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle(title);
        confirmAlert.setHeaderText(header);
        confirmAlert.setContentText(content);
        confirmAlert.initOwner(root);

        ButtonType yesButton = new ButtonType(Strings.DELETE, ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType(Strings.CANCEL, ButtonBar.ButtonData.NO);

        confirmAlert.getButtonTypes().setAll(yesButton, noButton);

        DialogPane dialogPane = confirmAlert.getDialogPane();
        dialogPane.getStylesheets().add(Main.class.getResource("/style/alert-style.css").toExternalForm());
        dialogPane.getStyleClass().add("custom-alert");

        return confirmAlert.showAndWait().filter(response -> response == yesButton).isPresent();
    }

    public static void navigateToLogin(){redirectView(Frames.LOGIN_VIEW_PATH, Frames.LOGIN_FRAME_WIDTH, Frames.LOGIN_FRAME_HEIGHT);}

    public static void navigateToAdminDashboard(){redirectView(Frames.ADMIN_DASHBOARD_VIEW_PATH, Frames.DASHBOARD_FRAME_WIDTH, Frames.DASHBOARD_FRAME_HEIGHT);}

    public static void navigateToUserDashboard(){redirectView(Frames.USER_DASHBOARD_VIEW_PATH, Frames.DASHBOARD_FRAME_WIDTH, Frames.DASHBOARD_FRAME_HEIGHT);}

    public static void navigateToAccountRequest(){redirectView(Frames.ACCOUNT_REQUEST_VIEW_PATH, Frames.ACCOUNT_REQUEST_FRAME_WIDTH, Frames.ACCOUNT_REQUEST_FRAME_HEIGHT);}

    public static void navigateToPendingUserDashboard(){redirectView(Frames.PENDING_DASHBOARD_VIEW_PATH, Frames.DASHBOARD_FRAME_WIDTH, Frames.DASHBOARD_FRAME_HEIGHT);}

    /**
     * Sets the main content area of the dashboard to display a new view.
     * The new view is loaded from the specified FXML file path.
     *
     * @param contentPath Path to the FXML file to load and display in the main content area
     * @param mainContent The main content node from the entrypoint of the view (dashboard or something like that)
     * @param parentController If there is a child controller loaded by a parent, the parent has to be passed as parameter to the child
     */
    public static void setMainContent(String contentPath, StackPane mainContent, Controller parentController) {
        try {
            FXMLLoader loader = new FXMLLoader(ViewManager.class.getResource(contentPath));
            Parent view = loader.load();

            Controller controller = loader.getController();
            controller.setParentController(parentController);

            mainContent.getChildren().setAll(view);
        } catch (IOException e) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
            System.err.println(e.getMessage());
        }
    }

    public static void initSessionManager(User sessionUser){
        SessionManager.initialize(sessionUser);
        sessionManager = SessionManager.getInstance();
    }

    public static User getSessionUser(){
        User sessionUser = null;
        if(sessionManager != null)
            sessionUser = sessionManager.getUser();
        return sessionUser;
    }

    public static void logout(){
        SessionManager.reset();
        navigateToLogin();
    }
}
