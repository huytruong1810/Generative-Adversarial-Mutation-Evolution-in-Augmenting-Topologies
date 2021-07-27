package Environment.Simulators;

import Environment.Controllers.Utils;
import Environment.Environment;
import Human.Ensemble;
import Human.Human;
import Human.HumanActionSet;
import NEAT.Individual;
import Wumpus.Wumpus;
import Wumpus.WumpusActionSet;

import java.util.ArrayList;

import static Environment.RewardSystem.*;

public class EnsembleRoom extends Simulator {

    Ensemble humanEnsemble;
    ArrayList<Individual> humanMembers;
    ArrayList<Individual> wumpusMembers;

    public EnsembleRoom(int s, int t) {
        super(s, t);
        factoryReset();
    }

    public void factoryReset() {
        humanMembers = new ArrayList<>();
        wumpusMembers = new ArrayList<>();
    }

    public String getHChoices() {
        StringBuilder str = new StringBuilder();
        for (Integer choice : humanEnsemble.getChoices()) str.append(HumanActionSet.getName(choice)).append(" ");
        return str.toString();
    }
    public String getHStateValues() {
        StringBuilder str = new StringBuilder();
        for (Double V_s : humanEnsemble.getStateValues()) str.append(V_s).append(" ");
        return str.toString();
    }

    @Override
    public void resetModel(char[][][] bp, Individual h, Individual w) {
        humanMembers.add(h);
        wumpusMembers.add(w);
    }

    /**
     * IMPORTANT: Must be invoked when all ensembles are finalized,
     * step() should only be invoked after this method returns
     */
    public void completeSetup(char[][][] bp) {

        environment = new Environment(worldSize, bp);
        humanEnsemble = new Ensemble(humanMembers);
        // let human be the fittest of all members
        human = new Human(environment, Utils.getFittest(humanMembers), true);
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
        hTakenAction = human.dead() ? HumanActionSet.SELF_TERMINATE : humanEnsemble.vote(human.getSenses());
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

        // accumulate rewards as scores
        hScore += hReward;
        wScore += wReward;
        // go to next step if all is good
        if (done == 0) stepCounter += 1;
        return done;

    }

}
