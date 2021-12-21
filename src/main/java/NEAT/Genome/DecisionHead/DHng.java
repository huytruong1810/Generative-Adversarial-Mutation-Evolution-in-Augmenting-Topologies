package NEAT.Genome.DecisionHead;

import NEAT.Genome.Adam;
import NEAT.Genome.ConGene;
import NEAT.Genome.NodeGene;
import NEAT.Lambdas.Activations;

import java.util.HashMap;

import static NEAT.Lambdas.Activations.*;
import static NEAT.NEAT.*;

public class DHng extends NodeGene {

    // keep track of time passage for BPTT
    private HashMap<Integer, Double> actorOutputs, criticOutputs, seerOutputs;

    private double actorB, criticB, seerB;
    private Activations.Type actorA, criticA, seerA;
    private Adam.Moment actorMoment, criticMoment, seerMoment;

    public DHng(int IN) {
        super(IN);
        randomBias();
        actorA = Type.LINEAR;
        criticA = Type.LINEAR;
        seerA = Type.LINEAR;
    }

    public DHng(int IN, char proxy) {
        super(IN, proxy);
    }

    public void randomBias() {
        actorB = Math.random() * 2 - 1;
        criticB = Math.random() * 2 - 1;
        seerB = Math.random() * 2 - 1;
        actorMoment = new Adam.Moment();
        criticMoment = new Adam.Moment();
        seerMoment = new Adam.Moment();
    }

    public DHng(NodeGene other) {
        super(other);
    }

    public DHng(NodeGene other, double x, double y) {
        super(other, x, y);
    }

    @Override
    public void episodePrep() {
        if (inCons == null) throw new IllegalStateException("Cannot use proxy.");
        actorOutputs = new HashMap<>();
        criticOutputs = new HashMap<>();
        seerOutputs = new HashMap<>();
        originalWeights = new HashMap<>();
        for (ConGene c : inCons)
            originalWeights.put(c, new double[] {
                    ((DHcg) c).getWeight('a'), // 0
                    ((DHcg) c).getWeight('c'), // 1
                    ((DHcg) c).getWeight('s')  // 2
            });
    }

    @Override
    public void episodeDone() {
        actorOutputs.clear();
        criticOutputs.clear();
        seerOutputs.clear();
        originalWeights.clear();
    }

    public void feed(int t, char select) {

        double weightedSum = weightedSum(t, select);

        double activatedOutput;
        switch (getA(select)) {
            case RELU: activatedOutput = reLU.apply(weightedSum); break;
            case SIGMOID: activatedOutput = sig.apply(weightedSum); break;
            case TANH: activatedOutput = tanh.apply(weightedSum); break;
            case LINEAR: activatedOutput = weightedSum; break;
            default: throw new IllegalStateException("Illegal Activation Function.");
        }

        overrideOutput(t, activatedOutput, select);

    }

    public double weightedSum(int t, char select) {
        // if invoked directly on actor's output node, no output is cached in the node

        if (inCons == null) throw new IllegalStateException("Cannot use proxy.");

        double weightedSum = 0;
        for (ConGene c : inCons) {
            if (c.isEnabled()) {
                DHcg con = (DHcg) c;
                DHng from = (DHng) con.getFG();
                weightedSum += from.getOutput(t, select) * con.getWeight(select);
            }
        }
        switch (select) {
            case 'a': weightedSum += actorB; break;
            case 'c': weightedSum += criticB; break;
            case 's': weightedSum += seerB; break;
            default: throw new IllegalStateException("Unknown select.");
        }

        return weightedSum;

    }

    public void backProp(int t, double dL_da, char select, HashMap<Integer, Double> backGrads) {

        if (x == hiddenNodeX) {
            // accumulate back gradient for MRU
            backGrads.put(IN, backGrads.containsKey(IN) ? (backGrads.get(IN) + dL_da) : dL_da);
            return; // stop when encounter hidden layer (ACU input layer)
        }

        double activatedOutput = getOutput(t, select); // use cached output
        double da_dz;

        switch (getA(select)) {
            case RELU: da_dz = dreLU.apply(activatedOutput); break;
            case SIGMOID: da_dz = dsig.apply(activatedOutput); break;
            case TANH: da_dz = dtanh.apply(activatedOutput); break;
            case LINEAR: da_dz = 1; break;
            default: throw new IllegalStateException("Illegal Activation Function.");
        }

        // dL_dz = dL_da * da_dz
        backPropdL_dz(t, dL_da * da_dz, select, backGrads);

    }

    public void backPropdL_dz(int t, double dL_dz, char select, HashMap<Integer, Double> backGrads) {
        // can be directly invoked on actor's output nodes because we didn't cache the output anyway

        if (inCons == null) throw new IllegalStateException("Cannot use proxy.");

        // dL_db = dL_dz * 1
        // note that biases are changed even when no connections exist yet
        switch (select) {
            case 'a': actorB += Adam.optimize(dL_dz, actorMoment); break;
            case 'c': criticB += Adam.optimize(dL_dz, criticMoment); break;
            case 's': seerB += Adam.optimize(dL_dz, seerMoment); break;
            default: throw new IllegalStateException("Unknown select.");
        }

        int savedIndex;
        switch (select) {
            case 'a': savedIndex = 0; break;
            case 'c': savedIndex = 1; break;
            case 's': savedIndex = 2; break;
            default: throw new IllegalStateException("Unknown select.");
        }
        for (ConGene c : inCons) {
            if (!c.isEnabled()) continue;
            DHcg con = (DHcg) c;
            DHng from = (DHng) con.getFG();
            // dL_dw = dL_dz * dz_dw = dL_dz * x
            con.optimizeWeight(dL_dz * from.getOutput(t, select), select);
            // dL_da_prev = dL_dx = dL_dz * dz_dx = dL_dz * w_old
            from.backProp(t, dL_dz * originalWeights.get(c)[savedIndex], select, backGrads);
        }

    }

    public double getOutput(int t, char select) {
        switch (select) {
            case 'a': return actorOutputs.get(t);
            case 'c': return criticOutputs.get(t);
            case 's': return seerOutputs.get(t);
            default: throw new IllegalStateException("Unknown select.");
        }
    }
    public Activations.Type getA(char select) {
        switch (select) {
            case 'a': return actorA;
            case 'c': return criticA;
            case 's': return seerA;
            default: throw new IllegalStateException("Unknown select.");
        }
    }
    public void overrideOutput(int t, double val, char select) {
        switch (select) {
            case 'a': actorOutputs.put(t, val); break;
            case 'c': criticOutputs.put(t, val); break;
            case 's': seerOutputs.put(t, val); break;
            default: throw new IllegalStateException("Unknown select.");
        }
    }
    public void setActivation(Activations.Type type, char select) {
        switch (select) {
            case 'a': actorA = type; break;
            case 'c': criticA = type; break;
            case 's': seerA = type; break;
            default: throw new IllegalStateException("Unknown select.");
        }
    }

    public boolean equals(Object o) {
        if (!(o instanceof DHng)) return false;
        return super.equals(o);
    }

    @Override
    public String inspect() {
        return "\nGene Type: ACU-NodeGene" + super.inspect() +
                "\n*Parameters" +
                "\n**Biases" +
                "\n***Actor: " + actorB +
                "\n***Critic: " + criticB +
                "\n***Seer: " + seerB +
                "\n**Activations" +
                "\n***Actor: " + actorA +
                "\n***Critic: " + criticA +
                "\n*** Seer: " + seerA +
                "\n**Moments" +
                "\n***Actor: " + actorMoment.m + "/" + actorMoment.v + "@" + actorMoment.t +
                "\n***Critic: " + criticMoment.m + "/" + criticMoment.v + "@" + criticMoment.t +
                "\n***Seer: " + seerMoment.m + "/" + seerMoment.v + "@" + seerMoment.t;
    }

    @Override
    public DHng clone() throws CloneNotSupportedException {

        DHng theClone = new DHng(IN);

        theClone.x = x;
        theClone.y = y;

        theClone.actorB = actorB;
        theClone.criticB = criticB;
        theClone.seerB = seerB;

        theClone.actorA = actorA;
        theClone.criticA = criticA;
        theClone.seerA = seerA;

        theClone.actorMoment = actorMoment.clone();
        theClone.criticMoment = criticMoment.clone();
        theClone.seerMoment = seerMoment.clone();

        return theClone; // don't copy in-connections and temporal outputs

    }

}
