package app.tracktune.view;

import app.tracktune.Main;
import app.tracktune.config.AppConfig;
import app.tracktune.controller.Controller;
import app.tracktune.controller.common.ResourceFileController;
import app.tracktune.utils.SessionManager;
import app.tracktune.model.user.User;
import app.tracktune.utils.Frames;
import app.tracktune.utils.Strings;
import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import javafx.geometry.Insets;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import static app.tracktune.Main.root;

/**
 * Dedicated class to manage view part of the MVC pattern
 */
public class ViewManager {
    /**
     * Singleton instance that maintains the current user session
     * and provides session-related information to the view layer.
     */
    private static SessionManager sessionManager;

    /**
     * Initializes and displays a JavaFX view specified by the given FXML path.
     * <p>
     * Sets the stage with fixed dimensions, title, and icons.
     * Typically used for the login or initial views.
     *
     * @param viewPath the path to the FXML view file to be loaded.
     * @throws IOException if the FXML file cannot be loaded.
     */
    public static void initView(String viewPath) throws IOException{
        FXMLLoader viewLoader = new FXMLLoader(Main.class.getResource(viewPath));
        Scene scene = new Scene(viewLoader.load(), 700, 550);
        setStageOnCurrentScreen(root, Frames.LOGIN_FRAME_WIDTH, Frames.LOGIN_FRAME_HEIGHT);
        root.setTitle(AppConfig.APP_TITLE);
        root.setResizable(false);
        root.getIcons().addAll(
                new Image(Objects.requireNonNull(Main.class.getResource(Frames.MAIN_ICON_192_PATH)).toExternalForm()),
                new Image(Objects.requireNonNull(Main.class.getResource(Frames.MAIN_ICON_256_PATH)).toExternalForm()),
                new Image(Objects.requireNonNull(Main.class.getResource(Frames.MAIN_ICON_PATH)).toExternalForm())
        );
        root.setScene(scene);
        root.show();
    }

    /**
     * Redirects the application to a new JavaFX view with a smooth fade transition.
     * <p>
     * This method first fades out the current scene, loads the new scene from the provided FXML path,
     * and then fades in the new scene. It also adjusts the stage size and position based on the given dimensions.
     *
     * @param viewPath    the path to the FXML file of the new view
     * @param frameWidth  the desired width of the new stage frame
     * @param frameHeight the desired height of the new stage frame
     */
    public static void redirectView(String viewPath, double frameWidth, double frameHeight) {
        // Fade out current scene
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), root.getScene().getRoot());
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        if(ResourceFileController.mediaPlayer != null){
            ResourceFileController.mediaPlayer.dispose();
        }

        fadeOut.setOnFinished(_ -> {
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
        dialogPane.getStylesheets().add(Objects.requireNonNull(Main.class.getResource("/style/alert-style.css")).toExternalForm());
        dialogPane.getStyleClass().add("custom-alert");

        alert.showAndWait();
    }

    /**
     * Displays a confirmation dialog with custom title, header, and content text.
     * <p>
     * The dialog includes two buttons: one for cancellation and one for confirmation (typically "Delete").
     * Custom styles are applied from an external CSS stylesheet.
     *
     * @param title   the title of the alert dialog
     * @param header  the header text (can be null)
     * @param content the content text to display in the dialog
     * @return {@code true} if the user confirms (presses the confirm/delete button), {@code false} otherwise
     */
    public static boolean setAndGetConfirmAlert(String title, String header, String content) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle(title);
        confirmAlert.setHeaderText(header);
        confirmAlert.setContentText(content);
        confirmAlert.initOwner(root);

        ButtonType noButton = new ButtonType(Strings.CANCEL, ButtonBar.ButtonData.NO);
        ButtonType yesButton = new ButtonType(Strings.DELETE, ButtonBar.ButtonData.YES);

        confirmAlert.getButtonTypes().setAll(noButton, yesButton);

        DialogPane dialogPane = confirmAlert.getDialogPane();
        dialogPane.getStylesheets().add(Objects.requireNonNull(Main.class.getResource("/style/alert-style.css")).toExternalForm());
        dialogPane.getStyleClass().add("custom-alert");

        return confirmAlert.showAndWait().filter(response -> response == yesButton).isPresent();
    }

    /**
     * Displays a text input dialog prompting the user to reply to a comment.
     * <p>
     * A simple input box appears with a placeholder and custom style.
     *
     * @return an {@link Optional} containing the user's input if confirmed, or empty if cancelled
     */
    public static Optional<String> showReplyDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(Strings.REPLY_TO_COMMENT);
        dialog.setHeaderText(null);
        dialog.setContentText(Strings.WRITE_YOUR_REPLY);
        dialog.initOwner(root);

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(
                Objects.requireNonNull(Main.class.getResource("/style/alert-style.css")).toExternalForm()
        );
        dialogPane.getStyleClass().add("custom-alert");

        return dialog.showAndWait();
    }

    /**
     * Displays a custom dialog that allows the user to add a segment-based comment.
     * <p>
     * The dialog includes input fields for:
     * <ul>
     *   <li>Start time of the segment (required)</li>
     *   <li>End time of the segment (optional, defaults to "0" if empty)</li>
     *   <li>The comment text</li>
     * </ul>
     * Styled using a custom CSS theme, the dialog returns the user input upon confirmation.
     *
     * @return an {@link Optional} containing a {@code String[]} of three elements:
     *         <ol>
     *           <li>start time</li>
     *           <li>end time ("0" if not provided)</li>
     *           <li>comment text</li>
     *         </ol>
     *         or an empty {@code Optional} if the user cancels the dialog
     */
    public static Optional<String[]> showSegmentCommentDialog() {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle(Strings.COMMENT_ADD);
        dialog.setHeaderText(null);
        dialog.initOwner(root);

        TextField startField = new TextField();
        startField.setPromptText(Strings.START_SEGMENT_PLACEHOLDER);
        startField.getStyleClass().add("textField");

        TextField endField = new TextField();
        endField.setPromptText(Strings.END_SEGMENT_PLACEHOLDER);
        endField.getStyleClass().add("textField");

        TextArea commentArea = new TextArea();
        commentArea.setPromptText(Strings.PLACEHOLDER_COMMENT_TEXTAREA);
        commentArea.setPrefRowCount(3);
        commentArea.setWrapText(true);
        commentArea.getStyleClass().add("textArea");

        VBox content = new VBox(10,
                new Label(Strings.START_SEGMENT), startField,
                new Label(Strings.END_SEGMENT), endField,
                new Label(Strings.COMMENT), commentArea
        );
        content.setPadding(new Insets(10, 0, 0, 0));
        dialog.getDialogPane().setContent(content);

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(Objects.requireNonNull(Main.class.getResource("/style/alert-style.css")).toExternalForm());
        dialogPane.getStyleClass().add("custom-alert");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return new String[]{
                        startField.getText().trim(),
                        endField.getText().trim().isEmpty() ? "0" : endField.getText().trim(),
                        commentArea.getText().trim()
                };
            }
            return null;
        });

        return dialog.showAndWait();
    }

    /**
     * Navigates to the login view.
     * <p>
     * The stage is made non-resizable and not fullscreen before redirecting.
     */
    public static void navigateToLogin(){
        root.setResizable(false);
        root.setFullScreen(false);
        redirectView(Frames.LOGIN_VIEW_PATH, Frames.LOGIN_FRAME_WIDTH, Frames.LOGIN_FRAME_HEIGHT);
    }

    /**
     * Navigates to the account request view.
     * <p>
     * The stage is made non-resizable and not fullscreen before redirecting.
     */
    public static void navigateToAccountRequest(){
        root.setResizable(false);
        root.setFullScreen(false);
        redirectView(Frames.ACCOUNT_REQUEST_VIEW_PATH, Frames.ACCOUNT_REQUEST_FRAME_WIDTH, Frames.ACCOUNT_REQUEST_FRAME_HEIGHT);
    }

    /**
     * Navigates to the admin dashboard view.
     * <p>
     * The stage is made resizable before redirecting.
     */
    public static void navigateToAdminDashboard(){
        root.setResizable(true);
        redirectView(Frames.ADMIN_DASHBOARD_VIEW_PATH, Frames.DASHBOARD_FRAME_WIDTH, Frames.DASHBOARD_FRAME_HEIGHT);
    }

    /**
     * Navigates to the standard user dashboard view.
     * <p>
     * The stage is made resizable before redirecting.
     */
    public static void navigateToUserDashboard(){
        root.setResizable(true);
        redirectView(Frames.USER_DASHBOARD_VIEW_PATH, Frames.DASHBOARD_FRAME_WIDTH, Frames.DASHBOARD_FRAME_HEIGHT);
    }

    /**
     * Navigates to the pending user dashboard view.
     * <p>
     * The stage is made resizable before redirecting.
     */
    public static void navigateToPendingUserDashboard(){
        root.setResizable(true);
        redirectView(Frames.PENDING_DASHBOARD_VIEW_PATH, Frames.DASHBOARD_FRAME_WIDTH, Frames.DASHBOARD_FRAME_HEIGHT);
    }

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

            if(ResourceFileController.mediaPlayer != null){
                ResourceFileController.mediaPlayer.dispose();
            }

            mainContent.getChildren().setAll(view);
        } catch (IOException e) {
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, e.getMessage(), Alert.AlertType.ERROR);
            System.err.println(e.getMessage());
        }
    }

    /**
     * Initializes the {@link SessionManager} with the provided user.
     *
     * @param sessionUser the user to associate with the session
     */
    public static void initSessionManager(User sessionUser){
        SessionManager.initialize(sessionUser);
        sessionManager = SessionManager.getInstance();
    }

    /**
     * Retrieves the current session user.
     *
     * @return the {@link User} associated with the current session, or {@code null} if not initialized
     */
    public static User getSessionUser(){
        User sessionUser = null;
        if(sessionManager != null)
            sessionUser = sessionManager.getUser();
        return sessionUser;
    }

    /**
     * Logs out the current session.
     * <p>
     * Resets the {@link SessionManager} and navigates back to the login view.
     */
    public static void logout(){
        SessionManager.reset();
        navigateToLogin();
    }
}
