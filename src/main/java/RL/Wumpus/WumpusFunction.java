package RL.Wumpus;

import java.util.Random;

public class WumpusFunction {

	private int[] actionTable;
	private Random rand;

	public WumpusFunction() {

		actionTable = new int[4];
		actionTable[0] = WumpusActionSet.MOVE_RIGHT;
		actionTable[1] = WumpusActionSet.MOVE_LEFT;
		actionTable[2] = WumpusActionSet.MOVE_UP;
		actionTable[3] = WumpusActionSet.MOVE_DOWN;
		rand = new Random();

	}
	
	// map from the perceived data to the action that needs to be taken
	public int process(WumpusSensory senses) {
	    return actionTable[rand.nextInt(4)];
	}
	
}