package app.tracktune.controller.authenticatedUser;

import app.tracktune.controller.Controller;
import app.tracktune.model.resource.Resource;
import app.tracktune.utils.Frames;
import app.tracktune.utils.Strings;
import app.tracktune.view.ViewManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;

public class ResourceFileController extends Controller implements Initializable {
    private Resource resource;

    public ResourceFileController(Resource resource) {this.resource = resource;}

    @Override
    public void initialize(java.net.URL location, java.util.ResourceBundle resources) {
    }

    @FXML
    private void handleReturn(){
        try{
            if(parentController instanceof AuthenticatedUserDashboardController authController){
                ViewManager.setMainContent(Frames.RESOURCES_VIEW_PATH, authController.mainContent, parentController);
            }
        }catch(Exception e){
            ViewManager.setAndShowAlert(Strings.ERROR, Strings.ERROR, Strings.ERR_GENERAL, Alert.AlertType.ERROR);
            System.err.println(e.getMessage());
        }
    }
}
