package Neat.Genome;

import java.util.ArrayList;

import static Neat.Neat.*;

public class NodeGene extends Gene implements Comparable<NodeGene>, Cloneable {

    private double x, y, output, SMG; // softmax gradient must be set by a driver
    private char actorActivation, criticActivation; // 's' - sigmoid, 'r' - ReLU, 'o' - softmax, 'l' - linear
    private ArrayList<ConGene> inCons;

    public NodeGene(int IN) {
        super(IN);
        inCons = new ArrayList<>();
        actorActivation = criticActivation = ' ';
    }

    public void feed(boolean selectActor) { // for actor, this should not be for output nodes

        if (selectActor && x == outputNodeX) throw new IllegalStateException("Illegal feed.");

        double weightedSum = 0;
        for (ConGene c : inCons) if (c.isEnabled()) weightedSum += c.getFG().getOutput() * c.getWeight(selectActor);

        char fa = selectActor ? actorActivation : criticActivation;
        switch (fa) {
            case 'r': output = Math.max(0, weightedSum); break;
            case 's': output = 1 / (1 + Math.exp(-weightedSum)); break;
            case 'l': output = weightedSum; break;
            default: throw new IllegalStateException("Node has not been assigned Activation Function.");
        }

    }

    public double expWeightedSum() { // only for actor softmax output nodes

        if (x != outputNodeX) throw new IllegalStateException("This is not an output node.");
        if (actorActivation != 'o') throw new IllegalStateException("This is not a softmax node.");

        double weightedSum = 0;
        for (ConGene c : inCons) if (c.isEnabled()) weightedSum += c.getFG().getOutput() * c.getWeight(true);
        output = weightedSum; // cache weighted sum for softmax
        return Math.exp(weightedSum);

    }

    public void backProp(double dL_da, boolean selectActor) {

        char fa = selectActor ? actorActivation : criticActivation;

        for (ConGene c:inCons) {

            if (!c.isEnabled()) continue;

            double da_dz;
            switch (fa) {
                case 'r': da_dz = (output <= 0) ? 0 : 1; break;
                case 's': da_dz = output * (1 - output); break;
                case 'o': da_dz = SMG; break;
                case 'l': da_dz = 1; break;
                default: throw new IllegalStateException("Node has not been assigned Activation Function.");
            }
            double dL_dz = dL_da * da_dz * LR;
            double dL_dw = dL_dz * c.getFG().output;
            double dL_dx = dL_dz * c.getWeight(selectActor);

            c.setWeight(c.getWeight(selectActor) + dL_dw, selectActor);
            c.getFG().backProp(dL_dx, selectActor);

        }

    }

    public boolean equals(Object o) {
        if (!(o instanceof NodeGene))
            return false;
        return IN == ((NodeGene) o).getIN();
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getOutput() { return output; }
    public char getFa(boolean selectActor) { return selectActor ? actorActivation : criticActivation; }
    public void setX(double val) { x = val; }
    public void setY(double val) { y = val; }
    public void setOutput(double val) { output = val; }
    public void setActivation(char activation, boolean selectActor) {
        if (selectActor) actorActivation = activation;
        else criticActivation = activation;
    }
    public void setSMG(double val) { SMG = val; }

    public void addCon(ConGene c) { inCons.add(c); }
    public void removeCon(ConGene c) { inCons.remove(c); }

    @Override
    public int hashCode() { return IN; }

    @Override
    public int compareTo(NodeGene o) {
        if (x > o.x) return -1;
        if (x < o.x) return 1;
        return 0;
    }

    @Override
    public NodeGene clone() {
        NodeGene copy = new NodeGene(IN);
        copy.x = x;
        copy.y = y;
        return copy; // don't copy in connections
    }

    @Override
    public String toString() { return " " + IN; }

}
