package Neat.Genome;

import Neat.Neat;

public class ConGene extends Gene {

    private NodeGene fG, tG;
    private double criticWeight, actorWeight;
    private boolean enabled;

    public ConGene(NodeGene fG, NodeGene tG) {
        this.fG = fG;
        this.tG = tG;
        this.tG.addCon(this);
        enabled = true;
    }

    public NodeGene getFG() { return fG; }
    public NodeGene getTG() { return tG; }

    public void setWeight(double w, boolean selectActor) {
        if (selectActor) actorWeight = w;
        else criticWeight = w;
    }
    public double getWeight(boolean selectActor) { return selectActor ? actorWeight : criticWeight; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean e) { enabled = e; }

    public boolean equals(Object o) {
        if (!(o instanceof ConGene))
            return false;
        ConGene c = (ConGene)o;
        return fG.equals(c.fG) && tG.equals(c.tG);
    }

    @Override
    public int hashCode() { return fG.IN * Neat.MAXNODE + tG.getIN(); }

    @Override
    public String toString() {
        return " " + IN + "(" + fG + " ~" + (enabled ? "" : "!") +
                "(a" + (Math.round(actorWeight * 10.0) / 10.0) +
                ",c" + (Math.round(criticWeight * 10.0) / 10.0) + ")~" + tG + " )";
    }

}
