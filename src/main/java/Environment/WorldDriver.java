package Environment;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;

public class WorldDriver extends Application {

	public static MediaPlayer bootTheme = new MediaPlayer(new Media(new File("src/main/resources/sounds/boot.m4a").toURI().toString()));
	public static MediaPlayer labTheme = new MediaPlayer(new Media(new File("src/main/resources/sounds/labTheme.m4a").toURI().toString()));
	public static MediaPlayer simTheme = new MediaPlayer(new Media(new File("src/main/resources/sounds/simTheme.m4a").toURI().toString()));

	/** ================================================================================================================
	 * MAIN
	 * */

	public static void main (String args[]) {
		launch(args);
	}

	@Override
	public void start (Stage stage) throws IOException {

		// audio theme set up
		bootTheme.setOnEndOfMedia(() -> labTheme.play());
		labTheme.setOnEndOfMedia(() -> labTheme.seek(Duration.ZERO));
		simTheme.setOnEndOfMedia(() -> simTheme.seek(Duration.ZERO));
		labTheme.setVolume(0.3);
		simTheme.setVolume(0.2);

		// login window initialization
		Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("view/login.fxml"));
		Scene loginScene = new Scene(root, 750, 250);
		loginScene.getStylesheets().add("css/LoginStyle.css");
		stage.setTitle("NEAT Lab");
		stage.getIcons().add(new Image("images/lab.png"));
		stage.setScene(loginScene);
		stage.setResizable(false);
		stage.show();

	}

}
