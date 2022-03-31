package NEAT.Genome.MemoryHead;

import NEAT.DataStructures.GateTuple;
import NEAT.Genome.ConGene;
import NEAT.Genome.NodeGene;

public class MHcg extends ConGene {

    private GateTuple actorWeightTuple;
    private GateTuple criticWeightTuple;
    private GateTuple seerWeightTuple;

    public MHcg(NodeGene fG, NodeGene tG) {
        super(fG, tG);
        randomWeights(0); // new random parameters
    }

    public void randomWeights(double p) {
        actorWeightTuple = new GateTuple(actorWeightTuple, p);
        criticWeightTuple = new GateTuple(criticWeightTuple, p);
        seerWeightTuple = new GateTuple(seerWeightTuple, p);
    }

    public MHcg(NodeGene fG, NodeGene tG, char proxy) {
        super(fG, tG, proxy);
    }

    public double getWeight(char selectGate, char select) {
        switch (select) {
            case 'a': return actorWeightTuple.getVal(selectGate);
            case 'c': return criticWeightTuple.getVal(selectGate);
            case 's': return seerWeightTuple.getVal(selectGate);
            default: throw new IllegalStateException("Unknown select.");
        }
    }

    public void optimizeACWeights(double af_dw, double ai_dw, double ac_dw, double ao_dw,
                                  double cf_dw, double ci_dw, double cc_dw, double co_dw,
                                  double sf_dw, double si_dw, double sc_dw, double so_dw) {
        actorWeightTuple.optimizeValues(af_dw, ai_dw, ac_dw, ao_dw);
        criticWeightTuple.optimizeValues(cf_dw, ci_dw, cc_dw, co_dw);
        seerWeightTuple.optimizeValues(sf_dw, si_dw, sc_dw, so_dw);
    }

    public boolean equals(Object o) {
        if (!(o instanceof MHcg)) return false;
        return super.equals(o);
    }

    @Override
    public MHcg redirectClone(NodeGene newFrom, NodeGene newTo) throws CloneNotSupportedException {

        MHcg theClone = new MHcg(newFrom, newTo);

        theClone.IN = IN;
        theClone.enabled = enabled;

        theClone.actorWeightTuple = actorWeightTuple.clone();
        theClone.criticWeightTuple = criticWeightTuple.clone();
        theClone.seerWeightTuple = seerWeightTuple.clone();

        return theClone;

    }

    @Override
    public String inspect() {
        return "\nGene Type: MRU-ConGene " + super.inspect() +
                "\nInnovation No.: " + IN +
                "\nEnabled: " + ((enabled) ? "YES" : "NO") +
                "\n*Parameters" +
                "\n**Weights:" +
                "\nActor:" + actorWeightTuple.inspect() +
                "\nCritic:" + criticWeightTuple.inspect() +
                "\nSeer:" + seerWeightTuple.inspect();
    }

    @Override
    public String toString() {
        return super.toString() +
                actorWeightTuple.toString() +
                criticWeightTuple.toString() +
                seerWeightTuple.toString();
    }

}
