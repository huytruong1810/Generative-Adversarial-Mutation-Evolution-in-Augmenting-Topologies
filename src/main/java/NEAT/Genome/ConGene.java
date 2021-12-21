package NEAT.Genome;

import static NEAT.GenePool.MAX_NODE;

public abstract class ConGene extends Gene {

    protected boolean enabled;
    protected final NodeGene f, t;

    public ConGene(NodeGene fG, NodeGene tG) {

        if (fG.getX() >= tG.getX())
            throw new IllegalStateException("From-gene has to be left of to-gene.");
        f = fG;
        t = tG; // this constructor do not add connection to node tG
        enabled = true;

    }

    public ConGene(NodeGene fG, NodeGene tG, char proxy) { // proxy is flag for method matching
        if (proxy != 'p') throw new IllegalStateException("Invalid acknowledgement of proxy constructor.");
        // returns a lite-proxy for record keeping
        f = fG;
        t = tG;
    }

    public NodeGene getFG() { return f; }
    public NodeGene getTG() { return t; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean e) { enabled = e; }

    public boolean equals(Object o) {
        if (!(o instanceof ConGene)) return false;
        return f.equals(((ConGene) o).f) && t.equals(((ConGene) o).t); // basing on innovation number
    }

    public abstract ConGene redirectClone(NodeGene f, NodeGene t) throws CloneNotSupportedException ;

    @Override
    public int hashCode() { return f.getIN() * MAX_NODE + t.getIN(); }

    @Override
    public String inspect() {
        return f + "->" + t +
                "\nInnovation No.: " + IN +
                "\nEnabled: " + ((enabled) ? "YES" : "NO");
    }

    @Override
    public String toString() {
        return "(" + IN + ")<" + f + (enabled ? "" : "!") + t + ">";
    }

}
