package Environment;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML private TextField WorldSizeT;
    @FXML private TextField TrialsT;
    @FXML private TextField TimeStepsT;

    @FXML public void runWorldSimulation(ActionEvent e) throws IOException {

        Stage s = (Stage)((Node)e.getSource()).getScene().getWindow();

        int size = Integer.parseInt(WorldSizeT.getText());
        int trial = Integer.parseInt(TrialsT.getText());
        int time = Integer.parseInt(TimeStepsT.getText());

        // simulation
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("view/simulation.fxml"));
        Parent sRoot = loader.load();
        SimulationController sc = loader.getController();
        sc.makeSimulationScene(size, trial, time);
        Scene simulationScene = new Scene(sRoot);
        simulationScene.getStylesheets().add("css/SimulationStyle.css");
        s.setScene(simulationScene);
        s.setFullScreen(true);
        s.setResizable(true);

    }

}
