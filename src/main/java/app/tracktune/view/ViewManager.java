package app.tracktune.view;

import app.tracktune.Main;
import app.tracktune.config.AppConfig;
import app.tracktune.controller.SessionManager;
import app.tracktune.model.user.User;
import app.tracktune.utils.Frames;
import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.*;
import java.io.IOException;

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
        Image icon = new Image(Main.class.getResource(Frames.MAIN_ICON_PATH).toExternalForm());
        setStageOnCurrentScreen(root, Frames.LOGIN_FRAME_WIDTH, Frames.LOGIN_FRAME_HEIGHT);
        root.setTitle(AppConfig.APP_TITLE);
        root.setResizable(false);
        root.getIcons().add(icon);
        root.setScene(scene);
        root.show();
    }

    /**
     * Redirect the current view to a new one with a fade transition
     * @param viewPath : view path to be redirected
     */
    public static void redirectView(String viewPath) {
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
                setStageOnCurrentScreen(root, Frames.DASHBOARD_FRAME_WIDTH, Frames.DASHBOARD_FRAME_HEIGHT);
                fadeIn.play();
            } catch (IOException ex) {
                System.err.println(ex.getMessage());
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
    public static void setAndShowAlert(String title, String header, String content, Alert.AlertType type){
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.initOwner(root);
        alert.showAndWait();
    }

    public static void navigateToLogin(){
        redirectView(Frames.LOGIN_VIEW_PATH);
    }

    public static void navigateToAdminDashboard(){
        redirectView(Frames.ADMIN_DASHBOARD_VIEW_PATH);
    }

    public static void navigateToUserDashboard(){
        redirectView(Frames.USER_DASHBOARD_VIEW_PATH);
    }

    public static void navigateToAccountRequest(){redirectView(Frames.REQUEST_VIEW_PATH);}

    public static void navigateToPendingUserDashboard(){redirectView(Frames.PENDING_DASHBOARD_VIEW_PATH);}

    public static void initSessionManager(User sessionUser){
        SessionManager.initialize(sessionUser);
    }
    public static User getSessionUser(){
        User sessionUser = null;
        if(sessionManager != null)
            sessionUser = sessionManager.getUser();
        return sessionUser;
    }
}
