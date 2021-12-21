package NEAT.DataStructures;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;

import java.util.ArrayList;

public class Grapher {

    private final ArrayList<Double> ppltMaxScores, ppltAvgScores;
    private final ArrayList<Integer> numSpecies;

    public Grapher() {
        numSpecies = new ArrayList<>();
        ppltMaxScores = new ArrayList<>();
        ppltAvgScores = new ArrayList<>();
    }

    public void addPoint(int speciesCount, double maxScore, double avgScore) {
        numSpecies.add(speciesCount);
        ppltMaxScores.add(maxScore);
        ppltAvgScores.add(avgScore);
    }

    public void addScoreSeries(LineChart<Number, Number> chartRef) {

        XYChart.Series<Number, Number> maxSeries = new XYChart.Series<>();
        maxSeries.setName("Max Score");
        XYChart.Series<Number, Number> smoothMaxSeries = new XYChart.Series<>();
        smoothMaxSeries.setName("Mean Max Score");

        makeSeries(ppltMaxScores, maxSeries, smoothMaxSeries);

        XYChart.Series<Number, Number> avgSeries = new XYChart.Series<>();
        avgSeries.setName("Average Score");
        XYChart.Series<Number, Number> smoothAvgSeries = new XYChart.Series<>();
        smoothAvgSeries.setName("Mean Average Score");

        makeSeries(ppltAvgScores, avgSeries, smoothAvgSeries);

        chartRef.getData().addAll(maxSeries, smoothMaxSeries, avgSeries, smoothAvgSeries);

    }

    public void addNumSpeciesSeries(LineChart<Number, Number> chartRef) {

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Number of Species");

        for (int i = 0, n = numSpecies.size(); i < n; ++i)
            series.getData().add(new XYChart.Data<>(i + 1, numSpecies.get(i)));

        chartRef.getData().add(series);

    }

    private void makeSeries(ArrayList<Double> data,
                            XYChart.Series<Number, Number> seriesRef,
                            XYChart.Series<Number, Number> smoothSeriesRef) {

        for (int i = 0, n = data.size(); i < n; ++i) {
            double localValues = data.get(i);
            seriesRef.getData().add(new XYChart.Data<>(i + 1, localValues));
            int count = 1;
            for (int j = 0; j < 10; ++j) {
                int backIndex = i - 10 + j;
                if (backIndex >= 0) {
                    localValues += data.get(backIndex);
                    count++;
                }
                int frontIndex = i + j + 1;
                if (frontIndex < n) {
                    localValues += data.get(frontIndex);
                    count++;
                }
            }
            localValues /= count;
            smoothSeriesRef.getData().add(new XYChart.Data<>(i + 1, localValues));
        }

    }

}
