package Supervisors;

import NEAT.DataStructures.SecuredList;
import NEAT.GenePool;
import NEAT.Genome.DecisionHead.DHcg;
import NEAT.Genome.DecisionHead.DHg;
import NEAT.Genome.DecisionHead.DHng;
import NEAT.Genome.MemoryHead.MHcg;
import NEAT.Genome.MemoryHead.MHg;
import NEAT.Genome.MemoryHead.MHng;
import NEAT.Genome.NodeGene;
import org.ejml.simple.SimpleMatrix;

public class Translator {

    public static class OCX {
        public SimpleMatrix origA, changedA, X;
        public OCX(SimpleMatrix ARef, SimpleMatrix XRef) {
            origA = ARef; X = XRef;
        }
        public SimpleMatrix diffMat() {
            if (changedA == null) throw new IllegalStateException("Change adj matrix is required.");
            return origA.minus(changedA);
        }
    }

    public static class OCXPair { // this is used in the context of discriminator training set
        public int c;
        public OCX OCX1, OCX2;
        public OCXPair(OCX one, OCX two, int label) {
            OCX1 = one; OCX2 = two; c = label;
        }
    }

    private final int dim;
    private final GenePool genePool;

    public Translator(int numNodes, GenePool pool) {
        dim = numNodes;
        genePool = pool;
    }

    public OCX translateMRU(MHg genome) {
        double[][] adjMat = new double[dim][dim];
        double[][] featuresMat = new double[dim][3]; // non-nodes get [0 0 0] entries
        // add self-loop to denote existence of a node AND for GCN self-feature consideration
        for (MHng n : genome.getNodes().getData()) {
            int IN = n.getIN();
            adjMat[IN][IN] = 1.0;
            featuresMat[IN] = new double[] {IN, n.getX(), n.getY()};
        }
        for (MHcg c : genome.getCons().getData()) adjMat[c.getFG().getIN()][c.getTG().getIN()] = 1.0;
        return new OCX(new SimpleMatrix(adjMat), new SimpleMatrix(featuresMat));
    }

    public OCX translateACU(DHg genome) {
        double[][] adjMat = new double[dim][dim];
        double[][] featuresMat = new double[dim][3];
        // add self-loop to denote existence of a node AND for GCN self-feature consideration
        for (DHng n : genome.getNodes().getData()) {
            int IN = n.getIN();
            adjMat[IN][IN] = 1.0;
            featuresMat[IN] = new double[] {IN, n.getX(), n.getY()};
        }
        for (DHcg c : genome.getCons().getData()) adjMat[c.getFG().getIN()][c.getTG().getIN()] = 1.0;
        return new OCX(new SimpleMatrix(adjMat), new SimpleMatrix(featuresMat));
    }

    public void parseMRU(MHg genome, int numMutatedCons, OCX ocx) {

        // this leaves behind 1.0's that reflects only the new changes
        SimpleMatrix diffMat = ocx.diffMat();
        SimpleMatrix newCoors = ocx.X;
        SecuredList<MHng> nodes = genome.getNodes();
        SecuredList<MHcg> cons = genome.getCons();

        // for error checking
        int numNewNodes = 0;
        int numNewCons = 0;

        for (int A_IN = 1, numRows = diffMat.numRows(); A_IN < numRows; ++A_IN) {
            for (int B_IN = 1, numCols = diffMat.numCols(); B_IN < numCols; ++B_IN) {
                if (diffMat.get(A_IN, B_IN) == 1.0) { // found new stuff
                    if (A_IN == B_IN) { // if it is a node
                        numNewNodes++;
                        NodeGene m = genePool.logNode(-1,
                                newCoors.get(A_IN, 1), newCoors.get(A_IN, 2),
                                GenePool.NodeType.MRU, false);
                        if (m.getIN() != newCoors.get(A_IN, 0))
                            throw new IllegalStateException("Mismatch node mutation.");
                        nodes.add((MHng) m);
                    } else { // if it is a connection
                        numNewCons++;
                        NodeGene a = nodes.get(new MHng(A_IN, 'p'));
                        NodeGene b = nodes.get(new MHng(B_IN, 'p'));
                        MHcg a_b = (MHcg) genePool.logCon(a, b, GenePool.ConType.MRU);
                        b.addInCons(a_b);
                        cons.addInOrder(a_b);
                    }
                }
            }
        }

        // for each mutated node, there should be 2 connections added as well
        if ((numNewNodes * 2 + numMutatedCons) != numNewCons)
            throw new IllegalStateException("Mismatch mutation checksum. Invalid parsing of MRU genome has been done.");

    }

    public void parseACU(DHg genome, int numMutatedCons, OCX OCX) {

        // this leaves behind 1.0's that reflects only the new changes
        SimpleMatrix diffMat = OCX.diffMat();
        SimpleMatrix newCoors = OCX.X;
        SecuredList<DHng> nodes = genome.getNodes();
        SecuredList<DHcg> cons = genome.getCons();

        // for error checking
        int numNewNodes = 0;
        int numNewCons = 0;

        for (int A_IN = 1, numRows = diffMat.numRows(); A_IN < numRows; ++A_IN) {
            for (int B_IN = 1, numCols = diffMat.numCols(); B_IN < numCols; ++B_IN) {
                if (diffMat.get(A_IN, B_IN) == 1.0) { // found new stuff
                    if (A_IN == B_IN) { // if it is a node
                        numNewNodes++;
                        NodeGene m = genePool.logNode(-1,
                                newCoors.get(A_IN, 1), newCoors.get(A_IN, 2),
                                GenePool.NodeType.ACU, false);
                        if (m.getIN() != newCoors.get(A_IN, 0))
                            throw new IllegalStateException("Mismatch node mutation.");
                        nodes.add((DHng) m);
                    } else { // if it is a connection
                        numNewCons++;
                        NodeGene a = nodes.get(new DHng(A_IN, 'p'));
                        NodeGene b = nodes.get(new DHng(B_IN, 'p'));
                        DHcg a_b = (DHcg) genePool.logCon(a, b, GenePool.ConType.ACU);
                        b.addInCons(a_b);
                        cons.addInOrder(a_b);
                    }
                }
            }
        }

        // for each mutated node, there should be 2 connections added as well
        if ((numNewNodes * 2 + numMutatedCons) != numNewCons)
            throw new IllegalStateException("Mismatch mutation checksum. Invalid parsing of ACU genome has been done.");

    }

}
