package RL.Controllers;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Objects;


public class Main extends Application {

	/** ================================================================================================================
	 * MAIN
	 **/

	public static void main (String[] args) { launch(args); }

	@Override
	public void start (Stage stage) throws IOException {

		// check if database server is online
		Utils.pingDatabaseServer();

		// login window
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
		Parent sRoot = loader.load();
		LoginController lc = loader.getController();
		lc.makeLoginScene(stage);
		Scene loginScene = new Scene(sRoot);
		loginScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/LoginStyle.css")).toExternalForm());
		stage.setTitle("NEAT Lab");
		stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/images/lab.png")).toExternalForm()));
		stage.setResizable(false);
		stage.setScene(loginScene);
		stage.initStyle(StageStyle.TRANSPARENT);
		stage.show();

	}

}
