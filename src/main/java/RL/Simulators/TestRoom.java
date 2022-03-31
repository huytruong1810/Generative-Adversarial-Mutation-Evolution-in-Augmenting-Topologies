package RL.Simulators;

import RL.Environment;
import RL.Human.Human;
import NEAT.Individual;
import RL.Wumpus.Wumpus;
import RL.Wumpus.WumpusActionSet;

import static NEAT.NEAT.actorOI;
import static NEAT.NEAT.obsII;

public class TestRoom extends Simulator {

    private double[] hActorThought, hSeerPred;
    private double hCriticThought, hReceivedReward;

    public TestRoom(int s, int t) { super(s, t); }

    public String getHActorThought() {
        StringBuilder thought = new StringBuilder("[ ");
        for (int i = 0; i < actorOI.length; ++i) thought.append(Math.round(hActorThought[i] * 100.0) / 100.0).append(" ");
        return thought + "]";
    }
    public String getHCriticThought() { return Double.toString(Math.round(hCriticThought * 100.0) / 100.0); }
    public String getHReceiveReward() { return Double.toString(Math.round(hReceivedReward * 100.0) / 100.0); }
    public String getHNextStatePred() {
        StringBuilder pred = new StringBuilder("[ ");
        for (int i = 0; i < obsII.length; ++i) pred.append(Math.round(hSeerPred[i] * 100.0) / 100.0).append(" ");
        return pred + "]";
    }

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
        hTakenAction = human.think();
        hActorThought = human.getActorThought();
        hCriticThought = human.getCriticThought();
        hSeerPred = human.getSeerPred();
        // sample wumpus's action
        wTakenAction = wumpus.dead() ? WumpusActionSet.SELF_TERMINATE : wumpus.think();

        if (environment.humanHearScream()) environment.setScream(false);
        if (environment.thereIsHWall()) environment.setHWall(false);
        if (environment.thereIsWWall()) environment.setWWall(false);

        double hReward = actuateHAction(hTakenAction); // sample reward for human
        double wReward = actuateWAction(wTakenAction); // sample reward for wumpus

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

        // no need to log reward because testing will not update parameters
        hReceivedReward = hReward; // store this for graphical display
        //wumpus.giveReward(wReward);

        // accumulate rewards as scores
        hScore += hReward;
        wScore += wReward;
        // go to next step if all is good
        if (done == 0) stepCounter += 1;
        return done;

    }

}