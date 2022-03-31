package RL.Human;

import NEAT.DataStructures.MemStream;
import NEAT.Individual;

import java.util.ArrayList;

import static NEAT.NEAT.actorOI;

public class Ensemble {

    private static final int[] actionTable = HumanFunction.actionTable;

    private final Individual[] brains;

    // these components are only relevant during a complete simulation
    private int[] choices;
    private double[] votes;
    private final double[] stateValues;
    private final MemStream[] memories;

    public int[] getChoices() { return choices; }
    public double[] getStateValues() { return stateValues; }

    public Ensemble(ArrayList<Individual> individuals) {

        // every individual attribute must align index-wise
        int n = individuals.size();
        // assuming all members have same base architecture
        int hiddenSize = individuals.get(0).getMH().getOutputNum();

        votes = new double[actorOI.length];
        stateValues = new double[n];
        memories = new MemStream[n];
        brains = new Individual[n];
        choices = new int[n];
        for (int i = 0; i < n; ++i) {
            brains[i] = individuals.get(i);
            choices[i] = HumanActionSet.WAKE_UP; // first thing is waking up
            MemStream memory = new MemStream(hiddenSize);
            memories[i] = memory;
        }

    }

    /**
     * Processes the senses and agrees on what action to take between the members
     * IMPORTANT: Each process() is not followed by a logEnvironmentReturn() because
     * no learning will happen
     * @param senses - the sensory system
     * @return final action decision
     */
    public int vote(HumanSensory senses) {
//
//        int n = brains.length;
//        choices = new int[n];
//
//        for (int i = 0; i < n; ++i) { // for each brain
//
//            MHg MRU = brains[i].getMRU();
//            DHg ACU = brains[i].getACU();
//            MemStream memory = memories[i];
//
//            // process state and update memory
//            int t = memory.push(MRU.feed(memory.recent(), senses.produceState(votes, stateValues[i])));
//
//            // collect memory encoding
//            double[] MRUOutput = memory.recent().h();
//            // get current actor's action distribution
//            double[] probs = ACU.feed(t, MRUOutput, true);
//            // get current critic's Q-value
//            stateValues[i] = ACU.feed(t, MRUOutput, false)[0];
//
//            // stochastic action selection
//            votes = new double[numAction];
//            double num = Math.random(), p = 0;
//            for (int j = 0; j < numAction; ++j) { // only first half is result of softmax
//                p += probs[j];
//                if (num < p) { // no need to log action in memory
//                    votes[j]++;
//                    choices[i] = actionTable[j];
//                    break;
//                }
//            }
//
//        }
//
//        // pick action with most votes
//        double max = votes[0];
//        int argmax = 0;
//        for (int i = 1; i < numAction; ++i) {
//            if (votes[i] > max) {
//                max = votes[i];
//                argmax = i;
//            }
//        }
//
//        return actionTable[argmax];

        return 0;

    }

}