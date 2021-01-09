package Neat.Genome;

import Neat.DataStructures.RandomHashSet;
import Neat.Species;

public class Genome {

    private double score;
    private Species species;
    private NeuralNetwork phenotype;
    private RandomHashSet<ConGene> cons;
    private RandomHashSet<NodeGene> nodes;

    public Genome() { cons = new RandomHashSet<>(); nodes = new RandomHashSet<>(); }

    public void setScore(double s) { score = s; }
    public double getScore() { return score; }

    public void setSpecies(Species s) { species = s; }
    public Species getSpecies() { return species; }

    public RandomHashSet<NodeGene> getNodes() { return nodes; }
    public RandomHashSet<ConGene> getCons() { return cons; }

    public void cast(Genome g) {
        species = g.species;
        phenotype = g.phenotype;
        cons = g.cons;
        nodes = g.nodes;
    }

    public void express() { phenotype = new NeuralNetwork(this); }

    public double[] feed(double[] inputs, boolean selectActor) {
        if (phenotype == null) express();
        return phenotype.forward(inputs, selectActor);
    }

    public void train(double[] inputs, double gradient, int takenAction, boolean selectActor) {
        // network should be most updated before training
        if (selectActor) phenotype.classificationFit(inputs, gradient, takenAction);
        else phenotype.regressionFit(inputs, gradient);
    }

    @Override
    public String toString() {
        StringBuilder rep = new StringBuilder("<Genome/Score=" + (Math.round(score * 100.0) / 100.0) + "/Nodes:");
        for (NodeGene n:nodes.getData()) rep.append(n);
        rep.append("/Cons:");
        for (ConGene c:cons.getData()) rep.append(c);
        return rep + "/>";
    }

}
