package Environment;

import Human.*;
import Neat.Genome.Genome;
import Wumpus.*;

import static Environment.RewardSystem.*;

public class Simulation {

	// human attributes
	private Human human;
	private int hScore;
	private int hTakenAction;
	private double[] hActorThought;
	private double hCriticThought, hTDTarget;

	// wumpus attributes
	private Wumpus wumpus;
	private int wScore;
	private int wTakenAction;

	// simulation attributes
	private int timeSteps, stepCounter, worldSize;
	private Environment environment;

	public Simulation(int s, int t) {
		worldSize = s;
		timeSteps = t;
	}

	public String getHActorThought() {
		String thought = "[ ";
		for (int i = 0; i < 5; ++i) thought += (Math.round(hActorThought[i] * 100.0) / 100.0) + " ";
		return thought + "]";
	}
	public String getHCriticThought() { return Double.toString(Math.round(hCriticThought * 100.0) / 100.0); }
	public String getHTDTarget() { return Double.toString(Math.round(hTDTarget * 100.0) / 100.0); }
	public int getHScore() { return hScore; }

	public String getWActorThought() { return null; }
	public String getWCriticThought() { return null; }
	public String getWTDTarget() { return null; }
	public int getWScore() { return wScore; }

	public int getTimeSteps() { return timeSteps; }
	public int getStepCounter() { return stepCounter; }

	public Environment getEnvironment() { return environment; }

	public String getHAction() {
		switch (hTakenAction) {
			case HumanActionSet.WAKE_UP: return "WAKE_UP";
			case HumanActionSet.MOVE_FORWARD: return "MOVE_FORWARD";
			case HumanActionSet.TURN_RIGHT: return "TURN_RIGHT";
			case HumanActionSet.TURN_LEFT: return "TURN_LEFT";
			case HumanActionSet.GRAB: return "GRAB";
			case HumanActionSet.SHOOT: return "SHOOT";
			case HumanActionSet.NO_OP: return "NO_OP";
			case HumanActionSet.SELF_TERMINATE: return "SELF_TERMINATE";
		}
		return "@";
	}
	public String getWAction() {
		switch (wTakenAction) {
			case WumpusActionSet.WAKE_UP: return "WAKE_UP";
			case WumpusActionSet.MOVE_RIGHT: return "MOVE_RIGHT";
			case WumpusActionSet.MOVE_LEFT: return "MOVE_LEFT";
			case WumpusActionSet.MOVE_UP: return "MOVE_UP";
			case WumpusActionSet.MOVE_DOWN: return "MOVE_DOWN";
			case WumpusActionSet.NO_OP: return "NO_OP";
			case WumpusActionSet.SELF_TERMINATE: return "SELF_TERMINATE";
		}
		return "@";
	}

	/**
	 * reset the simulation
	 * @param bp - the blueprint for the env
	 * @param hg - the human genome
	 * @param wg - the wumpus genome
	 */
	public void reset(char[][][] bp, Genome hg, Genome wg) {

		stepCounter = 1;
		hScore = hTakenAction = wScore = wTakenAction = 0;

		environment = new Environment(worldSize, bp);
		human = new Human(environment, hg);
		wumpus = new Wumpus(environment);

	}

	/**
	 * @return integer indicating the result of a step:
	 * -2: time's up
	 * -1: human dies
	 * 0: everything is good
	 * 1: human wins
	 * 2: wumpus wins
	 */
	public int step() {

		int done = 0; // initially assume everything is good

		if (stepCounter == timeSteps) {
			human.terminate();
			wumpus.terminate();
			hTakenAction = HumanActionSet.SELF_TERMINATE;
			wTakenAction = WumpusActionSet.SELF_TERMINATE;
			done = -2; // out of time
		}

		hTakenAction = human.think(); // sample human's action
		hActorThought = human.getActorThought(); // cache actor's opinion

		wTakenAction = wumpus.dead() ? WumpusActionSet.NO_OP : wumpus.think(); // sample wumpus's action

		if (environment.thereIsScream()) environment.setScream(false);
		if (environment.thereIsHWall()) environment.setHWall(false);
		if (environment.thereIsWWall()) environment.setWWall(false);

		int hReward = actuateHAction(hTakenAction); // sample reward for human
		int wReward = actuateWAction(wTakenAction); // sample reward for wumpus

		environment.regulateHumanScent();
		environment.updateHumanInfo(human);
		environment.updateWumpusInfo(wumpus);

		if (environment.humanInPit()) {
			hReward += deathCost;
			done = -1; // human died
		}
		if (environment.wumpusWithHuman() && !wumpus.dead()) {
			hReward += deathCost;
			wReward += killReward;
			done = 2; // wumpus won
		}
		if (human.haveGold()) {
			hReward += goldReward;
			wReward += lostCost;
			done = 1; // human won
		}

		human.learn(hReward, hTakenAction); // update human
		hCriticThought = human.getCriticThought(); // cache critic's opinion
		hTDTarget = human.getTDTarget(); // cache bootstrapped target state-value

		// update wumpus

		// accumulate rewards as scores
		hScore += hReward;
		wScore += wReward;
		// go to next step if all is good
		if (done == 0) stepCounter += 1;
		return done;

	}

	private int actuateHAction(int action) {

		int reward = 0;
		switch (action) {
			case HumanActionSet.MOVE_FORWARD: reward += actionCost;
				if (!human.moveForward()) { reward += bumpCost;
					environment.setHWall(true);
				}
				break;
			case HumanActionSet.TURN_RIGHT: reward += actionCost;
				human.turnRight();
				break;
			case HumanActionSet.TURN_LEFT: reward += actionCost;
				human.turnLeft();
				break;
			case HumanActionSet.GRAB: reward += actionCost;
				if (environment.grabGold()) human.takeGold();
				break;
			case HumanActionSet.SHOOT: reward += actionCost;
				if (human.shoot()) { reward += shootCost;
					if (environment.arrowHit()) {
						environment.setScream(true);
						wumpus.terminate();
					}
				}
				break;
		}
		return reward;

	}

	private int actuateWAction(int action) {

		int reward = 0;
		boolean bumped = false;
		switch (action) {
			case WumpusActionSet.MOVE_RIGHT: reward += actionCost;
				if (!wumpus.moveRight()) bumped = true;
				break;
			case WumpusActionSet.MOVE_LEFT: reward += actionCost;
				if (!wumpus.moveLeft()) bumped = true;
				break;
			case WumpusActionSet.MOVE_UP: reward += actionCost;
				if (!wumpus.moveUp()) bumped = true;
				break;
			case WumpusActionSet.MOVE_DOWN: reward += actionCost;
				if (!wumpus.moveDown()) bumped = true;
				break;
		}
		if (bumped) { reward += bumpCost;
			environment.setWWall(true);
		}
		return reward;

	}

}