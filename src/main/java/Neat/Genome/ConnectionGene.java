package Neat.Genome;

import Neat.Neat;

public class ConnectionGene extends Gene {

    private NodeGene fG, tG;
    private double weight;
    private boolean enabled = true;

    public ConnectionGene(NodeGene fG, NodeGene tG) {
        this.fG = fG;
        this.tG = tG;
    }

    public NodeGene getFG() {
        return fG;
    }
    public NodeGene getTG() {
        return tG;
    }

    public void setWeight(double w) {
        weight = w;
    }
    public double getWeight() {
        return weight;
    }

    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean e) {
        enabled = e;
    }

    public boolean equals(Object o) {
        if (!(o instanceof ConnectionGene))
            return false;
        ConnectionGene c = (ConnectionGene)o;
        return fG.equals(c.fG) && tG.equals(c.tG);
    }

    public int hashCode() {
        return fG.getIN() * Neat.MAXNODE + tG.getIN();
    }

}
