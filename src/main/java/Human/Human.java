package Human;

import Environment.Environment;
import NEAT.Individual;

public class Human {

	private int x, y;
	private char direction;
	private int numArrows;
	private boolean isDead, gotGold;

	private final int worldSizeKnowledge;
	private final HumanSensory senses;
	private final HumanFunction agentFunc;

	public Human(Environment E, Individual i, boolean beingTested) {

		numArrows = 1;
		isDead = false;
		gotGold = false;

		worldSizeKnowledge = E.getSize();
		senses = new HumanSensory(E);
		agentFunc = new HumanFunction(i, beingTested);

		x = E.getHumanLoc()[0];
		y = E.getHumanLoc()[1];
		direction = E.getHumanDir();
		
	}

	public void terminate() { isDead = true; }
	public boolean dead() { return isDead; }

	public void takeGold() { gotGold = true; }
	public boolean haveGold() { return gotGold; }

	public int[] myLocation() { return new int[]{x, y}; }
	public char myDirection() { return direction; }
	public void setDirection(char d) { direction = d; }
	public void setLocation(int[] here) { x = here[0]; y = here[1]; }

	public int think() { return agentFunc.process(senses); }
	public void giveEnvironmentReturn(int reward) { agentFunc.logEnvironmentReturn(reward, senses); }
	public void learn() { agentFunc.adjust(); }

	public HumanSensory getSenses() { return senses; }
	public double[] getActorThought() { return agentFunc.getProbs(); }
	public double getCriticThought() { return agentFunc.getStateValue(); }
	public double getTDTarget() { return agentFunc.getTDTarget(); }

	public boolean shoot() {
		if (numArrows > 0) { numArrows--; return true; }
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