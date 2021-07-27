package NEAT.Genome.ACU;

import NEAT.DataStructures.GeneSet;
import NEAT.Genome.Genome;
import NEAT.DataStructures.MemUnit;

import java.util.HashMap;

public class ACUGenome extends Genome {

    private NeuralNetwork phenotype;
    private GeneSet<ACUConGene> cons;
    private GeneSet<ACUNodeGene> nodes;

    public ACUGenome(int inN, int outN) {
        super(inN, outN);
        cons = new GeneSet<>();
        nodes = new GeneSet<>();
    }

    public GeneSet<ACUNodeGene> getNodes() { return nodes; }
    public GeneSet<ACUConGene> getCons() { return cons; }

    @Override
    public void cast(Genome g) {
        super.cast(g);
        phenotype = ((ACUGenome) g).phenotype;
        cons = ((ACUGenome) g).cons;
        nodes = ((ACUGenome) g).nodes;
    }

    @Override
    public void express() { phenotype = new NeuralNetwork(this); }

    public double[] feed(double[] inputs, boolean selectActor) {
        double[] outputs = phenotype.forward(inputs, selectActor);
        if (!selectActor) return outputs; // critic has 1 output
        else {
            double[] probs = new double[outputNum - 1]; // actor has number of output - 1
            System.arraycopy(outputs, 0, probs, 0, outputNum - 1);
            return probs;
        }
    }

    public void trainActorCritic(MemUnit memUnit,
                                 HashMap<Integer, Double> actorConGrads, HashMap<Integer, Double> criticConGrads,
                                 HashMap<Integer, Double> actorGradRet, HashMap<Integer, Double> criticGradRet) {

        int takenAction = memUnit.a();
        double[] inputs = memUnit.h();
        double actorGrad = memUnit.aG();
        double criticGrad = memUnit.cG();

        phenotype.classificationFit(inputs, actorGrad, takenAction, actorConGrads, actorGradRet);
        phenotype.regressionFit(inputs, criticGrad, criticConGrads, criticGradRet);

    }

    @Override
    public String toString() {
        StringBuilder rep = new StringBuilder("<ACU(");
        rep.append(getID());
        rep.append(")/Nodes:");
        for (ACUNodeGene n : nodes.getData()) rep.append(n);
        rep.append("/Cons:");
        for (ACUConGene c : cons.getData()) rep.append(c);
        return rep + "/ACU>";
    }

    @Override
    public ACUGenome clone() throws CloneNotSupportedException {

        super.clone(); // push down global ID
        ACUGenome theClone = new ACUGenome(inputNum, outputNum); // push it up

        GeneSet<ACUNodeGene> cloneNodes = theClone.nodes;
        GeneSet<ACUConGene> cloneCons = theClone.cons;
        for (ACUNodeGene n : nodes.getData()) cloneNodes.add(n.clone());
        for (ACUConGene c : cons.getData()) {

            ACUNodeGene n1 = cloneNodes.get(c.getFG()), n2 = cloneNodes.get(c.getTG());
            ACUConGene cloneCon = new ACUConGene(n1, n2);
            cloneCon.setIN(c.getIN());
            cloneCon.setWeight(c.getWeight(true), true);
            cloneCon.setWeight(c.getWeight(false), false);
            cloneCon.setEnabled(c.isEnabled());
            n2.getInCons().add(cloneCon);
            cloneCons.add(cloneCon);

        }
        theClone.ID = ID;

        return theClone;

    }

}
