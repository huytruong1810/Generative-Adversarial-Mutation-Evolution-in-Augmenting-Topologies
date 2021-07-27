package NEAT.Genome.MRU;

import NEAT.Genome.Gene;
import NEAT.NEAT;

public class MRUConGene extends Gene {

    private final MRUNodeGene fG, tG;
    // 'f' - forget gate, 'i' - input gate, 'c' - candidate gate, 'o' - output gate
    private double forgetWeight, inputWeight, candidateWeight, outputWeight;
    private boolean enabled;

    public MRUConGene(MRUNodeGene fG, MRUNodeGene tG) {
        if (fG.getX() >= tG.getX()) throw new IllegalStateException("From-gene is after to-gene.");
        this.fG = fG;
        this.tG = tG; // this constructor do not add connection to node tG
        enabled = true;
    }

    public MRUNodeGene getFG() { return fG; }
    public MRUNodeGene getTG() { return tG; }

    public void setWeight(double w, char selectGate) {
        switch (selectGate) {
            case 'f': forgetWeight = w; break;
            case 'i': inputWeight = w; break;
            case 'c': candidateWeight = w; break;
            case 'o': outputWeight = w; break;
            default: throw new IllegalStateException("Connection doesn't have gate specified.");
        }
    }
    public double getWeight(char selectGate) {
        switch (selectGate) {
            case 'f': return forgetWeight;
            case 'i': return inputWeight;
            case 'c': return candidateWeight;
            case 'o': return outputWeight;
            default: throw new IllegalStateException("Connection doesn't have gate specified.");
        }
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean e) { enabled = e; }

    public boolean equals(Object o) {
        if (!(o instanceof MRUConGene)) return false;
        return fG.equals(((MRUConGene) o).fG) && tG.equals(((MRUConGene) o).tG);
    }

    @Override
    public String inspect() {
        return "\nGene Type: MRU-ConGene " + fG + "->" + tG +
                "\nInnovation No.: " + IN +
                "\nEnabled: " + ((enabled) ? "YES" : "NO") +
                "\n*Parameters" +
                "\n**Weights" +
                "\n***Forget gate: " + forgetWeight +
                "\n***Input gate: " + inputWeight +
                "\n***Candidate gate: " + candidateWeight +
                "\n***Output gate: " + outputWeight;
    }

    @Override
    public int hashCode() { return fG.getIN() * NEAT.MAX_NODE + tG.getIN(); }

    @Override
    public String toString() {
        return "(" + IN + ")<" + fG + (enabled ? "" : "!") +
                "(f" + (Math.round(forgetWeight * 10.0) / 10.0) +
                ",i" + (Math.round(inputWeight * 10.0) / 10.0) +
                ",c" + (Math.round(candidateWeight * 10.0) / 10.0) +
                ",o" + (Math.round(outputWeight * 10.0) / 10.0) +
                ")" + tG + "> ";
    }

}
