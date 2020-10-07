package Environment;

import Human.*;
import Wumpus.*;

public class Simulation {

	// reward for both
	private final int bumpCost = -1;
	private final int actionCost = -1;

	// reward for human
	private final int goldReward = 1000;
	private final int deathCost = -1000;
	private final int shootCost = -10;
	private int hScore = 0;
	private int hTakenAction = 0;

	// reward for wumpus
	private final int killReward = 1000;
	private final int lostCost = -1000;
	private int wScore = 0;
	private int wTakenAction = 0;

	// agent attributes
	private Human human;
	private Wumpus wumpus;

	// simulation attributes
	private int timeSteps;
	private int stepCounter;
	private Environment environment;

	public Simulation(Environment E, int time) {

		stepCounter = 1;
		environment = E;
		timeSteps = time;
		human = new Human(environment);
		wumpus = new Wumpus(environment);

	}

	public int getHScore() { return hScore; }
	public int getWScore() { return wScore; }
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
	 * @return
	 * -2: game ends without any agent winning
	 * -1: agent dies
	 * 0: everything is good
	 * 1: agent wins
	 * 2: wumpus wins
	 */
	public int runTimeStep() {

		if (stepCounter == timeSteps) {
			human.terminate();
			wumpus.terminate();
			hTakenAction = HumanActionSet.SELF_TERMINATE;
			wTakenAction = WumpusActionSet.SELF_TERMINATE;
			return -2;
		}

		hTakenAction = human.think();
		if (wumpus.dead()) wTakenAction = WumpusActionSet.NO_OP;
		else wTakenAction = wumpus.think();

		if (environment.thereIsScream()) environment.setScream(false);
		if (environment.thereIsHWall()) environment.setHWall(false);
		if (environment.thereIsWWall()) environment.setWWall(false);

		actuateHumanAction(hTakenAction);
		actuateWumpusAction(wTakenAction);

		environment.regulateHumanScent();
		environment.updateHumanInfo(human);
		environment.updateWumpusInfo(wumpus);

		if (environment.humanInPit()) {
			hScore += deathCost;
			return -1;
		}
		if (environment.wumpusWithHuman() && !wumpus.dead()) {
			hScore += deathCost;
			wScore += killReward;
			return 2;
		}
		if (human.haveGold()) {
			hScore += goldReward;
			wScore += lostCost;
			return 1;
		}

		// none of ending condition is met so increment step
		stepCounter += 1;
		return 0;

	}

	public void actuateHumanAction(int action) {

		if (action == HumanActionSet.MOVE_FORWARD) {
			hScore += actionCost;
			if (human.moveForward() == false) {
				environment.setHWall(true);
				hScore += bumpCost;
			}
		}
		else if (action == HumanActionSet.TURN_RIGHT) {
			hScore += actionCost;
			human.turnRight();
		}
		else if (action == HumanActionSet.TURN_LEFT) {
			hScore += actionCost;
			human.turnLeft();
		}
		else if (action == HumanActionSet.GRAB) {
			hScore += actionCost;
			if (environment.grabGold() == true)
				human.takeGold();
		}
		else if (action == HumanActionSet.SHOOT) {
			hScore += actionCost;
			if (human.shoot() == true) {
				hScore += shootCost;
				if (environment.arrowHit()) {
					environment.setScream(true);
					wumpus.terminate();
				}
			}
		}

	}

	public void actuateWumpusAction(int action) {

		if (action == WumpusActionSet.MOVE_RIGHT) {
			wScore += actionCost;
			if (wumpus.moveRight() == false) {
				environment.setWWall(true);
				wScore += bumpCost;
			}
		}
		else if (action == WumpusActionSet.MOVE_LEFT) {
			wScore += actionCost;
			if (wumpus.moveLeft() == false) {
				environment.setWWall(true);
				wScore += bumpCost;
			}
		}
		else if (action == WumpusActionSet.MOVE_UP) {
			wScore += actionCost;
			if (wumpus.moveUp() == false) {
				environment.setWWall(true);
				wScore += bumpCost;
			}
		}
		else if (action == WumpusActionSet.MOVE_DOWN) {
			wScore += actionCost;
			if (wumpus.moveDown() == false) {
				environment.setWWall(true);
				wScore += bumpCost;
			}
		}

	}

}