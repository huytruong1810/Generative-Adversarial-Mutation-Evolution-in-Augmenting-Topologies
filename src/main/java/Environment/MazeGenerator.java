package Environment;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class MazeGenerator extends Application {

    int n = 50; // draw size

    public void drawMaze(Maze m, GraphicsContext gc) {

        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, 1000, 1000);

        gc.setFill(Color.BLACK);
        gc.setLineWidth(5);

        for (int x = 1; x <= m.size; x++) {
            for (int y = 1; y <= m.size; y++) {
                if (m.south[x][y]) gc.strokeLine(x*n, y*n, (x+1)*n, y*n);
                if (m.north[x][y]) gc.strokeLine(x*n, (y+1)*n, (x+1)*n, (y+1)*n);
                if (m.west[x][y]) gc.strokeLine(x*n, y*n, x*n, (y+1)*n);
                if (m.east[x][y]) gc.strokeLine((x+1)*n, y*n, (x+1)*n, (y+1)*n);
            }
        }

    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        Canvas cv = new Canvas(1000, 1000);
        GraphicsContext gc = cv.getGraphicsContext2D();
        TextField startX = new TextField("1");
        TextField startY = new TextField("1");
        TextField mazeSize = new TextField("10");
        Button newMaze = new Button("new maze");

        newMaze.setOnAction(e1 -> {
            Maze m = new Maze(Integer.parseInt(mazeSize.getText()));
            m.generateAt(Integer.parseInt(startX.getText()), Integer.parseInt(startY.getText()));
            drawMaze(m, gc);
        });

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(cv);
        borderPane.setTop(newMaze);
        borderPane.setLeft(new VBox(10, new Label("maze size"), mazeSize,
                new Label("start x:"), startX, new Label("start y:"), startY));

        primaryStage.setScene(new Scene(borderPane));
        primaryStage.setTitle("Maze generator");
        primaryStage.show();

    }
}
