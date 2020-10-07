package Environment;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Screen;

import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

public class SimulationController implements Initializable {

    int t; // tile size
    Environment E;
    Simulation T;
    Sprite human, wumpus;
    private GraphicsContext gc;

    @FXML private Label hStat, wStat;
    @FXML private Canvas WorldCanvas;
    @FXML private Group RootGroup;
    @FXML private Circle hBump, glitter, breeze, stench, scream;
    @FXML private Circle wBump, scentInt, scentDir;
    @FXML private Label scentIntText, scentDirText;

    @FXML public void exitApplication(ActionEvent event) {
        Platform.exit();
    }

    @FXML public void restartApplication(ActionEvent event) {}

    @FXML public void runNextStep(ActionEvent event) {

        int step = T.runTimeStep();
        E = T.getEnvironment();

        hBump.setFill(Color.GHOSTWHITE);
        glitter.setFill(Color.GHOSTWHITE);
        breeze.setFill(Color.GHOSTWHITE);
        stench.setFill(Color.GHOSTWHITE);
        scream.setFill(Color.GHOSTWHITE);
        wBump.setFill(Color.GHOSTWHITE);
        scentInt.setFill(Color.GHOSTWHITE);
        scentDir.setFill(Color.GHOSTWHITE);

        hStat.setText(String.format("Score: %d\nTaken Action: %s", T.getHScore(), T.getHAction()));
        if (E.thereIsHWall()) hBump.setFill(Color.FIREBRICK);
        if (E.thereIsGlitter()) glitter.setFill(Color.GOLD);
        if (E.thereIsBreeze()) breeze.setFill(Color.LIGHTSKYBLUE);
        if (E.thereIsStench()) stench.setFill(Color.DARKSEAGREEN);
        if (E.thereIsScream()) scream.setFill(Color.GHOSTWHITE);
        wStat.setText(String.format("Score: %d\nTaken Action: %s", T.getWScore(), T.getWAction()));
        if (E.thereIsWWall()) wBump.setFill(Color.YELLOWGREEN);
        if (E.thereIsScent()) {
            scentIntText.setText(""+E.getSceneInt());
            scentDirText.setText(""+E.getSceneDir());
            scentInt.setFill(Color.MISTYROSE);
            scentDir.setFill(Color.BURLYWOOD);
        }

        switch (T.getHAction()) {
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
        switch (T.getWAction()) {
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


        switch (step) {
            case 0: System.out.println("continue"); break;
            case -2: System.out.println("no win"); break;
            case -1: System.out.println("agent die"); break;
            case 1: System.out.println("agent win"); break;
            case 2: System.out.println("wumpus win"); break;
        }

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gc = WorldCanvas.getGraphicsContext2D();
    }

    public void makeSimulationScene(int worldSize, int trials, int timeSteps) {

//        int trialScores[] = new int[trials];
//        int trialWumpusScores[] = new int[trials];

        char[][][] bp = WorldDriver.generateBluePrint(worldSize, 2, 1, true, true);
        E = new Environment(worldSize, bp);

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

        // draw cave
        Random rand = new Random();
        String[] greens = new String[]{"tree0.png", "tree1.png", "tree2.png", "tree3.png", "tree4.png", "tree5.png",
                "tree6.png", "tree7.png", "tree8.png", "tree9.png", "tree10.png"};
        String[] grounds = new String[]{"brownsoil.jpg", "brownsoil.jpg", "sandsoil.jpg", "grasssoil.png", "grasssoil.png"};
        int pW = t/18, pH = t/18;
        for (int i = 0; i < worldSize; ++i) {
            for (int j = 0; j < worldSize; ++j) {
                gc.drawImage(new Image("images/"+grounds[rand.nextInt(5)]), j*t, i*t, t, t);
                gc.drawImage(new Image("images/"+greens[rand.nextInt(11)]),
                        j*t+(double)t/(rand.nextInt(10)+90), i*t+(double)t/(rand.nextInt(10)+90));
                gc.drawImage(new Image("images/"+greens[rand.nextInt(11)]),
                        j*t+(double)t/(rand.nextInt(9)+1), i*t+(double)t/(rand.nextInt(9)+1));
            }
        }
        for (int[] pLoc:E.getPits()) {
            gc.drawImage(new Image("images/"+grounds[rand.nextInt(5)]), pLoc[1]*t, pLoc[0]*t, t, t);
            Image pit = new Image("images/pit.png");
            gc.drawImage(pit, pLoc[1]*t+pW, pLoc[0]*t+pH, pit.getWidth()/1.8, pit.getHeight()/1.8);
        }
        for (int[] gLoc:E.getGolds()) {
            gc.drawImage(new Image("images/"+grounds[rand.nextInt(5)]), gLoc[1]*t, gLoc[0]*t, t, t);
            gc.drawImage(new Image("images/gold.png"), gLoc[1]*t, gLoc[0]*t);
        }

        T = new Simulation(E, timeSteps);

//        for (int i = 0; i < trials; ++i) {
//
//            Simulation T = new Simulation(E, timeSteps);
//            trialScores[i] = T.getHScore();
//            trialWumpusScores[i] = T.getWScore();
//
//        }

    }

}