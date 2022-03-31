package RL.Controllers;

import RL.Simulators.EnsembleRoom;
import RL.Simulators.TestRoom;
import NEAT.DataStructures.Grapher;
import NEAT.Individual;
import NEAT.Species;
import NEAT.NEAT;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.*;

import static NEAT.NEAT.*;

public class NEATController extends Controller {

    // database components
    private int accessID;
    private File tempFile;

    // graphical components
    private Individual selected;
    private double winX, winY;

    // plotting components
    private Grapher grapher;

    @FXML private HBox RootBox;
    @FXML private TextField evolveStep;
    @FXML private Button exitBtn, resetBtn, resizeBtn, uploadBtn, buildEnvBtn, evolveBtn, viewBtn, simulateBtn, baggingBtn, treeBtn, grapherBtn, importBtn, exportBtn;
    @FXML private Label WorldSizeT, TimeStepsT, PopT, TrEpsT, TeEpsT;
    @FXML private Label genNo, latestScore;
    @FXML private Pane SpotlightWrapper, Spotlight;

    @FXML public void upload() throws IOException { Utils.fileToDB(tempFile); }

    @Override
    @FXML public void exit() {
        Utils.DOT(tempFile, accessID, genNo.getText(), latestScore.getText());
        primaryStage.close();
    }

    @FXML public void reset() throws CloneNotSupportedException {

        selected = null;
        Spotlight.getChildren().clear();
        genNo.setText("0");
        latestScore.setText("");
        NEAT.getAncestorTree().reset();

        // before reset pplt
        Utils.DOT(tempFile, accessID, genNo.getText(), latestScore.getText());

        // reset pplt, count it as a new access
        makeNEATScene(primaryStage,
                Integer.parseInt(WorldSizeT.getText()), Integer.parseInt(TimeStepsT.getText()),
                Integer.parseInt(PopT.getText()), Integer.parseInt(TrEpsT.getText()), Integer.parseInt(TeEpsT.getText())
        );

    }

    @FXML public void buildEnv() throws IOException {

        Stage buildEnvWindow = new Stage();

        buildEnvWindow.initOwner(primaryStage);
        buildEnvWindow.initStyle(StageStyle.TRANSPARENT);
        buildEnvWindow.setTitle("Build Custom Environment");
        buildEnvWindow.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/images/buildEnv.png")).toExternalForm()));
        // simulation window initialization
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/buildEnv.fxml"));
        Parent sRoot = loader.load();
        BuildEnvController bec = loader.getController();
        bec.makeBuildEnvScene(buildEnvWindow, worldDim, bluePrint);
        Scene buildEnvScene = new Scene(sRoot);
        buildEnvScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/BuildEnv.css")).toExternalForm());
        buildEnvWindow.setResizable(true);
        buildEnvWindow.setScene(buildEnvScene);

        buildEnvWindow.showAndWait();

        // handle requested world size and blue print from previous scene
        worldDim = bec.getWorldSize();
        bluePrint = bec.getCustomBlueprint();

    }

    @FXML public void evolve() throws CloneNotSupportedException {

        int numSteps = Integer.parseInt(evolveStep.getText());
        int curStep = Integer.parseInt(genNo.getText());

//        // file access is slow so save them in local file temporarily
//        List<List<Individual>> stepList = new ArrayList<>();
//        List<List<Species>> speciesProgress = new ArrayList<>();
        for (int i = 0; i < numSteps; ++i) {
            NEAT.evolve();
            grapher.addPoint(
                    NEAT.listEcosystem().size(),
                    Utils.championOfChampions(NEAT.listEcosystem()).getScore(),
                    NEAT.getAvgScore()
            );
//            stepList.add(NEAT.listPopulation());
//            speciesProgress.add(NEAT.listEcosystem());
        }
//        try {
//            Utils.fileParse(tempFile, accessID, curStep, stepList, speciesProgress);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        genNo.setText(Integer.toString(curStep + numSteps));

        // showcase the best individual after the evolution
        selected = Utils.championOfChampions(NEAT.listEcosystem());
        Utils.drawIndividual(primaryStage, Spotlight, selected);

        // display high score
        latestScore.setText(Double.toString(selected.getScore()));

    }

    @FXML public void view() {

        Stage viewWindow = new Stage();

        viewWindow.initOwner(primaryStage);
        viewWindow.setTitle("NEAT Population");
        viewWindow.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/images/view.png")).toExternalForm()));

        // **START: VIEW SCENE**
        ToolBar toolBar = new ToolBar();
        Button spotlight = new Button("Spotlight");
        toolBar.getItems().add(spotlight);
        ListView<Individual> NEATPplt = new ListView<>(FXCollections.observableArrayList(NEAT.listPopulation()));
        VBox root = new VBox(toolBar, NEATPplt);
        spotlight.setOnAction(e -> {
            Individual cache = selected; // in case selected is null
            selected = NEATPplt.getSelectionModel().getSelectedItem();
            if (selected != null) Utils.drawIndividual(primaryStage, Spotlight, selected);
            else {
                selected = cache; // return to previous selection
                // **START: VIEW SCENE > ERROR SCENE**
                Stage errorWindow = new Stage();
                errorWindow.initModality(Modality.APPLICATION_MODAL);
                errorWindow.initOwner(viewWindow);
                errorWindow.setScene(Utils.makeWarningScene("No individual is\nselected", errorWindow));
                errorWindow.show();
                // **END: VIEW SCENE > ERROR SCENE**
            }
        });
        // **END: VIEW SCENE**

        Scene viewScene = new Scene(root, 500, 500);
        viewScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/ViewStyle.css")).toExternalForm());
        viewWindow.setResizable(false);
        viewWindow.setScene(viewScene);

        viewWindow.show(); // pop-up

    }

    @FXML public void simulate() throws IOException, CloneNotSupportedException {

        if (selected == null) {
            Stage noIndErrorWindow = new Stage();
            noIndErrorWindow.initOwner(primaryStage);
            noIndErrorWindow.setScene(Utils.makeWarningScene("Simulation Requires\nOne Selected Individual", noIndErrorWindow));
            noIndErrorWindow.show();
            return;
        }

        // clone the individual as testing phase should not update the individual's parameters,
        // this also supports threading of this phase
        Individual theClone = selected.clone();
        theClone.express(); // must express genome to get phenotype for testing

        // load up build environment scene first
        Stage buildEnvWindow = new Stage();

        buildEnvWindow.initOwner(primaryStage);
        buildEnvWindow.initStyle(StageStyle.TRANSPARENT);
        buildEnvWindow.setTitle("Build Custom Environment");
        buildEnvWindow.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/images/simulation.png")).toExternalForm()));
        // simulation window initialization
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/buildEnv.fxml"));
        Parent sRoot = loader.load();
        BuildEnvController bec = loader.getController();
        bec.makeBuildEnvScene(buildEnvWindow, worldDim, bluePrint);
        Scene buildEnvScene = new Scene(sRoot);
        buildEnvScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/BuildEnv.css")).toExternalForm());
        buildEnvWindow.setResizable(true);
        buildEnvWindow.setScene(buildEnvScene);

        buildEnvWindow.showAndWait(); // wait for it to be closed before proceeding

        // load up individual simulation
        Stage simulateWindow = new Stage();

        simulateWindow.initOwner(primaryStage);
        simulateWindow.setTitle("RL.Wumpus World Simulation");
        simulateWindow.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/images/simulation.png")).toExternalForm()));
        // simulation window initialization
        loader = new FXMLLoader(getClass().getResource("/view/simulation.fxml"));
        sRoot = loader.load();
        SimulationController sc = loader.getController();
        sc.makeSimulationScene(simulateWindow, new TestRoom(bec.getWorldSize(), timeHorizon), bec.getCustomBlueprint(), theClone);
        Scene simulationScene = new Scene(sRoot);
        simulationScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/SimulationStyle.css")).toExternalForm());
        simulateWindow.setResizable(true);
        simulateWindow.setScene(simulationScene);

        simulateWindow.show();

    }

    @FXML public void bagging() throws IOException, CloneNotSupportedException {

        if (NEAT.listEcosystem().size() <= 0) {
            Stage noEcoErrorWindow = new Stage();
            noEcoErrorWindow.initOwner(primaryStage);
            noEcoErrorWindow.setScene(Utils.makeWarningScene("Ecosystem has not\nbeen started.", noEcoErrorWindow));
            noEcoErrorWindow.show();
            return;
        }

        // collect and clone all the champions
        ArrayList<Individual> champions = new ArrayList<>();
        for (Species s : NEAT.listEcosystem()) {
            Individual clonedChampion = s.getChampion().clone();
            clonedChampion.express(); // must express genome to get phenotype
            champions.add(clonedChampion);
        }

        // load up build environment scene first
        Stage buildEnvWindow = new Stage();

        buildEnvWindow.initOwner(primaryStage);
        buildEnvWindow.initStyle(StageStyle.TRANSPARENT);
        buildEnvWindow.setTitle("Build Custom Environment");
        buildEnvWindow.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/images/bagging.png")).toExternalForm()));
        // simulation window initialization
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/buildEnv.fxml"));
        Parent sRoot = loader.load();
        BuildEnvController bec = loader.getController();
        bec.makeBuildEnvScene(buildEnvWindow, worldDim, bluePrint);
        Scene buildEnvScene = new Scene(sRoot);
        buildEnvScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/BuildEnv.css")).toExternalForm());
        buildEnvWindow.setResizable(true);
        buildEnvWindow.setScene(buildEnvScene);

        buildEnvWindow.showAndWait(); // wait for it to be closed before proceeding

        // load up bagging simulation
        Stage baggingWindow = new Stage();

        baggingWindow.initOwner(primaryStage);
        baggingWindow.setTitle("RL.Wumpus World Bagging Simulation");
        baggingWindow.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/images/bagging.png")).toExternalForm()));
        // simulation window initialization
        loader = new FXMLLoader(getClass().getResource("/view/bagging.fxml"));
        sRoot = loader.load();
        BaggingController bc = loader.getController();
        bc.makeBaggingScene(baggingWindow, new EnsembleRoom(bec.getWorldSize(), timeHorizon), bec.getCustomBlueprint(), champions);
        Scene baggingScene = new Scene(sRoot);
        baggingScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/BaggingStyle.css")).toExternalForm());
        baggingWindow.setResizable(true);
        baggingWindow.setScene(baggingScene);

        baggingWindow.show();

    }

    @FXML public void tree() {

        Stage treeWindow = new Stage();

        treeWindow.initOwner(primaryStage);
        treeWindow.setTitle("Species Tree");
        treeWindow.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/images/tree.png")).toExternalForm()));

        // **START: TREE SCENE**
        ToolBar toolBar = new ToolBar();
        Button ancestry = new Button("Visualize");
        toolBar.getItems().add(ancestry);
        ListView<Species> eco = new ListView<>(FXCollections.observableArrayList(NEAT.listEcosystem()));
        VBox root = new VBox(toolBar, eco);
        ancestry.setOnAction(e -> {
            // **START: TREE SCENE > ANCESTRY SCENE**
            Stage ancestryWindow = new Stage();
            ancestryWindow.initModality(Modality.APPLICATION_MODAL);
            ancestryWindow.initOwner(treeWindow);
            ancestryWindow.setTitle("Species Tree");
            ancestryWindow.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/images/tree.png")).toExternalForm()));
            Pane pane = new Pane();
            Utils.drawAncTree(NEAT.getAncestorTree().getRoot(), pane, 25, 0, 0);
            ancestryWindow.setScene(new Scene(new ScrollPane(pane), 500, 500));
            ancestryWindow.show();
            // **END: TREE SCENE > ANCESTRY SCENE**
        });
        // **END: TREE SCENE**

        Scene treeScene = new Scene(root, 500, 500);
        treeScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/ViewStyle.css")).toExternalForm()); // use same layout as view scene
        treeWindow.setResizable(false);
        treeWindow.setScene(treeScene);

        treeWindow.show(); // pop-up

    }

    @FXML public void grapher() throws IOException, SQLException {

        // make sure database is most updated
        upload();

        // make pop-up window
        Stage grapherWindow = new Stage();

        grapherWindow.initOwner(primaryStage);
        grapherWindow.setTitle("Grapher");
        grapherWindow.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/images/grapher.png")).toExternalForm()));

        // grapher window initialization
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/grapher.fxml"));
        Parent sRoot = loader.load();
        GrapherController sc = loader.getController();
        sc.makeGrapherScene(grapherWindow, grapher);
        Scene grapherScene = new Scene(sRoot);
        grapherScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/CommonPopUpStyle.css")).toExternalForm());
        grapherWindow.setResizable(true);
        grapherWindow.setScene(grapherScene);

        grapherWindow.show();

    }

    @FXML public void importGene() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("NEAT Gene Files (*.neatg)", "*.neatg"));
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            try (Scanner scanner = new Scanner(file)) {
                while (scanner.hasNext()) System.out.println(scanner.next());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

    }

    @FXML public void export() {

        Stage exportWindow = new Stage();

        if (selected != null) {
            exportWindow.setTitle("Export Individual");
            exportWindow.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/images/export.png")).toExternalForm()));
            TextArea geneticInfo = new TextArea(Utils.parseGene(selected));
            Button closeBtn = new Button("Save As");
            closeBtn.setOnAction(e -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("NEAT Gene Files (*.neatg)", "*.neatg"));
                File file = fileChooser.showSaveDialog(exportWindow);
                if (file != null) {
                    try {
                        FileWriter myWriter = new FileWriter(file, true);
                        myWriter.write(geneticInfo.getText());
                        myWriter.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                exportWindow.close();
            });
            Scene exportScene = new Scene(new VBox(geneticInfo, closeBtn));
            exportScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/CommonPopUpStyle.css")).toExternalForm());
            exportWindow.setResizable(true);
            exportWindow.setScene(exportScene);
        }
        else exportWindow.setScene(Utils.makeWarningScene("Exportation Requires\nOne Selected Individual", exportWindow));

        exportWindow.show();

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        exitBtn.setGraphic(new ImageView(Objects.requireNonNull(getClass().getResource("/images/shutdown.png")).toExternalForm()));
        resetBtn.setGraphic(new ImageView(Objects.requireNonNull(getClass().getResource("/images/reset.png")).toExternalForm()));
        resizeBtn.setGraphic(new ImageView(Objects.requireNonNull(getClass().getResource("/images/resize.png")).toExternalForm()));
        uploadBtn.setGraphic(new ImageView(Objects.requireNonNull(getClass().getResource("/images/upload.png")).toExternalForm()));
        buildEnvBtn.setGraphic(new ImageView(Objects.requireNonNull(getClass().getResource("/images/buildEnv.png")).toExternalForm()));
        evolveBtn.setGraphic(new ImageView(Objects.requireNonNull(getClass().getResource("/images/evolve.png")).toExternalForm()));
        viewBtn.setGraphic(new ImageView(Objects.requireNonNull(getClass().getResource("/images/view.png")).toExternalForm()));
        simulateBtn.setGraphic(new ImageView(Objects.requireNonNull(getClass().getResource("/images/simulation.png")).toExternalForm()));
        baggingBtn.setGraphic(new ImageView(Objects.requireNonNull(getClass().getResource("/images/bagging.png")).toExternalForm()));
        treeBtn.setGraphic(new ImageView(Objects.requireNonNull(getClass().getResource("/images/tree.png")).toExternalForm()));
        grapherBtn.setGraphic(new ImageView(Objects.requireNonNull(getClass().getResource("/images/grapher.png")).toExternalForm()));
        importBtn.setGraphic(new ImageView(Objects.requireNonNull(getClass().getResource("/images/import.png")).toExternalForm()));
        exportBtn.setGraphic(new ImageView(Objects.requireNonNull(getClass().getResource("/images/export.png")).toExternalForm()));

        if (!Utils.dbOnline) {
            uploadBtn.setDisable(true);
            return; // do not make temp file
        }

        tempFile = new File("temp.txt");
        try {
            if (tempFile.createNewFile()) System.out.println("Temp file successfully init!");
            else System.out.println("This should not happen.");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void makeNEATScene(Stage s, int worldSize, int timeSteps, int maxPop, int trainEps, int testEps) {

        grapher = new Grapher();
        accessID = Utils.insertAccess(worldSize, timeSteps, maxPop, trainEps, testEps);

        evolveStep.setEditable(false);
        WorldSizeT.setText("" + worldSize);
        TimeStepsT.setText("" + timeSteps);
        PopT.setText("" + maxPop);
        TrEpsT.setText("" + trainEps);
        TeEpsT.setText("" + testEps);

        int W = (int) Screen.getPrimary().getBounds().getMaxX();
        int H = (int) Screen.getPrimary().getBounds().getMaxY();
        SpotlightWrapper.toBack();
        SpotlightWrapper.setPrefSize(W, H);
        Spotlight.setPrefSize(W * 2, H * 2);

        // make window draggable
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
        primaryStage.addEventFilter(KeyEvent.KEY_PRESSED, ke -> {
            KeyCode keyCode = ke.getCode();
            double x = Spotlight.getLayoutX(), y = Spotlight.getLayoutY();
            try {
                if (keyCode == KeyCode.W || keyCode == KeyCode.UP) {
                    Spotlight.setTranslateY(-15);
                    Spotlight.setLayoutY(y - 15);
                } else if (keyCode == KeyCode.S || keyCode == KeyCode.DOWN) {
                    Spotlight.setTranslateY(15);
                    Spotlight.setLayoutY(y + 15);
                } else if (keyCode == KeyCode.A || keyCode == KeyCode.LEFT) {
                    Spotlight.setTranslateX(-15);
                    Spotlight.setLayoutX(x - 15);
                } else if (keyCode == KeyCode.D || keyCode == KeyCode.RIGHT) {
                    Spotlight.setTranslateX(15);
                    Spotlight.setLayoutX(x + 15);
                } else if (keyCode.isDigitKey()) evolveStep.setText(evolveStep.getText() + ke.getText());
                else if (keyCode == KeyCode.BACK_SPACE) {
                    String text = evolveStep.getText();
                    evolveStep.setText((text.length() != 0) ? text.substring(0, text.length() - 1) : "");
                } else if (keyCode == KeyCode.R) reset();
                else if (keyCode == KeyCode.U) upload();
                else if (keyCode == KeyCode.V) view();
                else if (keyCode == KeyCode.I) simulate();
                else if (keyCode == KeyCode.B) bagging();
                else if (keyCode == KeyCode.T) tree();
                else if (keyCode == KeyCode.G) grapher();
                else if (keyCode == KeyCode.M) importGene();
                else if (keyCode == KeyCode.E) export();
                else if (keyCode == KeyCode.TAB) buildEnv();
                else if (keyCode == KeyCode.ENTER) evolve();
                else if (keyCode == KeyCode.ESCAPE) exit();
                else if (keyCode == KeyCode.SHIFT) resize();
                else if (keyCode == KeyCode.MINUS) minimize();
            } catch (IOException | SQLException | CloneNotSupportedException e) {
                e.printStackTrace();
            }
            ke.consume();
        });

        // a null blueprint value means it is to be randomly set up
        // 7 input - 5 first nodes are observations, 2 last nodes are inflection nodes
        // 8 hidden - nodes connecting MRU and ACU
        // 12 output - 6 first nodes are relevant to actor, 7th is for critic, 5 last nodes are relevant to seer
        NEAT.setUp(worldSize, timeSteps, null,
                new int[] {0, 1, 2, 3, 4}, new int[] {5, 6}, 8,
                new int[] {0, 1, 2, 3, 4, 5}, new int[] {6}, new int[] {7, 8, 9, 10, 11}, maxPop, trainEps, testEps);

        // add the starting population to file
        try {
            Utils.fileParse(tempFile, accessID, -1,
                    new ArrayList<>(List.of(NEAT.listPopulation())),
                    new ArrayList<>(List.of(NEAT.listEcosystem())));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // set protocol when user exits application
        primaryStage.setOnCloseRequest(e -> Utils.DOT(tempFile, accessID, genNo.getText(), latestScore.getText()));

    }

}
