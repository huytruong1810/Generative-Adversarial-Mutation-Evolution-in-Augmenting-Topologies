package Environment.Controller;

import Environment.*;
import Neat.Genome.Genome;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

import static Environment.WorldDriver.labTheme;
import static Environment.WorldDriver.simTheme;
import static Neat.Neat.bluePrint;

public class SimulationController implements Initializable {

    private int t; // tile size
    private Genome humanBrain;
    private Stage window;
    private Environment E;
    private Simulation S;
    private Sprite human, wumpus;
    private GraphicsContext gc;

    @FXML private Button next;
    @FXML private Label scentIntText, scentDirText;
    @FXML private Canvas WorldCanvas;
    @FXML private Group RootGroup;
    @FXML private Circle hBump, glitter, breeze, stench, scream;
    @FXML private Circle wBump, scentInt, scentDir;
    @FXML private ListView<String> hProgress = new ListView<>();
    @FXML private ListView<String> wProgress = new ListView<>();

    @FXML public void exit() {
        simTheme.pause();
        labTheme.play();
        window.close();
    }

    @FXML public void restart() {
        next.setDisable(false);
        next.setStyle("-fx-text-fill: linear-gradient(from 0% 0% to 100% 200%, " +
                "repeat, rgb(48, 209, 88) 0%, rgb(100, 210, 255) 50%);");
        next.setText("Next Step");
        human.remove(RootGroup);
        wumpus.remove(RootGroup);
        hProgress.getItems().add("Environment restarted");
        wProgress.getItems().add("Environment restarted");
        makeSimulationScene(window, S, humanBrain);
    }

    @FXML public void runNextStep() {

        int done = S.step();

        hProgress.getItems().add("Step " + S.getStepCounter() + String.format("\nScore: %d Taken Action: %s", S.getHScore(), S.getHAction()));
        wProgress.getItems().add("Step " + S.getStepCounter() + String.format("\nScore: %d Taken Action: %s", S.getWScore(), S.getWAction()));

        hBump.setFill(Color.GHOSTWHITE);
        glitter.setFill(Color.GHOSTWHITE);
        breeze.setFill(Color.GHOSTWHITE);
        stench.setFill(Color.GHOSTWHITE);
        scream.setFill(Color.GHOSTWHITE);
        wBump.setFill(Color.GHOSTWHITE);
        scentInt.setFill(Color.GHOSTWHITE);
        scentDir.setFill(Color.GHOSTWHITE);

        if (E.thereIsHWall()) hBump.setFill(Color.FIREBRICK);
        if (E.thereIsGlitter()) glitter.setFill(Color.GOLD);
        if (E.thereIsBreeze()) breeze.setFill(Color.LIGHTSKYBLUE);
        if (E.thereIsStench()) stench.setFill(Color.DARKSEAGREEN);
        if (E.thereIsScream()) scream.setFill(Color.GHOSTWHITE);

        if (E.thereIsWWall()) wBump.setFill(Color.YELLOWGREEN);
        if (E.thereIsScent()) {
            scentIntText.setText(""+E.getSceneInt());
            scentDirText.setText(""+E.getSceneDir());
            scentInt.setFill(Color.MISTYROSE);
            scentDir.setFill(Color.BURLYWOOD);
        }

        switch (S.getHAction()) {
            case "MOVE_FORWARD":
                if (E.thereIsHWall()) break;
                human.walkForward(t);
                human.setCor(E.getHumanLoc()[1], E.getHumanLoc()[0], t, t/3);
                break;
            case "TURN_RIGHT":
            case "TURN_LEFT":
                human.remove(RootGroup);
                human.setMainMode(E.getHumanDir());
                human.render(RootGroup);
        }

        wumpus.remove(RootGroup);
        switch (S.getWAction()) {
            case "MOVE_RIGHT":
                if (E.thereIsWWall()) break;
                wumpus.setMainMode('E');
                wumpus.walkForward(t);
                wumpus.setCor(E.getWumpusLoc()[1], E.getWumpusLoc()[0], t, t/10);
                break;
            case "MOVE_LEFT":
                if (E.thereIsWWall()) break;
                wumpus.setMainMode('W');
                wumpus.walkForward(t);
                wumpus.setCor(E.getWumpusLoc()[1], E.getWumpusLoc()[0], t, t/10);
                break;
            case "MOVE_UP":
                if (E.thereIsWWall()) break;
                wumpus.setMainMode('N');
                wumpus.walkForward(t);
                wumpus.setCor(E.getWumpusLoc()[1], E.getWumpusLoc()[0], t, t/10);
                break;
            case "MOVE_DOWN":
                if (E.thereIsWWall()) break;
                wumpus.setMainMode('S');
                wumpus.walkForward(t);
                wumpus.setCor(E.getWumpusLoc()[1], E.getWumpusLoc()[0], t, t/10);
        }
        wumpus.render(RootGroup);

        hProgress.getItems().add("HA thinks " + S.getHActorThought() +
                               "\nHC thinks " + S.getHCriticThought() +
                               " while TD target " + S.getHTDTarget());

        // CHECK DONE
        if (done != 0) {
            next.setDisable(true);
            next.setStyle("-fx-text-fill: rgb(0, 0, 0)");
            switch (done) {
                case -2: next.setText("Time!"); break;
                case -1: next.setText("Dead!"); break;
                case 1: next.setText("Human!"); break;
                case 2: next.setText("Wumpus!"); break;
                default: throw new IllegalStateException("Simulation-done fails.");
            }
        }

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) { gc = WorldCanvas.getGraphicsContext2D(); }

    public void makeSimulationScene(Stage s, Simulation simulator, Genome humanGenome) {

        simulator.reset(bluePrint, humanGenome, null);

        humanBrain = humanGenome;
        window = s;
        S = simulator;
        E = S.getEnvironment();
        int worldSize = E.getSize();

        hProgress.getItems().add("Simulating\n" + humanGenome +
                "\nStep " + S.getStepCounter() + String.format("\nScore: %d Taken Action: %s", S.getHScore(), S.getHAction()));
        wProgress.getItems().add("Simulating\n" + null +
                "\nStep " + S.getStepCounter() + String.format("\nScore: %d Taken Action: %s", S.getWScore(), S.getWAction()));

        human = new Sprite(1.4, "images/up0.png", "images/up1.png", "images/up2.png",
                "images/right0.png", "images/right1.png", "images/right2.png",
                "images/down0.png", "images/down1.png", "images/down2.png",
                "images/left0.png", "images/left1.png", "images/left2.png");

        wumpus = new Sprite(3, "images/wup0.png", "images/wup1.png", "images/wup2.png",
                "images/wright0.png", "images/wright1.png", "images/wright2.png",
                "images/wdown0.png", "images/wdown1.png", "images/wdown2.png",
                "images/wleft0.png", "images/wleft1.png", "images/wleft2.png");

        int n = (int)Screen.getPrimary().getBounds().getMaxY();
        WorldCanvas.setWidth(n);
        WorldCanvas.setHeight(n);
        t = n/worldSize;

        human.setMainMode(E.getHumanDir());
        human.setCor(E.getHumanLoc()[1], E.getHumanLoc()[0], t, t/3);
        human.render(RootGroup);

        wumpus.setMainMode('S');
        wumpus.setCor(E.getWumpusLoc()[1], E.getWumpusLoc()[0], t, t/10);
        wumpus.render(RootGroup);

        // draw soil
        for (int i = 0; i < worldSize; ++i)
            for (int j = 0; j < worldSize; ++j)
                gc.drawImage(new Image("images/soil" + (int)(Math.random() * 6)+".png"), j*t, i*t, t, t);
        // draw natural elements
        double t1 = t/(Math.random()*50+50), t2 = t/(Math.random()*0.5+1.5);
        for (int i = 0; i < worldSize; ++i) {
            double it = i*t;
            for (int j = 0; j < worldSize; ++j) {
                if (occupiedTile(i, j)) continue;
                double jt = j*t;
                if (Math.random() < 0.9) gc.drawImage(new Image("images/tree"+(int)(Math.random()*11)+".png"), jt+t1, it+t1);
                else gc.drawImage(new Image("images/flower"+(int)(Math.random()*12)+".png"), jt+t1, it+t1);
                if (Math.random() < 0.3) gc.drawImage(new Image("images/rock"+(int)(Math.random()*8)+".png"), jt+t1, it+t2);
                else gc.drawImage(new Image("images/flower"+(int)(Math.random()*12)+".png"), jt+t1, it+t2);
                if (Math.random() < 0.3) gc.drawImage(new Image("images/rock"+(int)(Math.random()*8)+".png"), jt+t2, it+t1);
                else gc.drawImage(new Image("images/flower"+(int)(Math.random()*12)+".png"), jt+t2, it+t1);
                if (Math.random() < 0.9) gc.drawImage(new Image("images/tree"+(int)(Math.random()*11)+".png"), jt+t2, it+t2);
                else gc.drawImage(new Image("images/flower"+(int)(Math.random()*12)+".png"), jt+t2, it+t2);
            }
        }
        // draw pit(s) and gold(s)
        for (int[] pLoc:E.getPits()) gc.drawImage(new Image("images/pit.png"), pLoc[1]*t+t/18.0, pLoc[0]*t+t/18.0);
        for (int[] gLoc:E.getGolds()) gc.drawImage(new Image("images/gold.png"), gLoc[1]*t, gLoc[0]*t);

    }

    private boolean occupiedTile(int i, int j) {
        for (int[] tile : E.getPits()) if (tile[0] == i && tile[1] == j) return true;
        for (int[] tile : E.getGolds()) if (tile[0] == i && tile[1] == j) return true;
        return false;
    }

}