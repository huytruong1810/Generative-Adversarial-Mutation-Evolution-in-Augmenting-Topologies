package RL.Controllers;

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

    private static final int tileSize = 200;
    private boolean occupied;
    private TileType tileType;
    private final Pane motherPane;

    public BlueprintRec(Pane p, double x, double y) {
        tileType = TileType.EMPTY;
        motherPane = p;
        occupied = false;
        setWidth(tileSize);
        setHeight(tileSize);
        setLayoutX(x * tileSize);
        setLayoutY(y * tileSize);
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

        BlueprintRec newTile = new BlueprintRec(motherPane, getLayoutX()/tileSize, getLayoutY()/tileSize);
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
                motherPane.getChildren().add(new BlueprintRec(motherPane, newStackPane.getLayoutX()/tileSize, newStackPane.getLayoutY()/tileSize));
            }
        });

        return newStackPane;

    }

    public static Node translate(char[] here, Pane pane, int x, int y) {

        BlueprintRec tile = new BlueprintRec(pane, x, y);
        Image image = null;

        if (here[0] == 'P') {
            image = new Image(Objects.requireNonNull(BlueprintRec.class.getResource("/images/bppit.png")).toExternalForm());
            tile.tileType = TileType.PIT;
        }
        else if (here[2] == 'G') {
            image = new Image(Objects.requireNonNull(BlueprintRec.class.getResource("/images/bpgold.png")).toExternalForm());
            tile.tileType = TileType.GOLD;
        }
        else if (here[3] == 'N') {
            image = new Image(Objects.requireNonNull(BlueprintRec.class.getResource("/images/bpup.png")).toExternalForm());
            tile.tileType = TileType.UP;
        }
        else if (here[3] == 'S') {
            image = new Image(Objects.requireNonNull(BlueprintRec.class.getResource("/images/bpdown.png")).toExternalForm());
            tile.tileType = TileType.DOWN;
        }
        else if (here[3] == 'W') {
            image = new Image(Objects.requireNonNull(BlueprintRec.class.getResource("/images/bpleft.png")).toExternalForm());
            tile.tileType = TileType.LEFT;
        }
        else if (here[3] == 'E') {
            image = new Image(Objects.requireNonNull(BlueprintRec.class.getResource("/images/bpright.png")).toExternalForm());
            tile.tileType = TileType.RIGHT;
        }
        else if (here[1] == 'W') {
            image = new Image(Objects.requireNonNull(BlueprintRec.class.getResource("/images/bpwumpus.png")).toExternalForm());
            tile.tileType = TileType.WUMPUS;
        }

        if (image != null) {
            tile.occupied = true;
            StackPane stackPane = new StackPane(tile, new ImageView(image));
            stackPane.setLayoutX(tile.getLayoutX());
            stackPane.setLayoutY(tile.getLayoutY());
            stackPane.setOnMouseClicked(e -> {
                MouseButton eventButton = e.getButton();
                if (eventButton == MouseButton.SECONDARY) {
                    pane.getChildren().remove(stackPane);
                    pane.getChildren().add(new BlueprintRec(pane, stackPane.getLayoutX()/tileSize, stackPane.getLayoutY()/tileSize));
                }
            });
            return stackPane;
        }

        tile.occupied = false;
        return tile;

    }

    private StackPane toStackPane(String dragIdentifier) {

        BlueprintRec newTile = new BlueprintRec(motherPane, getLayoutX()/tileSize, getLayoutY()/tileSize);
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
                motherPane.getChildren().add(new BlueprintRec(motherPane, stackPane.getLayoutX()/tileSize, stackPane.getLayoutY()/tileSize));
            }
        });

        return stackPane;

    }

}
