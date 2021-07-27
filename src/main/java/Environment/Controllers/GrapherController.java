package Environment.Controllers;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.sql.*;

public class GrapherController extends Controller {

    private Connection dbConn;
    private Statement query;
    private int accessID;

    @FXML private Pane graphPane;

    @FXML public void scoreGraph() throws SQLException {

        XYChart.Series<Number, Number> avgSeries = new XYChart.Series<>();
        avgSeries.setName("Avg Score");

        try (ResultSet resultSet = query.executeQuery(String.format("CALL getAvgScoreProgress(%s);", accessID))) {
            while (resultSet.next()) {
                avgSeries.getData().add(new XYChart.Data<>(
                        resultSet.getInt("Step_No"),
                        resultSet.getInt("Avg_Score")
                ));
            }
        }


        XYChart.Series<Number, Number> maxSeries = new XYChart.Series<>();
        maxSeries.setName("Max Score");

        try (ResultSet resultSet = query.executeQuery(String.format("CALL getMaxScoreProgress(%s);", accessID))) {
            while (resultSet.next()) {
                maxSeries.getData().add(new XYChart.Data<>(
                        resultSet.getInt("Step_No"),
                        resultSet.getInt("Max_Score")
                ));
            }
        }

        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Evolution Step");
        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Score Progression");
        chart.getData().addAll(avgSeries, maxSeries);

        graphPane.getChildren().clear();
        graphPane.getChildren().add(chart);

    }

    @FXML public void speciesGraph() throws SQLException {

        XYChart.Series<Number, Number> speciesSeries = new XYChart.Series<>();
        speciesSeries.setName("Num Species");

        try (ResultSet resultSet = query.executeQuery(String.format("CALL getNumSpeciesProgress(%s);", accessID))) {
            while (resultSet.next()) {
                speciesSeries.getData().add(new XYChart.Data<>(
                        resultSet.getInt("Step_No"),
                        resultSet.getInt("Num_Species")
                ));
            }
        }

        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Evolution Step");
        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Number of Species Progression");
        chart.getData().add(speciesSeries);

        graphPane.getChildren().clear();
        graphPane.getChildren().add(chart);

    }

    public void makeGrapherScene(Stage s, int id) throws SQLException {

        primaryStage = s;
        accessID = id;

        dbConn = DriverManager.getConnection(Utils.databaseURL);
        query = dbConn.createStatement();
        query.execute("USE Population;");
        System.out.println("Grapher's database connection established.");

        s.setOnCloseRequest(e -> {
            try {
                query.close();
                dbConn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.out.println("Grapher's database connection de-allocated.");
        });

    }

}
