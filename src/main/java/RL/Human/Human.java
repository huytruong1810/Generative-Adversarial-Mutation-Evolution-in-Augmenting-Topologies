package RL.Human;

import RL.Environment;
import NEAT.Individual;

public class Human {

	private int x, y;
	private char direction;
	private int numArrows;
	private boolean isDead, gotGold;

	private final int worldSizeKnowledge;
	private final HumanFunction agentFunc;

	public Human(Environment environment, Individual brainGenome, boolean beingTested) {

		numArrows = 10;
		isDead = false;
		gotGold = false;

		worldSizeKnowledge = environment.getSize();
		agentFunc = new HumanFunction(brainGenome, new HumanSensory(environment), beingTested);

		x = environment.getHumanLoc()[0];
		y = environment.getHumanLoc()[1];
		direction = environment.getHumanDir();
		
	}

	public void terminate() { isDead = true; }
	public boolean dead() { return isDead; }

	public void takeGold() { gotGold = true; }
	public boolean haveGold() { return gotGold; }

	public int[] myLocation() { return new int[]{x, y}; }
	public char myDirection() { return direction; }

	public int think() { return agentFunc.process(isDead); }
	public void giveReward(double reward) { agentFunc.logReward(reward); }
	public void learn() { agentFunc.adjust(); }

	public double[] getActorThought() { return agentFunc.getProbs(); }
	public double getCriticThought() { return agentFunc.getStateValue(); }
	public double[] getSeerPred() { return agentFunc.getNextStatePred(); }

	public boolean shoot() {
		if (numArrows > 0) { --numArrows; return true; }
		return false;
	}

	public boolean moveForward() {
		switch (direction) {
			case 'N':
				if (x - 1 >= 0) { x--; return true; } break;
			case 'S':
				if (x + 1 < worldSizeKnowledge) { x++; return true; } break;
			case 'E':
				if (y + 1 < worldSizeKnowledge) { y++; return true; } break;
			case 'W':
				if (y - 1 >= 0) { y--; return true; } break;
		}
		return false;
	}

	public void turnLeft() {
		switch (direction) {
			case 'N': direction = 'W'; break;
			case 'S': direction = 'E'; break;
			case 'E': direction = 'N'; break;
			case 'W': direction = 'S'; break;
		}
	}

	public void turnRight() {
		switch (direction) {
			case 'N': direction = 'E'; break;
			case 'S': direction = 'W'; break;
			case 'E': direction = 'S'; break;
			case 'W': direction = 'N'; break;
		}
	}
	
}