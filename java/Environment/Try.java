package Environment;

import Neat.DataStructures.RandomHashSet;
import Neat.Genome.ConnectionGene;
import Neat.Genome.Genome;
import Neat.Genome.NodeGene;
import Neat.Neat;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;


public class Try extends Application {

    double r = 25; // neuron radius

    public static void main(String[] args) {
        launch(args);
    }

    public void drawNodes(GraphicsContext gc, RandomHashSet<NodeGene> nodes) {
        NodeGene node;
        for (int i = 0; i < nodes.size(); ++i) {
            node = nodes.get(i);
            gc.setStroke(Color.GHOSTWHITE.brighter());
            gc.setLineWidth(5);
            gc.strokeOval(node.getX(), node.getY(), r, r);
        }
    }

    public void drawConnections(GraphicsContext gc, RandomHashSet<ConnectionGene> connections) {
        ConnectionGene c;
        NodeGene a, b;
        for (int i = 0; i < connections.size(); ++i) {
            c = connections.get(i);
            a = c.getFG();
            b = c.getTG();
            if (c.isEnabled()) gc.setStroke(Color.FORESTGREEN);
            else gc.setStroke(Color.CRIMSON);
            gc.setLineWidth(1);
            gc.strokeLine(a.getX()+r/2, a.getY()+r/2, b.getX()+r/2, b.getY()+r/2);
        }
    }

    @Override
    public void start(Stage stage) {

        Neat neat = new Neat(5, 2, 100);
        Genome g = neat.emptyGenome();

        Button button = new Button("Mutate");
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        Canvas cv = new Canvas(screenBounds.getMaxX(), screenBounds.getMaxY());
        GraphicsContext gc = cv.getGraphicsContext2D();
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: black");


        button.setOnAction(e -> {
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, cv.getWidth(), cv.getHeight());
            g.mutate();
            drawConnections(gc, g.getConnections());
            drawNodes(gc, g.getNodes());
        });

        drawNodes(gc, g.getNodes());

        root.setTop(button);
        root.setCenter(cv);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.setTitle("NEAT Simulator");
        stage.show();

    }

}
