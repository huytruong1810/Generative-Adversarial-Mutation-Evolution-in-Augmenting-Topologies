package NEAT.Genome;

import NEAT.DataStructures.GraphicPack;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class NodeGene extends Gene implements Comparable<NodeGene> {

    protected double x, y;
    protected final ArrayList<ConGene> inCons;

    // for gradient computation spanning across time steps and layers
    protected HashMap<ConGene, double[]> originalWeights;

    public NodeGene(int innovationNumber) {
        IN = innovationNumber;
        inCons = new ArrayList<>();
    }

    public NodeGene(int innovationNumber, char proxy) {
        if (proxy != 'p') throw new IllegalStateException("Invalid acknowledgement of proxy constructor.");
        IN = innovationNumber;
        x = y = Double.NEGATIVE_INFINITY;
        inCons = null;
        originalWeights = null;
    }

    public NodeGene(NodeGene other) {
        // returns a lite-proxy for efficient collection search and retrieve
        IN = other.IN;
        x = y = Double.NEGATIVE_INFINITY;
        inCons = null;
        originalWeights = null;
    }

    public NodeGene(NodeGene other, double rec_x, double rec_y) {
        // returns a proxy for x, y record keeping
        IN = other.IN;
        x = rec_x;
        y = rec_y;
        inCons = null;
    }

    // do this at the beginning of each training episodes
    public abstract void episodePrep();
    // do this at the end of each training episodes
    public abstract void episodeDone();

    public double getX() { return x; }
    public double getY() { return y; }
    public void setX(double val) { x = val; }
    public void setY(double val) { y = val; }

    public void addInCons(ConGene c) {
        if (inCons == null) throw new IllegalStateException("Cannot use proxy.");
        inCons.add(c);
    }

    public boolean equals(Object o) {
        if (!(o instanceof NodeGene)) return false;
        return IN == ((NodeGene) o).IN;
    }

    @Override
    public String inspect() {
        return "\nInnovation No.: " + IN +
                "\nX: " + x + "\nY: " + y;
    }

    @Override
    public int hashCode() { return IN; }

    @Override
    public int compareTo(NodeGene o) { return Double.compare(x, o.x); }

    @Override
    public String toString() { return "(" + IN + ")"; }

    public StringBuilder parseInCons() {
        if (inCons == null) throw new IllegalStateException("Cannot use proxy.");
        StringBuilder allConsDetails = new StringBuilder();
        for (ConGene c : inCons) allConsDetails.append("\n").append(c.inspect());
        return allConsDetails;
    }

    public GraphicPack[] getGraphicInCons() {
        if (inCons == null) throw new IllegalStateException("Cannot use proxy.");
        int n = inCons.size();
        GraphicPack[] packs = new GraphicPack[n];
        for (int i = 0; i < n; ++i) {
            ConGene c = inCons.get(i);
            packs[i] = new GraphicPack(c.enabled, c.f.x, c.f.y, c.inspect());
        }
        return packs;
    }

}
