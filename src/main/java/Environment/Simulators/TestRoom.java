package Environment.Simulators;

import Environment.Environment;
import Human.Human;
import Human.HumanActionSet;
import NEAT.Individual;
import Wumpus.Wumpus;
import Wumpus.WumpusActionSet;

import static Environment.RewardSystem.*;

public class TestRoom extends Simulator {

    private double[] hActorThought;
    private double hCriticThought, hTDTarget;

    private double[] wActorThought;
    private double wCriticThought, wTDTarget;

    public TestRoom(int s, int t) { super(s, t); }

    public String getHActorThought() {
        StringBuilder thought = new StringBuilder("[ ");
        for (int i = 0; i < 5; ++i) thought.append(Math.round(hActorThought[i] * 100.0) / 100.0).append(" ");
        return thought + "]";
    }
    public String getHCriticThought() { return Double.toString(Math.round(hCriticThought * 100.0) / 100.0); }
    public String getHTDTarget() { return Double.toString(Math.round(hTDTarget * 100.0) / 100.0); }

    public String getWActorThought() {
        StringBuilder thought = new StringBuilder("[ ");
        for (int i = 0; i < 5; ++i) thought.append(Math.round(wActorThought[i] * 100.0) / 100.0).append(" ");
        return thought + "]";
    }
    public String getWCriticThought() { return Double.toString(Math.round(wCriticThought * 100.0) / 100.0); }
    public String getWTDTarget() { return Double.toString(Math.round(wTDTarget * 100.0) / 100.0); }

    @Override
    public void resetModel(char[][][] bp, Individual h, Individual w) {

        environment = new Environment(worldSize, bp);
        human = new Human(environment, h, true);
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
        hTakenAction = human.dead() ? HumanActionSet.SELF_TERMINATE : human.think();
        hActorThought = human.getActorThought();
        hCriticThought = human.getCriticThought();
        // sample wumpus's action
        wTakenAction = wumpus.dead() ? WumpusActionSet.SELF_TERMINATE : wumpus.think();

        if (environment.humanHearScream()) environment.setScream(false);
        if (environment.thereIsHWall()) environment.setHWall(false);
        if (environment.thereIsWWall()) environment.setWWall(false);

        int hReward = actuateHAction(hTakenAction); // sample reward for human
        int wReward = actuateWAction(wTakenAction); // sample reward for wumpus

        hReward += (done == -2) ? timeCost : 0; // apply time cost if necessary
        wReward += (done == -2) ? timeCost : 0;

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

        human.giveEnvironmentReturn(hReward); // for human to compute TD target
        hTDTarget = human.getTDTarget(); // cache bootstrapped target state-value
        //wumpus.giveReward(wReward);

        // accumulate rewards as scores
        hScore += hReward;
        wScore += wReward;
        // go to next step if all is good
        if (done == 0) stepCounter += 1;
        return done;

    }

}