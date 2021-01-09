package Environment.Controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

import static Environment.WorldDriver.bootTheme;

public class LoginController {

    @FXML private TextField WorldSizeT;
    @FXML private TextField TrialsT;
    @FXML private TextField TimeStepsT;
    @FXML private TextField PopT;
    @FXML private TextField EpT;

    @FXML public void exit() { Platform.exit(); }

    @FXML public void runWorldSimulation(ActionEvent e) throws IOException {

        bootTheme.play();

        Stage s = (Stage)((Node)e.getSource()).getScene().getWindow();

        int size = Integer.parseInt(WorldSizeT.getText());
        int trial = Integer.parseInt(TrialsT.getText());
        int time = Integer.parseInt(TimeStepsT.getText());
        int pop = Integer.parseInt(PopT.getText());
        int ep = Integer.parseInt(EpT.getText());

        // NEAT culture lab initialization
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("view/NEATLab.fxml"));
        Parent sRoot = loader.load();
        NEATController nc = loader.getController();
        nc.makeNEATScene(s, size, trial, time, pop, ep);
        Scene NEATScene = new Scene(sRoot);
        NEATScene.getStylesheets().add("css/NEATLabStyle.css");
        s.setResizable(true);
        s.setScene(NEATScene);

    }

}
