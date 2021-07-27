package NEAT.Genome.ACU;

import NEAT.Genome.Gene;

import java.util.ArrayList;
import java.util.HashMap;

import static NEAT.NEAT.*;

public class ACUNodeGene extends Gene implements Comparable<ACUNodeGene> {

    private double x, y,
            output,
            SMG, //softmax gradient must be set by a driver and for actor only
            actorB, criticB;
    private char actorActivation, criticActivation; // 's' - sigmoid, 'r' - ReLU, 'o' - softmax, 'l' - linear
    private ArrayList<ACUConGene> inCons;

    public ACUNodeGene(int IN) {
        super(IN);
        inCons = new ArrayList<>();
        actorActivation = criticActivation = ' ';
    }

    public void feed(boolean selectActor) { // in the case of actor, this should not be invoked on output nodes

        if (selectActor && x == outputNodeX) throw new IllegalStateException("Illegal feed.");

        double weightedSum = 0;
        for (ACUConGene c : inCons) if (c.isEnabled()) weightedSum += c.getFG().getOutput() * c.getWeight(selectActor);
        weightedSum += (selectActor ? actorB : criticB); // add node's bias

        char fa = selectActor ? actorActivation : criticActivation;
        switch (fa) { // output is cache for training
            case 'r': output = Math.max(0, weightedSum); break;
            case 's': output = 1 / (1 + Math.exp(-weightedSum)); break;
            case 'l': output = weightedSum; break;
            default: throw new IllegalStateException("Node has not been assigned Activation Function.");
        }

    }

    public double expWeightedSum() { // invoked only on actor's output nodes

        if (x != outputNodeX) throw new IllegalStateException("This is not an output node.");
        if (actorActivation != 'o') throw new IllegalStateException("This is not a softmax node.");

        double weightedSum = 0;
        for (ACUConGene c : inCons) if (c.isEnabled()) weightedSum += c.getFG().getOutput() * c.getWeight(true);
        weightedSum += actorB; // add actor output node's bias

        output = weightedSum; // cache weighted sum for softmax
        return Math.exp(weightedSum);

    }

    public void gradientFlow(double dL_da, boolean selectActor, HashMap<Integer, Double> backGrads, HashMap<Integer, Double> conGrads) {

        // early stop when at the hidden layer
        if (x == hiddenNodeX) {
            int IN = this.IN; // accumulate gradient
            backGrads.put(IN, backGrads.containsKey(IN) ? (backGrads.get(IN) + dL_da) : dL_da);
            return;
        }

        char fa = selectActor ? actorActivation : criticActivation;
        double da_dz;
        switch (fa) { // cached output need to be correct for actor/critic on relevant input
            case 'r': da_dz = (output <= 0) ? 0 : 1; break;
            case 's': da_dz = output * (1 - output); break;
            case 'o': da_dz = SMG; break;
            case 'l': da_dz = 1; break;
            default: throw new IllegalStateException("Node has not been assigned Activation Function.");
        }
        double dL_dz = dL_da * da_dz;

        for (ACUConGene c : inCons) {

            if (!c.isEnabled()) continue;
            double dL_db = dL_dz * ACU_LR;
            double dL_dw = dL_dz * c.getFG().output * ACU_LR;
            double dL_dx = dL_dz * c.getWeight(selectActor);

            // safely update bias as it would not be used throughout back-propagation
            if (selectActor) actorB += dL_db;
            else criticB += dL_db;

            int IN = c.getIN(); // do not adjust weight or nodes at lower level will be trained incorrectly
            conGrads.put(IN, conGrads.containsKey(IN) ? (conGrads.get(IN) + dL_dw) : dL_dw);
            c.getFG().gradientFlow(dL_dx, selectActor, backGrads, conGrads);

        }

    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getOutput() { return output; }
    public char getFa(boolean selectActor) { return selectActor ? actorActivation : criticActivation; }
    public ArrayList<ACUConGene> getInCons() { return inCons; }
    public void setX(double val) { x = val; }
    public void setY(double val) { y = val; }
    public void setOutput(double val) { output = val; }
    public void setSMG(double val) { SMG = val; }
    public void setActivation(char activation, boolean selectActor) {
        if (selectActor) actorActivation = activation;
        else criticActivation = activation;
    }

    public boolean equals(Object o) {
        if (!(o instanceof ACUNodeGene)) return false;
        return IN == ((ACUNodeGene) o).IN;
    }

    @Override
    public String inspect() {
        return "\nGene Type: ACU-NodeGene" +
                "\nInnovation No.: " + IN +
                "\nX: " + x + "\nY: " + y +
                "\n*Parameters" +
                "\n**Biases" +
                "\n***Actor: " + actorB +
                "\n***Critic: " + criticB +
                "\n**Activations" +
                "\n***Actor: " + actorActivation +
                "\n***Critic: " + criticActivation;
    }

    @Override
    public int hashCode() { return IN; }

    @Override
    public int compareTo(ACUNodeGene o) {
        if (x > o.x) return -1;
        if (x < o.x) return 1;
        return 0;
    }

    @Override
    public ACUNodeGene clone() {
        ACUNodeGene theClone = new ACUNodeGene(IN);
        theClone.x = x;
        theClone.y = y;
        theClone.actorB = actorB;
        theClone.criticB = criticB;
        return theClone; // don't copy in-connections
    }

    @Override
    public String toString() { return "(" + IN + ")"; }

}
