package NEAT.Genome.DecisionHead;

import NEAT.Genome.Adam;
import NEAT.Genome.ConGene;
import NEAT.Genome.NodeGene;

public class DHcg extends ConGene {

    private double actorWeight, criticWeight, seerWeight;
    private Adam.Moment actorMoment, criticMoment, seerMoment;

    public DHcg(NodeGene fG, NodeGene tG) {
        super(fG, tG);
        randomWeights(0);
    }

    public DHcg(NodeGene fG, NodeGene tG, char proxy) {
        super(fG, tG, proxy);
    }

    public void randomWeights(double p) {
        // initialize weights independently and uniformly from -1 to 1
        actorWeight = p * actorWeight + (1 - p) * (Math.random() * 2 - 1);
        criticWeight = p * criticWeight + (1 - p) * (Math.random() * 2 - 1);
        seerWeight = p * seerWeight + (1 - p) * (Math.random() * 2 - 1);
        // these moments start at 0 according to Moment constructor
        actorMoment = new Adam.Moment();
        criticMoment = new Adam.Moment();
        seerMoment = new Adam.Moment();
    }

    public double getWeight(char select) {
        switch (select) {
            case 'a': return actorWeight;
            case 'c': return criticWeight;
            case 's': return seerWeight;
            default: throw new IllegalStateException("Unknown select.");
        }
    }

    public void optimizeWeight(double dw, char select) {
        switch (select) {
            case 'a': actorWeight += Adam.optimize(dw, actorMoment); break;
            case 'c': criticWeight += Adam.optimize(dw, criticMoment); break;
            case 's': seerWeight += Adam.optimize(dw, seerMoment); break;
            default: throw new IllegalStateException("Unknown select.");
        }
    }

    public boolean equals(Object o) {
        if (!(o instanceof DHcg)) return false;
        return super.equals(o);
    }

    @Override
    public DHcg redirectClone(NodeGene newFrom, NodeGene newTo) throws CloneNotSupportedException {

        DHcg theClone = new DHcg(newFrom, newTo);

        theClone.IN = IN;
        theClone.enabled = enabled;

        theClone.actorWeight = actorWeight;
        theClone.criticWeight = criticWeight;
        theClone.seerWeight = seerWeight;

        theClone.actorMoment = actorMoment.clone();
        theClone.criticMoment = criticMoment.clone();
        theClone.seerMoment = seerMoment.clone();

        return theClone;

    }

    @Override
    public String inspect() {
        return "\nGene Type: ACU-ConGene " + super.inspect() +
                "\n*Parameters" +
                "\n**Weights" +
                "\n***Actor: " + actorWeight +
                "\n***Critic: " + criticWeight +
                "\n***Seer: " + seerWeight +
                "\n**Moments" +
                "\n***Actor: " + actorMoment.m + "/" + actorMoment.v + "@" + actorMoment.t +
                "\n***Critic: " + criticMoment.m + "/" + criticMoment.v + "@" + criticMoment.t +
                "\n***Seer: " + seerMoment.m + "/" + seerMoment.v + "@" + seerMoment.t;
    }

    @Override
    public String toString() {
        return super.toString() +
                "(a" + (Math.round(actorWeight * 10.0) / 10.0) +
                ",c" + (Math.round(criticWeight * 10.0) / 10.0) +
                ",s" + (Math.round(seerWeight * 10.0) / 10.0) + ")";
    }

}
