package Environment.Controllers;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.Objects;

public class BlueprintRec extends Rectangle {

    public enum TileType { EMPTY, PIT, GOLD, UP, DOWN, LEFT, RIGHT, WUMPUS }

    private final int tileSize;
    private boolean occupied;
    private TileType tileType;
    private final Pane motherPane;

    public BlueprintRec(Pane p, int size, double x, double y) {
        tileSize = size;
        tileType = TileType.EMPTY;
        motherPane = p;
        occupied = false;
        setWidth(size);
        setHeight(size);
        setLayoutX(x * size);
        setLayoutY(y * size);
        setOnDragOver(e -> {
            if (e.getGestureSource() != this && e.getDragboard().hasString())
                e.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        });
        setOnDragDropped(e -> {
            if (occupied) {
                Stage occupiedErrorWindow = new Stage();
                occupiedErrorWindow.setScene(Utils.makeWarningScene("Blueprint Tile\nIs Occupied.", occupiedErrorWindow));
                occupiedErrorWindow.show();
            }
            else {
                Dragboard db = e.getDragboard();
                if (db.hasString()) {
                    motherPane.getChildren().remove(this);
                    motherPane.getChildren().add(toStackPane(db.getString()));
                    e.setDropCompleted(true);
                } else e.setDropCompleted(false);
            }
            e.consume();
        });
        setOnDragEntered(e -> setFill(occupied ? Color.rgb(255, 59, 48) : Color.rgb(52, 199, 89)));
        setOnDragExited(e -> setFill(Color.rgb(64, 200, 224))); // as specified in CSS
    }

    public int getGridX() { return (int) (getLayoutY() / tileSize); }
    public int getGridY() { return (int) (getLayoutX() / tileSize); }

    public static BlueprintRec purify(Node node) {
        if (node instanceof StackPane) {
            BlueprintRec tile = (BlueprintRec) ((StackPane) node).getChildren().get(0);
            tile.setLayoutX(node.getLayoutX());
            tile.setLayoutY(node.getLayoutY());
            return tile;
        }
        else return (BlueprintRec) node;
    }

    public TileType getTileType() { return tileType; }

    public StackPane getDirectionReplace(KeyCode keyCode) {

        BlueprintRec newTile = new BlueprintRec(motherPane, tileSize, getLayoutX()/tileSize, getLayoutY()/tileSize);
        newTile.occupied = true;
        Image newImg = new Image(Objects.requireNonNull(getClass().getResource("/images/error.png")).toExternalForm());
        if (keyCode == KeyCode.W || keyCode == KeyCode.UP) {
            newImg = new Image(Objects.requireNonNull(getClass().getResource("/images/bpup.png")).toExternalForm());
            newTile.tileType = TileType.UP;
        } else if (keyCode == KeyCode.S || keyCode == KeyCode.DOWN) {
            newImg = new Image(Objects.requireNonNull(getClass().getResource("/images/bpdown.png")).toExternalForm());
            newTile.tileType = TileType.DOWN;
        } else if (keyCode == KeyCode.A || keyCode == KeyCode.LEFT) {
            newImg = new Image(Objects.requireNonNull(getClass().getResource("/images/bpleft.png")).toExternalForm());
            newTile.tileType = TileType.LEFT;
        } else if (keyCode == KeyCode.D || keyCode == KeyCode.RIGHT) {
            newImg = new Image(Objects.requireNonNull(getClass().getResource("/images/bpright.png")).toExternalForm());
            newTile.tileType = TileType.RIGHT;
        }

        StackPane newStackPane = new StackPane(newTile, new ImageView(newImg));
        newStackPane.setLayoutX(getLayoutX());
        newStackPane.setLayoutY(getLayoutY());
        newStackPane.setOnMouseClicked(e -> {
            MouseButton eventButton = e.getButton();
            if (eventButton == MouseButton.SECONDARY) {
                motherPane.getChildren().remove(newStackPane);
                motherPane.getChildren().add(new BlueprintRec(motherPane, tileSize, newStackPane.getLayoutX()/tileSize, newStackPane.getLayoutY()/tileSize));
            }
        });

        return newStackPane;

    }

    private StackPane toStackPane(String dragIdentifier) {

        BlueprintRec newTile = new BlueprintRec(motherPane, tileSize, getLayoutX()/tileSize, getLayoutY()/tileSize);
        newTile.occupied = true;
        Image image = new Image(Objects.requireNonNull(getClass().getResource("/images/error.png")).toExternalForm());
        switch (dragIdentifier) {
            case "PitImg":
                image = new Image(Objects.requireNonNull(getClass().getResource("/images/bppit.png")).toExternalForm());
                newTile.tileType = TileType.PIT;
                break;
            case "GoldImg":
                image = new Image(Objects.requireNonNull(getClass().getResource("/images/bpgold.png")).toExternalForm());
                newTile.tileType = TileType.GOLD;
                break;
            case "HumanImg":
                image = new Image(Objects.requireNonNull(getClass().getResource("/images/bpdown.png")).toExternalForm());
                newTile.tileType = TileType.DOWN;
                break;
            case "WumpusImg":
                image = new Image(Objects.requireNonNull(getClass().getResource("/images/bpwumpus.png")).toExternalForm());
                newTile.tileType = TileType.WUMPUS;
                break;
        }

        StackPane stackPane = new StackPane(newTile, new ImageView(image));
        stackPane.setLayoutX(getLayoutX());
        stackPane.setLayoutY(getLayoutY());
        stackPane.setOnMouseClicked(e -> {
            MouseButton eventButton = e.getButton();
            if (eventButton == MouseButton.SECONDARY) {
                motherPane.getChildren().remove(stackPane);
                motherPane.getChildren().add(new BlueprintRec(motherPane, tileSize, stackPane.getLayoutX()/tileSize, stackPane.getLayoutY()/tileSize));
            }
        });

        return stackPane;

    }

}
