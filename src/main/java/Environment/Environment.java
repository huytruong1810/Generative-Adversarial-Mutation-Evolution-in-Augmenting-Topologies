package Environment;

import Human.Human;
import Wumpus.Wumpus;

import java.util.ArrayList;

public class Environment {

	private final int size;
	private final char[][][] map, hPerceptMap, wPerceptMap;

	private int hX, hY, wX, wY;
	private char hD;

	private boolean hWall;
	private boolean wWall;
	private boolean scream;

	private final ArrayList<int[]> pits, golds;

	private final int P = 0, W = 1, G = 2, H = 3;
	private final int B = 0, S = 1, Gl = 2;
	private final int SLT = 0, SD = 1;
	
	public Environment(int s, char[][][] worldBlueprint) {

		size = s;
		map = new char[s][s][4];
		hPerceptMap = new char[s][s][3];
		wPerceptMap = new char[s][s][2];

		hWall = false;
		wWall = false;
		scream = false;

		pits = new ArrayList<>();
		golds = new ArrayList<>();

		for (int i = 0; i < s; ++i) { // clear out the maps
			for (int j = 0; j < s; ++j) {
				System.arraycopy(worldBlueprint[i][j], 0, map[i][j], 0, 4);
				for (int n = 0; n < 3; ++n) hPerceptMap[i][j][n] = ' ';
				wPerceptMap[i][j][SLT] = '0';
				wPerceptMap[i][j][SD] = ' ';
			}
		}
		char[] space;
		for (int i = 0; i < s; ++i) { // fill the maps
			for (int j = 0; j < s; ++j) {
				space = worldBlueprint[i][j];
				if (space[P] == 'P') {
					pits.add(new int[]{i, j});
					hPerceptMap[i][j][B] = 'B';
					if (j-1 >= 0) hPerceptMap[i][j-1][B] = 'B';
					if (i+1 < s) hPerceptMap[i+1][j][B] = 'B';
					if (j+1 < s) hPerceptMap[i][j+1][B] = 'B';
					if (i-1 >= 0) hPerceptMap[i-1][j][B] = 'B';
				}
				if (space[W] == 'W') {
					wX = i;
					wY = j;
					hPerceptMap[i][j][S] = 'S';
					if (wY-1 >= 0) hPerceptMap[i][j-1][S] = 'S';
					if (wX+1 < s) hPerceptMap[i+1][j][S] = 'S';
					if (wY+1 < s) hPerceptMap[i][j+1][S] = 'S';
					if (wX-1 >= 0) hPerceptMap[i-1][j][S] = 'S';
				}
				if (space[G] == 'G') {
					golds.add(new int[]{i, j});
					hPerceptMap[i][j][Gl] = 'G';
				}
				if (space[H] != ' ') {
					hX = i;
					hY = j;
					hD = space[H];
					wPerceptMap[i][j][SLT] = '5';
					wPerceptMap[i][j][SD] = 'X';
				}

			}
		}
				
	}

	public void debug() {
		System.out.println("\n***Debugging maps***");
		System.out.println("Cave layout:");
		for (int i = 0; i < size; ++i) {
			for (int j = 0; j < size; ++j) System.out.print("| "+ map[i][j][0]+" "+ map[i][j][1]+" |");
			System.out.println();
			for (int j = 0; j < size; ++j) System.out.print("| "+ map[i][j][2]+" "+ map[i][j][3]+" |");
			System.out.println("\n-----------------------------------------------------------------------------------");
		}
		System.out.println("\nHuman percept map layout:");
		for (int i = 0; i < size; ++i) {
			for (int j = 0; j < size; ++j) System.out.print("| "+hPerceptMap[i][j][0]+" "+hPerceptMap[i][j][1]+" |");
			System.out.println();
			for (int j = 0; j < size; ++j) System.out.print("| "+hPerceptMap[i][j][2]+"   |");
			System.out.println("\n-----------------------------------------------------------------------------------");
		}
		System.out.println("\nWumpus percept map layout:");
		for (int i = 0; i < size; ++i) {
			for (int j = 0; j < size; ++j) System.out.print("| "+wPerceptMap[i][j][0]+" "+wPerceptMap[i][j][1]+" |");
			System.out.println();
			for (int j = 0; j < size; ++j) System.out.print("|     |");
			System.out.println("\n-----------------------------------------------------------------------------------");
		}
	}
	
	public int getSize() { return size; }
	
	public char getHumanDir() { return hD; }
	public int[] getHumanLoc() { return new int[]{hX, hY}; }
	public int[] getWumpusLoc() { return new int[]{wX, wY}; }
	public ArrayList<int[]> getPits() { return pits; }
	public ArrayList<int[]> getGolds() { return golds; }
	
	public void setHWall(boolean w) { hWall = w; }
	public void setWWall(boolean w) { wWall = w; }
	public boolean thereIsHWall() { return hWall; }
	public boolean thereIsWWall() { return wWall; }
	
	public void setScream(boolean s) { scream = s; }
	public boolean humanHearScream() { return scream; }

	public boolean humanFeelBreeze() { return hPerceptMap[hX][hY][B] == 'B'; }
	public boolean humanSmellStench() { return hPerceptMap[hX][hY][S] == 'S'; }
	public boolean humanSeeGlitter() { return hPerceptMap[hX][hY][Gl] == 'G'; }
	public boolean wumpusSmellScent() { return wPerceptMap[wX][wY][SD] != ' '; }
	public char wumpusSmellIntensityIs() { return wPerceptMap[wX][wY][SLT]; }
	public char wumpusSmellDirectionIs() { return wPerceptMap[wX][wY][SD]; }

	public boolean wumpusIsWithHuman() { return map[hX][hY][W] == 'W'; }
	public boolean humanIsInPit() { return map[hX][hY][P] == 'P'; }

	public void updateHumanInfo(Human h) {

		map[hX][hY][H] = ' '; // remove human from prev location
		wPerceptMap[hX][hY][SD] = hD; // leave a scent there

		hX = h.myLocation()[0];
		hY = h.myLocation()[1];
		hD = h.myDirection();
		map[hX][hY][H] = h.myDirection();
		wPerceptMap[hX][hY][SLT] = '5';
		wPerceptMap[hX][hY][SD] = 'X';

	}

	public void updateWumpusInfo(Wumpus w) {

		map[wX][wY][1] = ' '; // remove wumpus from prev location
		hPerceptMap[wX][wY][S] = ' '; // remove its scents
		if (wY-1 >= 0) hPerceptMap[wX][wY-1][S] = ' ';
		if (wX+1 < size) hPerceptMap[wX+1][wY][S] = ' ';
		if (wY+1 < size) hPerceptMap[wX][wY+1][S] = ' ';
		if (wX-1 >= 0) hPerceptMap[wX-1][wY][S] = ' ';

		wX = w.myLocation()[0];
		wY = w.myLocation()[1];
		map[wX][wY][W] = w.myIcon();
		hPerceptMap[wX][wY][S] = 'S';
		if (wY-1 >= 0) hPerceptMap[wX][wY-1][S] = 'S';
		if (wX+1 < size) hPerceptMap[wX+1][wY][S] = 'S';
		if (wY+1 < size) hPerceptMap[wX][wY+1][S] = 'S';
		if (wX-1 >= 0) hPerceptMap[wX-1][wY][S] = 'S';

	}

	public void regulateHumanScent() {
		for (int i = 0; i < size; ++i) {
			for (int j = 0; j < size; ++j) {
				if (wPerceptMap[i][j][SLT] == '0') wPerceptMap[i][j][SD] = ' ';
				else wPerceptMap[i][j][SLT] -= 1;
			}
		}	
	}

	public boolean goldIsThereToGrab() {
		if (hPerceptMap[hX][hY][Gl] == 'G') {
			hPerceptMap[hX][hY][Gl] = ' ';
			map[hX][hY][G] = ' ';
			return true;
		}
		return false;
	}

	public boolean arrowHit() {

		switch (hD) {
			case 'N':
				for (int i = 0; i < hX - 1; ++i)
					if (wX == i && wY == hY) return true; break;
			case 'E':
				for (int i = hY + 1; i < size; ++i)
					if (wX == hX && wY == i) return true; break;
			case 'S':
				for (int i = hX + 1; i < size; ++i)
					if (wX == i && wY == hY) return true; break;
			case 'W':
				for (int i = 0; i < hY - 1; ++i)
					if (wX == hX && wY == i) return true; break;
		}
		return false;

	}
	
}