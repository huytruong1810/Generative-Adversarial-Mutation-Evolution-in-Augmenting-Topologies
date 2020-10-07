package Wumpus;

import Environment.Environment;

public class Wumpus {

	private int x, y;
	private boolean isDead;

	private int worldSizeKnowledge;
	private WumpusSensory senses;
	private WumpusFunction agentFunc;

	public Wumpus(Environment E) {

		isDead = false;

		worldSizeKnowledge = E.getSize();
		senses = new WumpusSensory(E);

		agentFunc = new WumpusFunction();
		x = E.getWumpusLoc()[0];
		y = E.getWumpusLoc()[1];
		
	}

	public void terminate() {
		isDead = true;
	}
	public boolean dead() {
		return isDead;
	}

	public int[] myLocation() {
		return new int[]{x, y};
	}
	public char myIcon() {
		return 'W';
	}
	public void setLocation(int[] here) {
		x = here[0]; y = here[1];
	}

	public int think() {
		return agentFunc.process(senses);
	}

	public boolean moveRight() {
		if (y + 1 < worldSizeKnowledge) { ++y; return true; }
		return false;
	}

	public boolean moveLeft() {
		if (y - 1 >= 0) { --y; return true; }
		return false;
	}

	public boolean moveUp() {
		if (x - 1 >= 0) { --x; return true; }
		return false;
	}

	public boolean moveDown() {
		if (x + 1 < worldSizeKnowledge) { ++x; return true; }
		return false;
	}

	
}