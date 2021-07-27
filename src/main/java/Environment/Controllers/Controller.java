package Environment.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public abstract class Controller implements Initializable {

    protected Stage primaryStage;

    @FXML public void exit() { primaryStage.close(); }
    @FXML public void resize() { primaryStage.setFullScreen(!primaryStage.isFullScreen()); }
    @FXML public void minimize() { primaryStage.setIconified(true); }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) { }

}
