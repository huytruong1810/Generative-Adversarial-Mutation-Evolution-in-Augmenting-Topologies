package GANAS;

import NEAT.DataStructures.SecuredList;
import NEAT.GenePool;
import NEAT.Genome.ConGene;
import NEAT.Genome.DecisionHead.DHcg;
import NEAT.Genome.DecisionHead.DHg;
import NEAT.Genome.DecisionHead.DHng;
import NEAT.Genome.MemoryHead.MHcg;
import NEAT.Genome.MemoryHead.MHg;
import NEAT.Genome.MemoryHead.MHng;
import NEAT.Genome.NodeGene;
import NEAT.Individual;
import org.ejml.simple.SimpleMatrix;

public class Translator {

    public static int featureDim = 6,
            IN_INDEX = 0, FROM_INDEX = 1, TO_INDEX = 2,
            X_INDEX = 3, Y_INDEX = 4, TYPE_INDEX = 5, MH_CODE = -10, DH_CODE = 10;

    public static class OCX {
        public SimpleMatrix origA, changedA, X;
        public OCX(SimpleMatrix ARef, SimpleMatrix XRef) {
            origA = ARef; X = XRef;
        }
        public SimpleMatrix diffMat() { // this leaves behind 1.0's that reflects only the changes
            if (changedA == null) throw new IllegalStateException("Change adj matrix is required.");
            return changedA.minus(origA);
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

    public OCX translate(Individual individual, boolean isMH) {

        double[][] adjMat = new double[dim][dim];
        double[][] featuresMat = new double[dim][featureDim]; // non-nodes get zeros entries
        // add self-loop to denote existence of a node AND for GCN self-feature consideration
        for (NodeGene n : (isMH ? individual.getMH().getNodes() : individual.getDH().getNodes()).getData()) {
            int IN = n.getIN();
            adjMat[IN][IN] = 1.0;
            double[] featureRef = featuresMat[IN];
            featureRef[IN_INDEX] = IN;
            featureRef[X_INDEX] = n.getX();
            featureRef[Y_INDEX] = n.getY();
            featureRef[TYPE_INDEX] = isMH ? MH_CODE : DH_CODE;
        }
        for (ConGene c : (isMH ? individual.getMH().getCons() : individual.getDH().getCons()).getData()) {
            NodeGene f = c.getFG(), t = c.getTG();
            int fIN = f.getIN(), tIN = t.getIN();
            adjMat[fIN][tIN] = 1.0;
            ConGene conGene = isMH ? new MHcg(f, t, 'p') : new DHcg(f, t, 'p');
            GenePool.NodeType type = isMH ? GenePool.NodeType.MH : GenePool.NodeType.DH;
            int IN = genePool.getBetweenNodeIN(conGene, type);
            if (IN != -1) { // -1 means no node in between the connection so leave it as [ ... 0 0 ... ]
                if (adjMat[IN][IN] == 1.0) { // see if this node exists in this genome
                    double[] featureRef = featuresMat[IN]; // inform its feature matrix with the two ending nodes
                    featureRef[FROM_INDEX] = fIN;
                    featureRef[TO_INDEX] = tIN;
                }
            }
        }
        return new OCX(new SimpleMatrix(adjMat), new SimpleMatrix(featuresMat));

    }

    public void parseMH(MHg genome, int[] checksum, OCX ocx) {

        SimpleMatrix diffMat = ocx.diffMat();
        if (diffMat.numRows() != dim || diffMat.numCols() != dim)
            throw new IllegalStateException("Incorrect dimensions of difference matrix.");

        SimpleMatrix nodeFeats = ocx.X;
        SecuredList<MHng> nodes = genome.getNodes();
        SecuredList<MHcg> cons = genome.getCons();

        // for error checking
        int numMutatedCons = checksum[0], numMutatedNodes = checksum[1];
        int numNewCons = 0, numNewNodes = 0;

        // add all the new nodes (if any) first, these nodes are non-base
        for (int IN = 1; IN < dim; ++IN) {
            if (diffMat.get(IN, IN) == 1.0) { // found new node
                numNewNodes++;
                if (nodeFeats.get(IN, TYPE_INDEX) != MH_CODE)
                    throw new IllegalStateException("Node type is not of memory head.");
                NodeGene m =
                        genePool.logNode(-1,
                                new MHcg(new MHng((int) (nodeFeats.get(IN, FROM_INDEX))), new MHng((int) (nodeFeats.get(IN, TO_INDEX))), 'p'),
                                nodeFeats.get(IN, X_INDEX), nodeFeats.get(IN, Y_INDEX), GenePool.NodeType.MH, false);
                if (m.getIN() != (int) nodeFeats.get(IN, IN_INDEX))
                    throw new IllegalStateException("Mismatch node mutation: " + m.getIN() + ", " + nodeFeats.get(IN, IN_INDEX));
                nodes.addInOrder((MHng) m);
            }
        }

        // now add the new connections (if any)
        for (int A_IN = 1; A_IN < dim; ++A_IN) {
            for (int B_IN = 1; B_IN < dim; ++B_IN) {
                if (A_IN == B_IN) continue; // skip diagonal values
                if (diffMat.get(A_IN, B_IN) == 1.0) { // found new connection
                    numNewCons++;
                    NodeGene a = nodes.get(new MHng(A_IN, 'p'));
                    NodeGene b = nodes.get(new MHng(B_IN, 'p'));
                    MHcg a_b = (MHcg) genePool.logCon(a, b, GenePool.ConType.MH);
                    b.addInCons(a_b);
                    cons.addInOrder(a_b);
                }
            }
        }

        if (numNewCons != numMutatedCons || numNewNodes != numMutatedNodes)
            throw new IllegalStateException("Mismatch mutation checksum. " +
                    "Invalid parsing of MH genome has been done: " +
                    numNewCons + ", " + numMutatedCons + " and " + numNewNodes + ", " + numMutatedNodes);

    }

    public void parseDH(DHg genome, int[] checksum, OCX ocx) {

        SimpleMatrix diffMat = ocx.diffMat();
        if (diffMat.numRows() != dim || diffMat.numCols() != dim)
            throw new IllegalStateException("Incorrect dimensions of difference matrix.");

        SimpleMatrix nodeFeats = ocx.X;
        SecuredList<DHng> nodes = genome.getNodes();
        SecuredList<DHcg> cons = genome.getCons();

        // for error checking
        int numMutatedCons = checksum[0], numMutatedNodes = checksum[1];
        int numNewCons = 0, numNewNodes = 0;

        // add all the new nodes (if any) first, these nodes are non-base
        for (int IN = 1; IN < dim; ++IN) {
            if (diffMat.get(IN, IN) == 1.0) { // found new node
                numNewNodes++;
                if (nodeFeats.get(IN, TYPE_INDEX) != DH_CODE)
                    throw new IllegalStateException("Node type is not of decision head.");
                NodeGene m =
                        genePool.logNode(-1,
                                new DHcg(new DHng((int) (nodeFeats.get(IN, FROM_INDEX))), new DHng((int) (nodeFeats.get(IN, TO_INDEX))), 'p'),
                                nodeFeats.get(IN, X_INDEX), nodeFeats.get(IN, Y_INDEX), GenePool.NodeType.DH, false);
                if (m.getIN() != (int) nodeFeats.get(IN, IN_INDEX))
                    throw new IllegalStateException("Mismatch node mutation: " + m.getIN() + ", " + nodeFeats.get(IN, IN_INDEX));
                nodes.addInOrder((DHng) m);
            }
        }

        // now add the new connections (if any)
        for (int A_IN = 1; A_IN < dim; ++A_IN) {
            for (int B_IN = 1; B_IN < dim; ++B_IN) {
                if (A_IN == B_IN) continue; // skip diagonal values
                if (diffMat.get(A_IN, B_IN) == 1.0) { // found new connection
                    numNewCons++;
                    NodeGene a = nodes.get(new DHng(A_IN, 'p'));
                    NodeGene b = nodes.get(new DHng(B_IN, 'p'));
                    DHcg a_b = (DHcg) genePool.logCon(a, b, GenePool.ConType.DH);
                    b.addInCons(a_b);
                    cons.addInOrder(a_b);
                }
            }
        }

        if (numNewCons != numMutatedCons || numNewNodes != numMutatedNodes)
            throw new IllegalStateException("Mismatch mutation checksum. " +
                    "Invalid parsing of DH genome has been done: " +
                    numNewCons + ", " + numMutatedCons + " and " + numNewNodes + ", " + numMutatedNodes);

    }

}
