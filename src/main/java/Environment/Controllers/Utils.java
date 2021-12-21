package Environment.Controllers;

import NEAT.DataStructures.AncestorTree;
import NEAT.DataStructures.GraphicPack;
import NEAT.Genome.DecisionHead.DHng;
import NEAT.Genome.MemoryHead.MHng;
import NEAT.Individual;
import NEAT.Species;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Line;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import static NEAT.Lambdas.Graphics.*;
import static NEAT.NEAT.*;

public class Utils {

    /** ================================================================================================================
     * DATABASE UTILS
     * - Every method here (except pingDatabaseServer) only work when database server is online
     **/

    public static final String databaseURL = "jdbc:mysql://localhost:3306?user=root&password=password&useSSL=false";
    public static boolean dbOnline;

    /**
     * IMPORTANT:
     * - This method needs to be run at application startup
     * - This method is only run once to avoid database corruption
     */
    public static void pingDatabaseServer() {

        System.out.println("Ping database server...");
        try {
            DriverManager.getConnection(databaseURL);
            dbOnline = true;
            System.out.println("Database server is online.");
        }
        catch (SQLException ex) {
            dbOnline = false;
            System.out.println("Database server is offline.");
            Stage popup = new Stage();
            popup.setScene(makeWarningScene("Warning: Database server is offline.\nThis run will not be logged.", popup));
            popup.show();
        }

    }

    /**
     * Turns off database server logging for current run
     */
    public static void disableLogging() {

        if (!dbOnline) return;

        dbOnline = false;
        System.out.println("This run will NOT be logged onto the database server.");
        Stage popup = new Stage();
        popup.setScene(makeWarningScene("Warning: This run will NOT be logged\nonto the database server.", popup));
        popup.show();

    }

    /**
     * Uploads contents in file to the database server
     * @param file - the file with contents to be uploaded
     * @throws IOException - file resetting exception
     */
    public static void fileToDB(File file) throws IOException {

        if (!dbOnline) return;

        // upload everything in temp file into database
        try (Connection dbConn = DriverManager.getConnection(databaseURL)) {
            try (Statement query = dbConn.createStatement()) {
                query.addBatch("USE Population;");
                try (Scanner scanner = new Scanner(file)) {
                    if (scanner.hasNextLine()) scanner.nextLine(); // skip initial empty line
                    else return; // file is empty so ignore
                    while (scanner.hasNextLine()) query.addBatch(scanner.nextLine());
                    query.executeBatch();
                }
            }
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }

        // reset the temp file in case of next upload
        if (file.delete()) System.out.println("Temp file deleted.");
        else System.out.println("This should not happen.");
        if (file.createNewFile()) System.out.println("Temp file successfully reset!");
        else System.out.println("This should not happen.");

    }

    /**
     * Database-server On Termination protocol
     * @param file - the file with contents to be uploaded
     * @param accessID - the ID of this access
     * @param genNo - the latest generation number of this access
     * @param latestScore - the latest score of this access
     */
    public static void DOT(File file, int accessID, String genNo, String latestScore) {

        if (!dbOnline) return;

        // try upload one last time in case of unsaved data
        try {
            fileToDB(file);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        // finalize run information into database
        completeAccess(accessID, genNo, latestScore);

        // delete temp file
        if (file.delete()) System.out.println("Temp file deleted.");
        else System.out.println("This should not happen.");

    }

    /**
     * Inserts this access onto the database and returns the unique ID of the access
     * @param worldSize - world square 2D dimension
     * @param timeSteps - number of time step per episode
     * @param maxPop - individual count upperbound
     * @param numTrEps - number of training episodes
     * @param numTeEps - number of testing episodes
     * @return unique ID for the inserted access
     */
    public static int insertAccess(int worldSize, int timeSteps, int maxPop, int numTrEps, int numTeEps) {

        if (!dbOnline) return -1;

        int accessID = -1;
        try (Connection dbConn = DriverManager.getConnection(databaseURL)) {
            try (Statement query = dbConn.createStatement()) {
                query.execute("USE Population;");
                query.executeUpdate(String.format(
                        "INSERT INTO Accesses(Access_Time, Access_Date, World_Size, No_Time_Steps, Max_Population, No_TrEps, No_TeEps)" +
                                " VALUES (\"%s\" , \"%s\" , %s , %s , %s , %s , %s);",
                        LocalTime.now(),
                        LocalDate.now(), worldSize, timeSteps, maxPop, numTrEps, numTeEps));
                try (ResultSet rs = query.executeQuery("SELECT LAST_INSERT_ID() id;")) {
                    if (rs.next()) accessID = rs.getInt("id");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        if (accessID == -1) throw new IllegalStateException("Insert Access fails.");
        return accessID;

    }

    /**
     * Updates the database with the final information of this access
     * @param accessID - the ID of this access
     * @param genNo - the latest generation number of this access
     * @param latestScore - the latest score of this access
     */
    public static void completeAccess(int accessID, String genNo, String latestScore) {

        if (!dbOnline) return;

        try (Connection dbConn = DriverManager.getConnection(databaseURL)) {
            try (Statement query = dbConn.createStatement()) {
                query.execute("USE Population;");
                query.executeUpdate(String.format("UPDATE Accesses SET Max_Generation = %s WHERE Access_ID = %s;", genNo, accessID));
                query.executeUpdate(String.format("UPDATE Accesses SET Latest_Score = \"%s\" WHERE Access_ID = %s;", latestScore, accessID));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

    }

    /**
     * Parse the population into a file. To be called after each evolution
     * @param file - the file to be parsed onto
     * @param accessID - the ID of this access
     * @param stepNo - evolution step number before this evolution happen
     * @param stepList - the population at each step
     * @param speciesProgression - the species at each step
     * @throws IOException - file writing exception
     */
    public static void fileParse(File file, int accessID, int stepNo, List<List<Individual>> stepList, List<List<Species>> speciesProgression) throws IOException {
/*                                                                                                         BECAUSE ACTOR-CRITIC UPDATE TO LSTM
        if (!dbOnline) return;

        if (file == null) throw new IllegalStateException("File is not initialized.");

        StringBuilder text = new StringBuilder();

        for (int i = 0, n = stepList.size(); i < n; ++i) {

            List<Individual> populationStep = stepList.get(i);
            List<Species> ecosystemStep = speciesProgression.get(i);
            stepNo++;

            for (Individual individual : populationStep) {

                MRUGenome MRU = individual.getMRU();
                ACUGenome ACU = individual.getACU();

                // Access_ID , Step_No , Score , Species_ID , Parent_Species_ID , MRU_ID , ACU_ID
                text.append(String.format("\nINSERT INTO Individuals VALUES (%s , %s , \"%s\" , \"%s\" , \"%s\" , %s , %s);",
                        accessID, stepNo, individual.getScore(), (individual.getSpecies() != null) ? individual.getSpecies().getID() : "NEW",
                        (individual.getParentSpecies() != null) ? individual.getParentSpecies().getID() : "N/A", MRU.getID(), ACU.getID()));

                for (MRUConGene c : MRU.getCons().getData())
                    // Access_ID , Step_No , MRU_ID , ACU_ID , MRU_Con_IN , From_Node_IN , To_Node_IN , Forget_Weight , Input_Weight , Candidate_Weight , Output_Weight
                    text.append(String.format("\nINSERT INTO MRUs VALUES (%s , %s , %s , %s , %s , %s , %s , %s , %s , %s , %s);",
                            accessID, stepNo, MRU.getID(), ACU.getID(), c.getIN(), c.getFG().getIN(), c.getTG().getIN(),
                            c.getWeight('f'), c.getWeight('i'), c.getWeight('c'), c.getWeight('o')));

                for (ACUConGene c : ACU.getCons().getData())
                    // Access_ID , Step_No , ACU_ID , MRU_ID , ACU_Con_IN , From_Node_IN , To_Node_IN , Critic_Weight , Actor_Weight
                    text.append(String.format("\nINSERT INTO ACUs VALUES (%s , %s , %s , %s , %s , %s , %s , %s , %s);",
                            accessID, stepNo, ACU.getID(), MRU.getID(), c.getIN(), c.getFG().getIN(), c.getTG().getIN(),
                            c.getWeight(false), c.getWeight(true)));

            }

            for (Species species : ecosystemStep) {

                Individual individual = species.getRepr();
                // Access_ID , Step_No , Species_ID, Score , Ancestor_ID , Repr_MRU_ID , Repr_ACU_ID , Population_Size
                text.append(String.format("\nINSERT INTO Species VALUES (%s , %s , %s , %s , %s , %s , %s , %s);",
                        accessID, stepNo, species.getID(), species.getScore(), species.getPredID(),
                        individual.getMRU().getID(), individual.getACU().getID(), species.size()));

            }

        }

        FileWriter myWriter = new FileWriter(file.getName(), true);
        myWriter.write(text.toString());
        myWriter.close();
*/
    }

    /** ================================================================================================================
     * GRAPHICAL UTILS
     **/

    /**
     * Assists making common error scene for pop-up windows
     * @param msg - message to be displayed on the scene
     * @param window - the owner window
     * @return the error scene
     */
    public static Scene makeWarningScene(String msg, Stage window) {

        window.setTitle("ATTENTION!");
        window.getIcons().add(new Image(Objects.requireNonNull(Utils.class.getResource("/images/error.png")).toExternalForm()));
        window.setResizable(false);

        // **START: ERROR SCENE**
        Label errorMessage = new Label(msg);
        Button closeError = new Button("Acknowledge");
        VBox errorBox = new VBox(errorMessage, closeError);
        closeError.setOnAction(e -> window.close());
        Scene errorScene = new Scene(errorBox, msg.length() * 10, 200);
        errorScene.getStylesheets().add(Objects.requireNonNull(Utils.class.getResource("/css/CommonPopUpStyle.css")).toExternalForm());
        // **END: ERROR SCENE**

        return errorScene;

    }

    /**
     * Gets a customized tool-tip
     * @param str - the tip
     * @return the tool-tip
     */
    public static Tooltip customTooltip(String str) {
        Tooltip tooltip = new Tooltip(str);
        tooltip.setShowDelay(Duration.ZERO);
        tooltip.setShowDuration(Duration.INDEFINITE);
        tooltip.setHideDelay(Duration.ZERO);
        return tooltip;
    }

    /**
     * Creates a simple pop-up
     * @param stage - the root stage
     * @param title - the title
     * @param content - the content
     */
    public static void inspectPopUp(Stage stage, String title, String content) {

        // make pop-up window
        Stage inspectWindow = new Stage();
        inspectWindow.initModality(Modality.NONE);
        inspectWindow.initOwner(stage);
        inspectWindow.setTitle(title);
        inspectWindow.setResizable(false);

        // **START: INSPECT SCENE**
        ScrollPane inspectInfo = new ScrollPane(new Label(content));
        Button closeInspect = new Button("Done");
        VBox inspectBox = new VBox(inspectInfo, closeInspect);
        inspectInfo.setPrefSize(400, 500);
        closeInspect.setOnAction(e -> inspectWindow.close());
        Scene inspectScene = new Scene(inspectBox);
        inspectScene.getStylesheets().add(Objects.requireNonNull(Utils.class.getResource("/css/CommonPopUpStyle.css")).toExternalForm());
        // **END: INSPECT SCENE**

        inspectWindow.setScene(inspectScene);
        inspectWindow.show();

    }

    /**
     * Parses the genetic information of an individual onto a string and returns it
     * @param i - the individual
     * @return genetic information string of the individual
     */
    public static String parseGene(Individual i) {

        StringBuilder geneticInfo = new StringBuilder("\nIndividual\n" + i);

        geneticInfo.append("\nMRU\n");
        for (MHng n : i.getMRU().getNodes().getData()) {
            StringBuilder allConsDetails = n.parseInCons();
            geneticInfo.append(n.inspect()).append(allConsDetails).append("\n");
        }

        geneticInfo.append("\nACU\n");
        for (DHng n : i.getACU().getNodes().getData()) {
            StringBuilder allConsDetails = n.parseInCons();
            geneticInfo.append(n.inspect()).append(allConsDetails).append("\n");
        }

        return geneticInfo.toString();

    }

    /**
     * Recursively layouts the species ancestor tree
     * @param cur - current node to draw
     * @param pane - root pane
     * @param d - distance
     * @param x - x-coordinate
     * @param y - y-coordinate
     */
    public static void drawAncTree(AncestorTree.Node cur, Pane pane, double d, double x, double y) {

        if (cur == null) return; // no species exists yet

        Species s = cur.getValue();
        Button button = new Button(Integer.toString(((s != null) ? s.getID() : NULL_SPECIES)));
        button.setPrefSize(2*d, d);
        button.setLayoutX(x);
        button.setLayoutY(y);
        button.setTooltip(customTooltip((s != null) ? ("Size: " + s.size() + "\nScore: " + s.getScore() + "\nRepr: " + s.getRepr()) : "Nothingness"));
        button.setStyle(cur.getActive() ? "-fx-background-color: green;" : "-fx-background-color: red;");
        button.setOnAction(e -> {
            Stage stage = new Stage();
            stage.initModality(Modality.NONE);
            Pane drawPane = new Pane();
            Scene scene;
            if (s == null) scene = makeWarningScene("This is the initial population.\nNo species exist yet", stage);
            else {
                stage.setTitle("Species " + s.getID());
                Utils.drawIndividual(stage, drawPane, cur.getStructure());
                scene = new Scene(new ScrollPane(new HBox(drawPane)), 500, 500);
                scene.getStylesheets().add(Objects.requireNonNull(Utils.class.getResource("/css/NEATLabStyle.css")).toExternalForm());
            }
            stage.setScene(scene);
            stage.show();
        });
        pane.getChildren().add(button);

        double succX = x, succY = y + 2 * d;
        for (AncestorTree.Node succ : cur.getSuccessors()) {
            double randFactor = Math.random() * 100;
            pane.getChildren().add(new Line(x+d/2.0, y+d/2.0, succX+d/2.0, succY+d/2.0+randFactor));
            drawAncTree(succ, pane, d, succX, succY + randFactor);
            succX += 2 * d;
        }

    }

    /**
     * Layouts the individual onto the spotlight on root stage
     * @param stage - the root stage
     * @param spotlight - the spotlight
     * @param i - the individual to be displayed
     */
    public static void drawIndividual(Stage stage, Pane spotlight, Individual i) {

        double r = 25; // radius of a node, as specified in CSS

        // clear spotlight
        spotlight.getChildren().clear();

        // present individual
        Button help = new Button("i");
        help.setPrefSize(200, 10);
        help.setTooltip(customTooltip(i.toString()));
        help.setOnAction(e -> inspectPopUp(stage, "Selected Individual", i.toString()));
        spotlight.getChildren().add(help);

        // draw memory head
        for (MHng n : i.getMRU().getNodes().getData()) {
            double x = n.getX(), y = n.getY();
            String nodeDetails = n.inspect();
            Button node = new Button();
            node.setLayoutX(x);
            node.setLayoutY(y);
            node.setTooltip(customTooltip(nodeDetails));
            StringBuilder allConsDetails = new StringBuilder();
            ArrayList<CubicCurve> curves = new ArrayList<>();
            double intensity = 0;
            GraphicPack[] graphicPacks = n.getGraphicInCons();
            for (GraphicPack pack : graphicPacks) {
                intensity += pack._enabled ? 1 : 0;
                String conDetails = pack._inspected;
                double sx = pack._fgx + r, sy = pack._fgy + r/2;
                CubicCurve cubicCurve = new CubicCurve(sx, sy,
                        (3 * sx + x) / 4.0, firstQuadY.apply(sy, y + r/2),
                        (sx + 3 * x) / 4.0, secondQuadY.apply(sy, y + r/2),
                        x, y + r/2);
                cubicCurve.setStroke((pack._enabled) ? Color.rgb(14, 77, 146) : Color.rgb(178, 190, 181));
                cubicCurve.setFill(Color.color(0, 0, 0, 0));
                if (pack._enabled) cubicCurve.setEffect(new DropShadow(BlurType.GAUSSIAN,
                        Color.rgb(255, 255, 255, 0.7), 30, 0.2, 0.0, 0.0));
                Tooltip.install(cubicCurve, customTooltip(conDetails));
                allConsDetails.append("\n").append(conDetails);
                curves.add(cubicCurve);
                spotlight.getChildren().add(cubicCurve);
            }
            if (OBS_NODES_Y.contains(y)) // make observation nodes glow white
                node.setEffect(new DropShadow(BlurType.GAUSSIAN,
                        Color.rgb(255, 253, 250, Math.min(0.5 + (intensity / 10.0), 1)), 1, 0.75, 0.0, 0.0));
            else if (INF_NODES_Y.contains(y)) // make inflection nodes glow red
                node.setEffect(new DropShadow(BlurType.GAUSSIAN,
                        Color.rgb(255, 40, 0, Math.min(0.5 + (intensity / 10.0), 1)), 1, 0.75, 0.0, 0.0));
            else // make other nodes glow yale
                node.setEffect(new DropShadow(BlurType.GAUSSIAN,
                        Color.rgb(14, 77, 146, Math.min(0.5 + (intensity / 10.0), 1)), intensity, 0.75, 0.0, 0.0));
            node.setOnAction(e -> inspectPopUp(stage, "Node " + n, nodeDetails + allConsDetails));
            node.setOnMouseEntered(e -> { for (CubicCurve curve : curves) curve.setStrokeWidth(3); });
            node.setOnMouseExited(e -> { for (CubicCurve curve : curves) curve.setStrokeWidth(1); });
            spotlight.getChildren().add(node);
        }

        // draw decision head
        for (DHng n : i.getACU().getNodes().getData()) {
            double x = n.getX(), y = n.getY();
            String nodeDetails = n.inspect();
            Button node = new Button();
            if (x == hiddenNodeX) x += r; // intersection case
            node.setLayoutX(x);
            node.setLayoutY(y);
            node.setTooltip(customTooltip(nodeDetails));
            StringBuilder allConsDetails = new StringBuilder();
            ArrayList<CubicCurve> curves = new ArrayList<>();
            int intensity = 0;
            GraphicPack[] graphicPacks = n.getGraphicInCons();
            for (GraphicPack pack : graphicPacks) {
                intensity += pack._enabled ? 1 : 0;
                String conDetails = pack._inspected;
                double sx = pack._fgx + r, sy = pack._fgy + r/2;
                if (sx == hiddenNodeX + r) sx += r; // intersection case
                CubicCurve cubicCurve = new CubicCurve(sx, sy,
                        (3 * sx + x) / 4.0, firstQuadY.apply(sy, y + r/2),
                        (sx + 3 * x) / 4.0, secondQuadY.apply(sy, y + r/2),
                        x, y + r/2);
                cubicCurve.setStroke((pack._enabled) ? Color.rgb(80, 220, 100) : Color.rgb(178, 190, 181));
                cubicCurve.setFill(Color.color(0, 0, 0, 0));
                if (pack._enabled) cubicCurve.setEffect(new DropShadow(BlurType.GAUSSIAN,
                        Color.rgb(255, 255, 255, 0.7), 30, 0.2, 0.0, 0.0));
                Tooltip.install(cubicCurve, customTooltip(conDetails));
                allConsDetails.append("\n").append(conDetails);
                curves.add(cubicCurve);
                spotlight.getChildren().add(cubicCurve);
            }
            if (CRITIC_NODES_Y.contains(y)) // make critic node glow orange
                node.setEffect(new DropShadow(BlurType.GAUSSIAN,
                        Color.rgb(253, 106, 2, Math.min(0.5 + (intensity / 10.0), 1)), intensity, 0.75, 0.0, 0.0));
            else if (SEER_NODES_Y.contains(y)) // make seer node glow violet
                node.setEffect(new DropShadow(BlurType.GAUSSIAN,
                        Color.rgb(91, 10, 145, Math.min(0.5 + (intensity / 10.0), 1)), intensity, 0.75, 0.0, 0.0));
            else // make other nodes glow emerald
                node.setEffect(new DropShadow(BlurType.GAUSSIAN,
                        Color.rgb(80, 220, 100, Math.min(0.5 + (intensity / 10.0), 1)), intensity, 0.75, 0.0, 0.0));
            node.setOnAction(e -> inspectPopUp(stage, "Node " + n, nodeDetails + allConsDetails));
            node.setOnMouseEntered(e -> { for (CubicCurve curve : curves) curve.setStrokeWidth(3); });
            node.setOnMouseExited(e -> { for (CubicCurve curve : curves) curve.setStrokeWidth(1); });
            spotlight.getChildren().add(node);
        }

    }

    /**
     * Makes a Wumpus World blueprint and returns it
     * @param worldSize - world size
     * @param randHLoc - random human location lock
     * @param randWLoc - random wumpus location lock
     * @param numPits - number of pit tiles
     * @param numGolds - number of gold tiles
     * @param pits - array of locations of pit tiles (leave null if random)
     * @param golds - array of locations of gold tiles (leave null if random)
     * @return the blueprint for environment building
     */
    public static char[][][] makeBlueprint(int worldSize, boolean randHLoc, boolean randWLoc,
                                     int numPits, int numGolds, int[][] pits, int[][] golds) {

        int hX = 0, hY = 0, wX = worldSize - 1, wY = worldSize - 1;

        char[][][] bluePrint = new char[worldSize][worldSize][4];
        boolean[][] occupied = new boolean[worldSize][worldSize];

        for (int i = 0; i < worldSize; ++i) { // empty world map
            for (int j = 0; j < worldSize; ++j) {
                occupied[i][j] = false;
                for (int k = 0; k < 4; ++k) bluePrint[i][j][k] = ' ';
            }
        }

        char hD = 'S';
        if (randHLoc) {
            hX = (int) (Math.random() * worldSize);
            hY = (int) (Math.random() * worldSize);
            switch ((int) (Math.random() * 4)) {
                case 0: hD = 'N'; break;
                case 1: hD = 'E'; break;
                case 2: hD = 'S'; break;
                case 3: hD = 'W'; break;
            }
        }
        occupied[hX][hY] = true;
        bluePrint[hX][hY][3] = hD;

        if (randWLoc) {
            wX = (int) (Math.random() * worldSize);
            wY = (int) (Math.random() * worldSize);
            while (wX == hX && wX == hY) {
                wX = (int) (Math.random() * worldSize);
                wY = (int) (Math.random() * worldSize);
            }
        }
        occupied[wX][wY] = true;
        bluePrint[wX][wY][1] = 'W';

        int x, y;
        if (pits != null) {
            for (int[] pLoc : pits) {
                x = pLoc[0]; y = pLoc[1];
                occupied[x][y] = true;
                bluePrint[x][y][0] = 'P';
            }
        }
        else {
            for (int i = 0; i < numPits; ++i) {
                x = (int) (Math.random() * worldSize);
                y = (int) (Math.random() * worldSize);
                while ((x == hX && y == hY) || occupied[x][y]) {
                    x = (int) (Math.random() * worldSize);
                    y = (int) (Math.random() * worldSize);
                }
                occupied[x][y] = true;
                bluePrint[x][y][0] = 'P';
            }
        }
        if (golds != null) {
            for (int[] gLoc : golds) {
                x = gLoc[0]; y = gLoc[1];
                occupied[x][y] = true;
                bluePrint[x][y][2] = 'G';
            }
        }
        else {
            for (int i = 0; i < numGolds; ++i) {
                x = (int) (Math.random() * worldSize);
                y = (int) (Math.random() * worldSize);
                while ((x == hX && y == hY) || occupied[x][y]) {
                    x = (int) (Math.random() * worldSize);
                    y = (int) (Math.random() * worldSize);
                }
                occupied[x][y] = true;
                bluePrint[x][y][2] = 'G';
            }
        }

        return bluePrint;

    }

    /**
     * This should only be used when champions are still relevant
     * That is, right after an evolution step
     * @param ecosystem - the ecosystem after an evolution step
     * @return the fittest champion
     */
    public static Individual championOfChampions(List<Species> ecosystem) {
        Individual fittest = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (Species s : ecosystem) {
            if (s.getChampion().getScore() > bestScore) {
                fittest = s.getChampion();
                bestScore = fittest.getScore();
            }
        }
        return fittest;
    }

}
