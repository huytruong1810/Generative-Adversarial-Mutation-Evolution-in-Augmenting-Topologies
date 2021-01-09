package Environment.Controller;

import Environment.*;
import Neat.Genome.ConGene;
import Neat.Genome.Genome;
import Neat.Genome.NodeGene;
import Neat.Neat;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;

import static Environment.WorldDriver.labTheme;
import static Environment.WorldDriver.simTheme;

public class NEATController implements Initializable {

    private Stage mainStage;
    private Genome selected;
    private Simulation S;
    private GraphicsContext gc;

    @FXML private TextField evolveStep;
    @FXML private Label WorldSizeT, TrialsT, TimeStepsT, PopT, EpT;
    @FXML private Label genNo, latestScore;
    @FXML private Canvas GenomeCanvas;

    @FXML public void exit() { mainStage.close(); }

    @FXML public void expand() { mainStage.setFullScreen(true); }

    @FXML public void reset() {
        gc.clearRect(0, 0, GenomeCanvas.getWidth(), GenomeCanvas.getHeight());
        genNo.setText("0");
        latestScore.setText("");
        makeNEATScene(mainStage, S.getEnvironment().getSize(), 0,
                S.getTimeSteps(), Neat.getPopulation().size(), Neat.numEps);
    }

    @FXML public void evolve() {

        int numSteps = Integer.parseInt(evolveStep.getText());
        genNo.setText(Integer.toString(Integer.parseInt(genNo.getText()) + numSteps));

        for (int i = 0; i < numSteps; ++i) Neat.evolve();

        // showcase the best genome after the evolution step(s)
        selected = Neat.getFittest();
        drawGenome(selected);

        // display high score and convergence
        latestScore.setText(Double.toString(selected.getScore()));

    }

    @FXML public void view() {

        // make pop-up window
        Stage viewWindow = new Stage();
        viewWindow.initModality(Modality.APPLICATION_MODAL);
        viewWindow.initOwner(mainStage);
        viewWindow.setTitle("NEAT Population");
        viewWindow.getIcons().add(new Image("images/lab.png"));

        // **START: VIEW SCENE**
        ToolBar toolBar = new ToolBar();
        Button spotlight = new Button("Spotlight");
        toolBar.getItems().add(spotlight);
        ListView<Genome> NEATPop = new ListView<>(FXCollections.observableArrayList(Neat.getPopulation()));
        VBox root = new VBox(toolBar, NEATPop);
        spotlight.setOnAction(e -> {
            Genome cache = selected; // in case selected is null
            selected = NEATPop.getSelectionModel().getSelectedItem();
            if (selected != null) drawGenome(selected);
            else {
                selected = cache; // return to previous selection
                // **START: VIEW SCENE > ERROR SCENE**
                Stage errorWindow = new Stage();
                errorWindow.initModality(Modality.APPLICATION_MODAL);
                errorWindow.initOwner(viewWindow);
                errorWindow.setScene(makeErrorScene("No genome is\nselected", errorWindow));
                errorWindow.show();
                // **END: VIEW SCENE > ERROR SCENE**
            }
        });
        // **END: VIEW SCENE**

        Scene viewScene = new Scene(root, 500, 500);
        viewScene.getStylesheets().add("css/ViewStyle.css");
        viewWindow.setResizable(false);
        viewWindow.setScene(viewScene);

        viewWindow.show(); // pop-up

    }

    @FXML public void simulate() throws IOException {

        // make pop-up window
        Stage simulateWindow = new Stage();
        simulateWindow.initModality(Modality.APPLICATION_MODAL);
        simulateWindow.initOwner(mainStage);

        if (selected != null) {

            labTheme.pause();
            simTheme.play();

            simulateWindow.setTitle("Wumpus World Simulation");
            simulateWindow.getIcons().add(new Image("images/simulation.png"));

            // simulation window initialization
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("view/simulation.fxml"));
            Parent sRoot = loader.load();
            SimulationController sc = loader.getController();
            sc.makeSimulationScene(simulateWindow, S, selected);
            Scene simulationScene = new Scene(sRoot);
            simulationScene.getStylesheets().add("css/SimulationStyle.css");
            simulateWindow.setResizable(true);
            simulateWindow.setScene(simulationScene);

        }
        else simulateWindow.setScene(makeErrorScene("Simulation Requires\nOne Selected Genome", simulateWindow));

        simulateWindow.show(); // pop-up

    }

    @FXML public void export() {

        // make pop-up window
        Stage exportWindow = new Stage();
        exportWindow.initModality(Modality.APPLICATION_MODAL);
        exportWindow.initOwner(mainStage);

        if (selected != null) {

            exportWindow.setTitle("Genome Exportation");
            exportWindow.getIcons().add(new Image("images/lab.png"));

            // **START: EXPORT SCENE**
            TextField fNameField = new TextField("genome");
            Label fExt = new Label(".txt");
            Button done = new Button("Done");
            VBox root = new VBox(new HBox(fNameField, fExt), done);
            done.setOnAction(e -> {
                try { // make file and save genome's information
                    File f = new File(fNameField.getText() + ".txt");
                    if (f.createNewFile()) System.out.println("Success!");
                    else System.out.println("File already exists. Overwriting!");
                    FileWriter myWriter = new FileWriter(f.getName());
                    myWriter.write(dissect(selected));
                    myWriter.close();
                } catch (IOException ex) { ex.printStackTrace(); }
                exportWindow.close();
            });
            Scene exportScene = new Scene(root, 200, 200);
            exportScene.getStylesheets().add("css/CommonPopUpStyle.css");
            // **END: EXPORT SCENE**

            exportWindow.setScene(exportScene);

        }
        else exportWindow.setScene(makeErrorScene("Exportation Requires\nOne Selected Genome", exportWindow));

        exportWindow.show(); // pop-up

    }

    private Scene makeErrorScene(String msg, Stage window) {

        window.setTitle("Error!");
        window.getIcons().add(new Image("images/error.png"));
        window.setResizable(false);

        // **START: ERROR SCENE**
        Label errorMessage = new Label(msg);
        Button closeError = new Button("Acknowledge");
        VBox errorBox = new VBox(errorMessage, closeError);
        closeError.setOnAction(e -> window.close());
        Scene errorScene = new Scene(errorBox, msg.length() * 10, 200);
        errorScene.getStylesheets().add("css/CommonPopUpStyle.css");
        // **END: ERROR SCENE**

        return errorScene;

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) { gc = GenomeCanvas.getGraphicsContext2D(); }

    public void makeNEATScene(Stage s, int worldSize, int numTrials, int timeSteps, int maxPop, int numEps) {

        WorldSizeT.setText("World Size: " + worldSize);
        TrialsT.setText("No. Trials: " + numTrials);
        TimeStepsT.setText("Max Steps: " + timeSteps);
        PopT.setText("Max Pop.: " + maxPop);
        EpT.setText("No. Eps: " + numEps);

        int n = (int) Screen.getPrimary().getBounds().getMaxY();
        GenomeCanvas.setWidth(n);
        GenomeCanvas.setHeight(n);

        S = new Simulation(worldSize, timeSteps);
        mainStage = s;

        // **START: MAKE WORLD BLUEPRINT**
        int pits = 2, golds = 1;
        boolean randomHLoc = true, randomWLoc = true;
        char[][][] bluePrint = new char[worldSize][worldSize][4];
        boolean[][] occupied = new boolean[worldSize][worldSize];
        int x, y;
        Random randGen = new Random();

        for (int i = 0; i < worldSize; ++i) {
            for (int j = 0; j < worldSize; ++j) {
                occupied[i][j] = false;
                for (int k = 0; k < 4; ++k) bluePrint[i][j][k] = ' ';
            }
        }

        int hX = 0;
        int hY = 0;
        char hD = 'N';
        if (randomHLoc) {
            hX = randGen.nextInt(worldSize);
            hY = randGen.nextInt(worldSize);
            switch (randGen.nextInt(4)) {
                case 0: hD = 'N'; break;
                case 1: hD = 'E'; break;
                case 2: hD = 'S'; break;
                case 3: hD = 'W'; break;
            }
        }
        occupied[hX][hY] = true;
        bluePrint[hX][hY][3] = hD;

        int wX = worldSize-1;
        int wY = worldSize-1;
        if (randomWLoc) {
            wX = randGen.nextInt(worldSize);
            wY = randGen.nextInt(worldSize);
            while (wX == hX && wX == hY) {
                wX = randGen.nextInt(worldSize);
                wY = randGen.nextInt(worldSize);
            }
        }
        occupied[wX][wY] = true;
        bluePrint[wX][wY][1] = 'W';

        for (int i = 0; i < pits; ++i) {
            x = randGen.nextInt(worldSize);
            y = randGen.nextInt(worldSize);
            while ((x == hX && y == hY) || occupied[x][y]) {
                x = randGen.nextInt(worldSize);
                y = randGen.nextInt(worldSize);
            }
            occupied[x][y] = true;
            bluePrint[x][y][0] = 'P';
        }

        for (int i = 0; i < golds; ++i) {
            x = randGen.nextInt(worldSize);
            y = randGen.nextInt(worldSize);
            while ((x == hX && y == hY) || occupied[x][y]) {
                x = randGen.nextInt(worldSize);
                y = randGen.nextInt(worldSize);
            }
            occupied[x][y] = true;
            bluePrint[x][y][2] = 'G';
        }
        // **END: MAKE WORLD BLUEPRINT**

        // 5 first output nodes are relevant to actor, 6th is to critic
        Neat.setUp(S, bluePrint, 5, 6, maxPop, numEps);

    }

    private void drawGenome(Genome g) {

        int r = 25; // neuron radius

        // clear main canvas before drawing
        gc.clearRect(0, 0, GenomeCanvas.getWidth(), GenomeCanvas.getHeight());

        // draw nodes
        List<NodeGene> nodes = g.getNodes().getData();
        for (NodeGene n : nodes) {
            gc.setStroke(Color.GHOSTWHITE.brighter());
            gc.setLineWidth(5);
            gc.strokeOval(n.getX(), n.getY(), r, r);
            gc.setLineWidth(1);
            gc.strokeText(n.getFa(true) + "/" + n.getFa(false),
                    n.getX()+r/2.0, n.getY()-r/2.0);
        }

        // draw connections
        List<ConGene> cons = g.getCons().getData();
        for (ConGene c : cons) {
            NodeGene a = c.getFG(), b = c.getTG();
            if (c.isEnabled()) gc.setStroke(Color.FORESTGREEN);
            else gc.setStroke(Color.CRIMSON);
            gc.setLineWidth(1);
            gc.strokeLine(a.getX()+r/2.0, a.getY()+r/2.0, b.getX()+r/2.0, b.getY()+r/2.0);
            gc.strokeText(Math.round(c.getWeight(true) * 100.0) / 100.0 + "/" +
                            Math.round(c.getWeight(false) * 100.0) / 100.0,
                    (a.getX()+b.getX())/2, (a.getY()+b.getY())/2);
        }

        gc.setStroke(Color.LIGHTSKYBLUE);
        gc.strokeText(String.valueOf(g), 20, 20);

    }

    private String dissect(Genome g) {
        StringBuilder result = new StringBuilder(g.getScore() + "\n");
        List<NodeGene> nodes = g.getNodes().getData();
        List<ConGene> cons = g.getCons().getData();
        for (NodeGene n : nodes) result.append(n.getIN()).append("x").append(n.getX()).append("\n");
        for (ConGene c : cons) result.append(String.format("%se%sf%st%sa%sc%s\n", c.getIN(), c.isEnabled() ? "1" : "0",
                c.getFG().getIN(), c.getTG().getIN(), c.getWeight(true), c.getWeight(false)));
        return result.toString();
    }

}
