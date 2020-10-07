package Environment;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Random;

public class WorldDriver extends Application {

	public static char[][][] generateBluePrint(int size, int pits, int golds, boolean randomHLoc, boolean randomWLoc) {

		char[][][] newWorld = new char[size][size][4];
		boolean[][] occupied = new boolean[size][size];
		int x, y;

		Random randGen = new Random();

		for (int i = 0; i < size; ++i) {
			for (int j = 0; j < size; ++j) {
				occupied[i][j] = false;
				for (int k = 0; k < 4; ++k) {
					newWorld[i][j][k] = ' ';
				}
			}
		}

		int hX = 0;
		int hY = 0;
		char hD = 'N';
		if (randomHLoc) {
			hX = randGen.nextInt(size);
			hY = randGen.nextInt(size);
			switch (randGen.nextInt(4)) {
				case 0: hD = 'N'; break;
				case 1: hD = 'E'; break;
				case 2: hD = 'S'; break;
				case 3: hD = 'W'; break;
			}
		}
		occupied[hX][hY] = true;
		newWorld[hX][hY][3] = hD;

		int wX = size-1;
		int wY = size-1;
		if (randomWLoc) {
			wX = randGen.nextInt(size);
			wY = randGen.nextInt(size);
			while (wX == hX && wX == hY) {
				wX = randGen.nextInt(size);
				wY = randGen.nextInt(size);
			}
		}
		occupied[wX][wY] = true;
		newWorld[wX][wY][1] = 'W';

		for (int i = 0; i < pits; ++i) {
			x = randGen.nextInt(size);
			y = randGen.nextInt(size);
			while ((x == hX && y == hY) || occupied[x][y] == true) {
				x = randGen.nextInt(size);
				y = randGen.nextInt(size);
			}
			occupied[x][y] = true;
			newWorld[x][y][0] = 'P';
		}

		for (int i = 0; i < golds; ++i) {
			x = randGen.nextInt(size);
			y = randGen.nextInt(size);
			while ((x == hX && y == hY) || occupied[x][y] == true) {
				x = randGen.nextInt(size);
				y = randGen.nextInt(size);
			}
			occupied[x][y] = true;
			newWorld[x][y][2] = 'G';
		}

		return newWorld;

	}

	/** ================================================================================================================
	 * MAIN
	 * */

	public static void main (String args[]) {
		launch(args);
	}

	@Override
	public void start (Stage stage) throws IOException {

		// login
		Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("view/login.fxml"));
		Scene loginScene = new Scene(root, 700, 300);
		loginScene.getStylesheets().add("css/LoginStyle.css");
		stage.setScene(loginScene);
		stage.setTitle("Wumpus World");
		stage.setResizable(false);
		stage.show();

	}

}
