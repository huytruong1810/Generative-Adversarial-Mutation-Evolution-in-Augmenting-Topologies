package Environment.Controllers;

import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class LoginController extends Controller {

    private double winX, winY;
    private TextField selectedField;
    private EventHandler<KeyEvent> loginKeyEvent;

    @FXML private HBox RootBox;
    @FXML private TextField WorldSizeT, TimeStepsT, PopT, TrEpsT, TeEpsT;
    @FXML private Button reviewBtn, runBtn, exitBtn;
    @FXML private RadioButton logEnableBtn;

    @FXML public void runLab() throws IOException, CloneNotSupportedException {

        primaryStage.removeEventFilter(KeyEvent.KEY_PRESSED, loginKeyEvent); // remove login key handler for this scene

        if (logEnableBtn.isSelected()) System.out.println("This run will be logged onto the database server.");
        else Utils.disableLogging();

        // NEAT culture lab initialization
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/NEATLab.fxml"));
        Parent sRoot = loader.load();
        NEATController nc = loader.getController();
        nc.makeNEATScene(primaryStage,
                Integer.parseInt(WorldSizeT.getText()),
                Integer.parseInt(TimeStepsT.getText()),
                Integer.parseInt(PopT.getText()),
                Integer.parseInt(TrEpsT.getText()),
                Integer.parseInt(TeEpsT.getText())
        );
        Scene NEATScene = new Scene(sRoot);
        NEATScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/NEATLabStyle.css")).toExternalForm());
        primaryStage.setResizable(true);
        primaryStage.setScene(NEATScene);

    }

    @FXML void reviewRuns() throws SQLException {

        // make pop-up window
        Stage reviewWindow = new Stage();
        reviewWindow.initModality(Modality.NONE);
        reviewWindow.setTitle("Previous Accesses");
        reviewWindow.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/images/database.png")).toExternalForm()));

        // **START: REVIEW SCENE**
        List<String> accesses = new ArrayList<>();
        if (Utils.dbOnline) {
            try (Connection dbConn = DriverManager.getConnection(Utils.databaseURL)) {
                try (Statement query = dbConn.createStatement()) {
                    query.execute("USE Population;");
                    try (ResultSet rs = query.executeQuery("SELECT * FROM Accesses;")) {
                        while (rs.next()) {
                            accesses.add(String.format("RUN ID %s\n - AT %s %s\n - HAVING MAX_GEN %s, SCORE %s\n - WITH " +
                                            "WORLD_SIZE %s, NO_TIME_STEPS %s, MAX_POPULATION %s, NO_TR_EPS %s, NO_TE_EPS %s",
                                    rs.getString("Access_ID"), rs.getString("Access_Time"), rs.getString("Access_Date"),
                                    rs.getString("Max_Generation"), rs.getString("Latest_Score"),
                                    rs.getString("World_Size"), rs.getString("No_Time_Steps"), rs.getString("Max_Population"),
                                    rs.getString("No_TrEps"), rs.getString("No_TeEps")));
                        }
                    }
                }
            }
        }
        ListView<String> accessList = new ListView<>(FXCollections.observableArrayList(accesses));
        // **END: VIEW SCENE**

        Scene reviewScene = new Scene(accessList, 850, 500);
        reviewScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/ViewStyle.css")).toExternalForm());
        reviewWindow.setResizable(false);
        reviewWindow.setScene(reviewScene);

        reviewWindow.show(); // pop-up

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        if (!Utils.dbOnline) {
            reviewBtn.setDisable(true);
            logEnableBtn.setDisable(true);
        }

        reviewBtn.setGraphic(new ImageView(Objects.requireNonNull(getClass().getResource("/images/download.png")).toExternalForm()));
        runBtn.setGraphic(new ImageView(Objects.requireNonNull(getClass().getResource("/images/enter.png")).toExternalForm()));
        exitBtn.setGraphic(new ImageView(Objects.requireNonNull(getClass().getResource("/images/exit.png")).toExternalForm()));

    }

    public void makeLoginScene(Stage s) {

        primaryStage = s;
        RootBox.setOnMousePressed(e -> {
            winX = e.getSceneX();
            winY = e.getSceneY();
        });
        RootBox.setOnMouseDragged(e -> {
            primaryStage.setX(e.getScreenX() - winX);
            primaryStage.setY(e.getScreenY() - winY);
        });

        // set up keyboard shortcuts for this scene
        loginKeyEvent = ke -> {
            TextField toField = null;
            KeyCode keyCode = ke.getCode();
            try {
                if (keyCode == KeyCode.ENTER) runLab();
                else if (keyCode == KeyCode.ESCAPE) exit();
                else if (keyCode == KeyCode.MINUS) minimize();
                else if (keyCode == KeyCode.W || keyCode == KeyCode.UP) {
                    if (selectedField == TimeStepsT) toField = WorldSizeT;
                    else if (selectedField == PopT) toField = TimeStepsT;
                    else if (selectedField == TeEpsT) toField = TrEpsT;
                }
                else if (keyCode == KeyCode.S || keyCode == KeyCode.DOWN) {
                    if (selectedField == WorldSizeT) toField = TimeStepsT;
                    else if (selectedField == TimeStepsT) toField = PopT;
                    else if (selectedField == TrEpsT) toField = TeEpsT;
                }
                else if (keyCode == KeyCode.A || keyCode == KeyCode.LEFT) {
                    if (selectedField == TrEpsT) toField = WorldSizeT;
                    else if (selectedField == TeEpsT) toField = TimeStepsT;
                }
                else if (keyCode == KeyCode.D || keyCode == KeyCode.RIGHT) {
                    if (selectedField == WorldSizeT) toField = TrEpsT;
                    else if (selectedField == TimeStepsT || selectedField == PopT) toField = TeEpsT;
                }
                else if (keyCode.isDigitKey()) selectedField.setText(selectedField.getText() + ke.getText());
                else if (keyCode == KeyCode.BACK_SPACE) {
                    String text = selectedField.getText();
                    selectedField.setText((text.length() != 0) ? text.substring(0, text.length() - 1) : "");
                }
            } catch (IOException | CloneNotSupportedException e) {
                e.printStackTrace();
            }
            if (toField != null) changeSelectedTextField(toField);
            ke.consume();
        };
        primaryStage.addEventFilter(KeyEvent.KEY_PRESSED, loginKeyEvent);

        changeSelectedTextField(WorldSizeT); // initial field

        WorldSizeT.setOnMouseClicked(e -> changeSelectedTextField(WorldSizeT));
        TimeStepsT.setOnMouseClicked(e -> changeSelectedTextField(TimeStepsT));
        PopT.setOnMouseClicked(e -> changeSelectedTextField(PopT));
        TrEpsT.setOnMouseClicked(e -> changeSelectedTextField(TrEpsT));
        TeEpsT.setOnMouseClicked(e -> changeSelectedTextField(TeEpsT));

    }

    private void changeSelectedTextField(TextField toRef) {
        if (selectedField != null) selectedField.setStyle("-fx-background-color: rgb(99, 99, 102);-fx-text-fill: rgb(199, 199, 204);");
        selectedField = toRef;
        selectedField.setEditable(false);
        selectedField.setStyle("-fx-background-color: rgb(242, 242, 247);-fx-text-fill: rgb(72, 72, 74);");
    }

}
