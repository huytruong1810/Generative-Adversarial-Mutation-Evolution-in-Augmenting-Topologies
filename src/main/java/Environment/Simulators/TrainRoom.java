package Environment.Simulators;

import Environment.Environment;
import Human.Human;
import NEAT.Individual;
import Wumpus.Wumpus;
import Wumpus.WumpusActionSet;

import static Environment.RewardConst.*;

public class TrainRoom extends Simulator {

	public TrainRoom(int s, int t) { super(s, t); }

	@Override
	public void resetModel(char[][][] bp, Individual h, Individual w) {

		environment = new Environment(worldSize, bp);
		human = new Human(environment, h, false);
		wumpus = new Wumpus(environment);

	}

	@Override
	public int step() {

		int done = 0; // initially assume everything is good

		if (stepCounter == timeSteps) {
			human.terminate();
			wumpus.terminate();
			done = -2; // out of time
		}

		// sample human's action
		hTakenAction = human.think(); // if time, push last memory unit on for final reward log
		// sample wumpus's action
		wTakenAction = wumpus.dead() ? WumpusActionSet.SELF_TERMINATE : wumpus.think();

		if (environment.humanHearScream()) environment.setScream(false);
		if (environment.thereIsHWall()) environment.setHWall(false);
		if (environment.thereIsWWall()) environment.setWWall(false);

		int hReward = actuateHAction(hTakenAction); // sample reward for human
		int wReward = actuateWAction(wTakenAction); // sample reward for wumpus

		hReward += (done == -2) ? deathCost : 0; // apply time cost if necessary
		wReward += (done == -2) ? deathCost : 0;

		if (done != -2) { // update environment if not time

			environment.regulateHumanScent();
			environment.updateHumanInfo(human);
			environment.updateWumpusInfo(wumpus);

			if (environment.humanIsInPit()) {
				hReward += deathCost;
				done = -1; // human died
			}
			if (environment.wumpusIsWithHuman() && !wumpus.dead()) {
				hReward += deathCost;
				wReward += killReward;
				done = 2; // wumpus won
			}
			if (human.haveGold()) {
				hReward += goldReward;
				wReward += lostCost;
				done = 1; // human won
			}

		}

		human.giveReward(hReward);
		//wumpus.giveReward(wReward);

		// accumulate rewards as scores
		hScore += hReward;
		wScore += wReward;
		// go to next step if all is good
		if (done == 0) stepCounter += 1;
		else { // let agents learn at the end of the episode
			human.learn(); // learns from the completed trajectory
			//wumpus.learn();
		}
		return done;

	}

}