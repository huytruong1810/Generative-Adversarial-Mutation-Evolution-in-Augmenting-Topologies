package Environment.Controllers;

import NEAT.DataStructures.Grapher;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class GrapherController extends Controller {

    private Grapher grapher;

    @FXML private Pane graphPane;

    @FXML public void scoreGraph() {

        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Evolution Step");
        yAxis.setLabel("Score");
        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Score Progression");
        grapher.addScoreSeries(chart);

        graphPane.getChildren().clear();
        graphPane.getChildren().add(chart);

    }

    @FXML public void speciesGraph() {

        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Evolution Step");
        yAxis.setLabel("Species Count");
        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Number of Species Progression");
        grapher.addNumSpeciesSeries(chart);

        graphPane.getChildren().clear();
        graphPane.getChildren().add(chart);

    }

    public void makeGrapherScene(Stage s, Grapher g) {

        primaryStage = s;
        grapher = g;

    }

}
