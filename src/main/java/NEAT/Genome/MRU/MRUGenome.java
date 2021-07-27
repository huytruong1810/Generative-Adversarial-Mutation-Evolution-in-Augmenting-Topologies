package NEAT.Genome.MRU;

import NEAT.DataStructures.GeneSet;
import NEAT.Genome.Genome;
import NEAT.DataStructures.MemUnit;

import java.util.HashMap;

public class MRUGenome extends Genome {

    private LSTM phenotype;
    private GeneSet<MRUConGene> cons;
    private GeneSet<MRUNodeGene> nodes;

    public MRUGenome(int inN, int outN) {
        super(inN, outN);
        cons = new GeneSet<>();
        nodes = new GeneSet<>();
    }

    public GeneSet<MRUNodeGene> getNodes() { return nodes; }
    public GeneSet<MRUConGene> getCons() { return cons; }

    @Override
    public void cast(Genome g) {
        super.cast(g);
        phenotype = ((MRUGenome) g).phenotype;
        cons = ((MRUGenome) g).cons;
        nodes = ((MRUGenome) g).nodes;
    }

    @Override
    public void express() { phenotype = new LSTM(this); }

    public MemUnit feed(MemUnit prevUnit, double[] input) { return phenotype.forward(prevUnit, input); }

    public void trainGates(MemUnit memUnit, MemUnit prevUnit, double[] dL_dCnext, double[] dL_dhnext,
                           HashMap<Integer, Double> actorGradRet, HashMap<Integer, Double> criticGradRet,
                           HashMap<Integer, Double> fConGrads, HashMap<Integer, Double> iConGrads,
                           HashMap<Integer, Double> cConGrads, HashMap<Integer, Double> oConGrads) {

        double[] dL_dh_acu = new double[outputNum];
        // these are innovation numbers at input layer of ACU, so 1..hidN
        for (int i = 0; i < outputNum; ++i) {
            int IN = i + 1;
            dL_dh_acu[i] = ((actorGradRet.containsKey(IN)) ? actorGradRet.get(IN) : 0) + ((criticGradRet.containsKey(IN)) ? criticGradRet.get(IN) : 0);
        }
        /** TESTING----------------------------------------------------------------------------------------------------=
         System.out.print("sum actor-critic gradient: ");
         for (double d : dL_dh_acu) System.out.print(d + ", ");
         System.out.println();
         /** TESTING----------------------------------------------------------------------------------------------------=
         */
        phenotype.BPTT(memUnit, prevUnit, dL_dh_acu, dL_dCnext, dL_dhnext, fConGrads, iConGrads, cConGrads, oConGrads);

    }

    @Override
    public String toString() {
        StringBuilder rep = new StringBuilder("<MRU(");
        rep.append(getID());
        rep.append(")/Nodes:");
        for (MRUNodeGene n : nodes.getData()) rep.append(n);
        rep.append("/Cons:");
        for (MRUConGene c : cons.getData()) rep.append(c);
        return rep + "/MRU>";
    }

    @Override
    public MRUGenome clone() throws CloneNotSupportedException {

        super.clone(); // push down global ID
        MRUGenome theClone = new MRUGenome(inputNum, outputNum); // push it up

        GeneSet<MRUNodeGene> cloneNodes = theClone.nodes;
        GeneSet<MRUConGene> cloneCons = theClone.cons;
        for (MRUNodeGene n : nodes.getData()) cloneNodes.add(n.clone());
        for (MRUConGene c : cons.getData()) {

            MRUNodeGene n1 = cloneNodes.get(c.getFG()), n2 = cloneNodes.get(c.getTG());
            MRUConGene cloneCon = new MRUConGene(n1, n2);
            cloneCon.setIN(c.getIN());
            cloneCon.setWeight(c.getWeight('f'), 'f');
            cloneCon.setWeight(c.getWeight('i'), 'i');
            cloneCon.setWeight(c.getWeight('c'), 'c');
            cloneCon.setWeight(c.getWeight('o'), 'o');
            cloneCon.setEnabled(c.isEnabled());
            n2.getInCons().add(cloneCon);
            cloneCons.add(cloneCon);

        }
        theClone.ID = ID;

        return theClone;

    }

}
