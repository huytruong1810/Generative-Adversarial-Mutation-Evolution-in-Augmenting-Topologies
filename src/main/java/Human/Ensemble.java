package Human;

import NEAT.Genome.ACU.ACUGenome;
import NEAT.DataStructures.MemStack;
import NEAT.DataStructures.MemUnit;
import NEAT.Genome.MRU.MRUGenome;
import NEAT.Individual;

import java.util.ArrayList;

public class Ensemble {

    private final int numPercept, numAction;
    private final int[] actionTable;
    private final Individual[] brains;

    // these components are only relevant during a complete simulation
    private int agreedAction;
    private int[] choices;
    private double[] stateValues;
    private final MemStack[] memories;

    public int[] getChoices() { return choices; }
    public double[] getStateValues() { return stateValues; }

    public Ensemble(ArrayList<Individual> individuals) {

        numPercept = 6;
        numAction = 5;
        actionTable = new int[numAction]; // the available actions
        actionTable[0] = HumanActionSet.MOVE_FORWARD;
        actionTable[1] = HumanActionSet.TURN_RIGHT;
        actionTable[2] = HumanActionSet.TURN_LEFT;
        actionTable[3] = HumanActionSet.GRAB;
        actionTable[4] = HumanActionSet.SHOOT;

        // every individual attribute must align index-wise
        int n = individuals.size();
        // assuming all members have same base architecture
        int hiddenSize = individuals.get(0).getMRU().getOutputNum();

        memories = new MemStack[n];
        brains = new Individual[n];
        choices = new int[n];
        for (int i = 0; i < n; ++i) {
            brains[i] = individuals.get(i);
            choices[i] = HumanActionSet.WAKE_UP; // first thing is waking up
            MemStack memory = new MemStack();
            memory.push(new MemUnit(-1, null,
                    new double[hiddenSize], // initialize hidden state to zero vector
                    new double[hiddenSize], // initialize cell state to zero vector
                    null));
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

        int n = brains.length;
        stateValues = new double[n];
        choices = new int[n];

        int[] votes = new int[numAction];
        for (int i = 0; i < n; ++i) { // for each brain

            MRUGenome MRU = brains[i].getMRU();
            ACUGenome ACU = brains[i].getACU();
            MemStack memory = memories[i];

            // process state and update memory
            memory.push(MRU.feed(memory.peek(), senses.produceState(agreedAction, numPercept)));

            double[] MRUOutput = memory.peek().h();
            // get actor's action opinion on output of MRU
            double[] probs = ACU.feed(MRUOutput, true);
            // get critic's state-value opinion
            stateValues[i] = ACU.feed(MRUOutput, false)[0];

            // stochastic action selection
            double num = Math.random(), p = 0;
            for (int j = 0; j < numAction; ++j) { // only first half is result of softmax
                p += probs[j];
                if (num < p) { // no need to log action in memory
                    votes[j]++;
                    choices[i] = actionTable[j];
                    break;
                }
            }

        }

        // pick action with most votes
        double max = votes[0];
        int argmax = 0;
        for (int i = 1; i < numAction; ++i) {
            if (votes[i] > max) {
                max = votes[i];
                argmax = i;
            }
        }
        agreedAction = actionTable[argmax];

        return agreedAction;

    }

}