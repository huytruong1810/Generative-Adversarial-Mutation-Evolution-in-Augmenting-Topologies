package NEAT.Genome.ACU;

import NEAT.Genome.Gene;
import NEAT.NEAT;

public class ACUConGene extends Gene {

    private final ACUNodeGene fG, tG;
    private double criticWeight, actorWeight;
    private boolean enabled;

    public ACUConGene(ACUNodeGene fG, ACUNodeGene tG) {
        if (fG.getX() >= tG.getX()) throw new IllegalStateException("From-gene is after to-gene.");
        this.fG = fG;
        this.tG = tG;
        enabled = true;
    }

    public ACUNodeGene getFG() { return fG; }
    public ACUNodeGene getTG() { return tG; }

    public void setWeight(double w, boolean selectActor) {
        if (selectActor) actorWeight = w;
        else criticWeight = w;
    }
    public double getWeight(boolean selectActor) { return selectActor ? actorWeight : criticWeight; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean e) { enabled = e; }

    public boolean equals(Object o) {
        if (!(o instanceof ACUConGene)) return false;
        return fG.equals(((ACUConGene) o).fG) && tG.equals(((ACUConGene) o).tG);
    }

    @Override
    public String inspect() {
        return "\nGene Type: ACU-ConGene " + fG + "->" + tG +
                "\nInnovation No.: " + IN +
                "\nEnabled: " + ((enabled) ? "YES" : "NO") +
                "\n*Parameters" +
                "\n**Weights" +
                "\n***Actor: " + actorWeight +
                "\n***Critic: " + criticWeight;
    }

    @Override
    public int hashCode() { return fG.getIN() * NEAT.MAX_NODE + tG.getIN(); }

    @Override
    public String toString() {
        return "(" + IN + ")<" + fG + (enabled ? "" : "!") +
                "(a" + (Math.round(actorWeight * 10.0) / 10.0) +
                ",c" + (Math.round(criticWeight * 10.0) / 10.0) + ")" + tG + "> ";
    }

}
