package Environment;

import java.util.Random;

public class Maze {

    public int size;
    public boolean[][] north, east, south, west;
    public boolean[][] visited;
    public Random rand = new Random();

    public Maze(int n) {
        size = n;
        north = new boolean[n+2][n+2];
        east  = new boolean[n+2][n+2];
        south = new boolean[n+2][n+2];
        west  = new boolean[n+2][n+2];
        visited = new boolean[n+2][n+2];
        for (int i = 0; i < n+2; ++i) {
            visited[i][0] = true;
            visited[i][n+1] = true;
            visited[0][i] = true;
            visited[size+1][i] = true;
        }
        for (int i = 0; i < n+2; ++i) {
            for (int j = 0; j < n+2; ++j) {
                north[i][j] = true;
                east[i][j]  = true;
                south[i][j] = true;
                west[i][j]  = true;
            }
        }
    }

    public void generateAt(int x, int y) {
        punch(x, y);
    }

    private void punch(int x, int y) {
        visited[x][y] = true;
        while (!visited[x][y+1] || !visited[x+1][y] || !visited[x][y-1] || !visited[x-1][y]) {
            boolean ret = false;
            while (!ret) {
                switch (rand.nextInt(4)) {
                    case 0:
                        if (!visited[x][y+1]) {
                            north[x][y] = false;
                            south[x][y + 1] = false;
                            punch(x, y + 1);
                            ret = true;
                        } break;
                    case 1:
                        if (!visited[x-1][y]) {
                            west[x][y] = false;
                            east[x - 1][y] = false;
                            punch(x - 1, y);
                            ret = true;
                        } break;
                    case 2:
                        if (!visited[x][y-1]) {
                            south[x][y] = false;
                            north[x][y - 1] = false;
                            punch(x, y - 1);
                            ret = true;
                        } break;
                    case 3:
                        if (!visited[x+1][y]) {
                            east[x][y] = false;
                            west[x + 1][y] = false;
                            punch(x + 1, y);
                            ret = true;
                        } break;
                }
            }
        }
    }

}
