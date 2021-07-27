package Environment.Controllers;

import Environment.Simulators.TestRoom;
import NEAT.Individual;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class BuildEnvController  extends Controller {

    private int worldSize;
    private TestRoom testRoom;
    private Individual human;

    @FXML private TextField WorldSizeT;
    @FXML private Pane DesignPane;
    @FXML private ImageView PitImg, GoldImg, HumanImg, WumpusImg;

    @FXML public void reset() {

        resetElems();

        worldSize = Integer.parseInt(WorldSizeT.getText());
        testRoom.setWorldSize(worldSize);

        // draw tiles
        DesignPane.getChildren().clear();
        ArrayList<BlueprintRec> tiles = new ArrayList<>();
        for (int i = 0; i < worldSize; ++i)
            for (int j = 0; j < worldSize; ++j)
                tiles.add(new BlueprintRec(DesignPane, 200, j, i));
        DesignPane.getChildren().addAll(tiles);

    }

    @FXML public void random() {

    }

    @FXML public void done() throws IOException {

        // build custom blueprint from completed design pane
        boolean wumpusFound = false, humanFound = false;
        char[][][] customBlueprint = new char[worldSize][worldSize][4];
        for (int i = 0; i < worldSize; ++i)
            for (int j = 0; j < worldSize; ++j)
                for (int k = 0; k < 4; ++k) customBlueprint[i][j][k] = ' ';
        for (Node child : DesignPane.getChildren()) {
            BlueprintRec tile = BlueprintRec.purify(child);
            int x = tile.getGridX();
            int y = tile.getGridY();
            if (tile.getTileType() == BlueprintRec.TileType.PIT) customBlueprint[x][y][0] = 'P';
            else if (tile.getTileType() == BlueprintRec.TileType.GOLD) customBlueprint[x][y][2] = 'G';
            else if (tile.getTileType() == BlueprintRec.TileType.UP) {
                humanFound = true;
                customBlueprint[x][y][3] = 'N';
            }
            else if (tile.getTileType() == BlueprintRec.TileType.DOWN) {
                humanFound = true;
                customBlueprint[x][y][3] = 'S';
            }
            else if (tile.getTileType() == BlueprintRec.TileType.LEFT) {
                humanFound = true;
                customBlueprint[x][y][3] = 'W';
            }
            else if (tile.getTileType() == BlueprintRec.TileType.RIGHT) {
                humanFound = true;
                customBlueprint[x][y][3] = 'E';
            }
            else if (tile.getTileType() == BlueprintRec.TileType.WUMPUS) {
                wumpusFound = true;
                customBlueprint[x][y][1] = 'W';
            }
        }
        if (!humanFound) {
            Stage missingHumanWindow = new Stage();
            missingHumanWindow.initOwner(primaryStage);
            missingHumanWindow.setScene(Utils.makeWarningScene("Missing Human\nLocation.", missingHumanWindow));
            missingHumanWindow.show();
            return;
        }
        if (!wumpusFound) {
            Stage missingWumpusWindow = new Stage();
            missingWumpusWindow.initOwner(primaryStage);
            missingWumpusWindow.setScene(Utils.makeWarningScene("Missing Wumpus\nLocation.", missingWumpusWindow));
            missingWumpusWindow.show();
            return;
        }

        // make pop-up window
        Stage simulateWindow = new Stage();

        simulateWindow.setTitle("Wumpus World Simulation");
        simulateWindow.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/images/simulation.png")).toExternalForm()));
        // simulation window initialization
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/simulation.fxml"));
        Parent sRoot = loader.load();
        SimulationController sc = loader.getController();
        sc.makeSimulationScene(simulateWindow, testRoom, customBlueprint, human);
        Scene simulationScene = new Scene(sRoot);
        simulationScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/SimulationStyle.css")).toExternalForm());
        simulateWindow.setResizable(true);
        simulateWindow.setScene(simulationScene);

        primaryStage.close();
        simulateWindow.show();

    }

    public void makeBuildEnvScene(Stage s, TestRoom testRm, Individual h) {

        primaryStage = s;
        testRoom = testRm;
        human = h;

        WorldSizeT.setEditable(false);
        resetElems();

        primaryStage.addEventFilter(KeyEvent.KEY_PRESSED, ke -> {
            KeyCode keyCode = ke.getCode();
            if (keyCode == KeyCode.A || keyCode == KeyCode.W ||
                    keyCode == KeyCode.D || keyCode == KeyCode.S ||
                    keyCode.isArrowKey()) {
                BlueprintRec humanTile = null;
                for (Node child : DesignPane.getChildren()) { // get the tile containing human
                    BlueprintRec tile = BlueprintRec.purify(child);
                    if (tile.getTileType() == BlueprintRec.TileType.UP ||
                            tile.getTileType() == BlueprintRec.TileType.DOWN ||
                            tile.getTileType() == BlueprintRec.TileType.LEFT ||
                            tile.getTileType() == BlueprintRec.TileType.RIGHT) {
                        humanTile = tile;
                        break;
                    }
                }
                if (humanTile == null) {
                    Stage missingHumanWindow = new Stage();
                    missingHumanWindow.initOwner(primaryStage);
                    missingHumanWindow.setScene(Utils.makeWarningScene("Human has not\nbeen placed.", missingHumanWindow));
                    missingHumanWindow.show();
                } else {
                    DesignPane.getChildren().remove(humanTile);
                    DesignPane.getChildren().add(humanTile.getDirectionReplace(keyCode));
                }
            }
        });

        PitImg.setOnDragDetected(e -> { // many pits can be copied
            Dragboard db = PitImg.startDragAndDrop(TransferMode.COPY);
            ClipboardContent content = new ClipboardContent();
            content.putString("PitImg");
            db.setContent(content);
            e.consume();
        });
        GoldImg.setOnDragDetected(e -> { // many golds can be copied
            Dragboard db = GoldImg.startDragAndDrop(TransferMode.COPY);
            ClipboardContent content = new ClipboardContent();
            content.putString("GoldImg");
            db.setContent(content);
            e.consume();
        });
        HumanImg.setOnDragDetected(e -> { // only 1 human can be moved
            Dragboard db = HumanImg.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString("HumanImg");
            db.setContent(content);
            e.consume();
        });
        HumanImg.setOnDragDone(e -> { // if design pane is populated, consider the drag valid
            if (DesignPane.getChildren().size() > 0) HumanImg.setImage(null);
        });
        WumpusImg.setOnDragDetected(e -> { // only 1 wumpus can be moved
            Dragboard db = WumpusImg.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString("WumpusImg");
            db.setContent(content);
            e.consume();
        });
        WumpusImg.setOnDragDone(e -> { // if design pane is populated, consider the drag valid
            if (DesignPane.getChildren().size() > 0) WumpusImg.setImage(null);
        });

    }

    private void resetElems() {
        PitImg.setImage(new Image(Objects.requireNonNull(getClass().getResource("/images/bppit.png")).toExternalForm()));
        GoldImg.setImage(new Image(Objects.requireNonNull(getClass().getResource("/images/bpgold.png")).toExternalForm()));
        HumanImg.setImage(new Image(Objects.requireNonNull(getClass().getResource("/images/bpdown.png")).toExternalForm()));
        WumpusImg.setImage(new Image(Objects.requireNonNull(getClass().getResource("/images/bpwumpus.png")).toExternalForm()));
    }

}
