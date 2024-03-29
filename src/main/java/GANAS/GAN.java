package GANAS;

import NEAT.DataStructures.SecuredList;
import NEAT.GenePool;
import NEAT.Genome.ConGene;
import NEAT.Genome.DecisionHead.DHcg;
import NEAT.Genome.DecisionHead.DHng;
import NEAT.Genome.MemoryHead.MHcg;
import NEAT.Genome.MemoryHead.MHng;
import NEAT.Individual;
import org.ejml.simple.SimpleMatrix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import static NEAT.GenePool.MAX_NODE;
import static GANAS.Translator.*;

public class GAN {

    /**-----------------------------------------------------------------------------------------------------------------
     * Generator's reward assignments
     * we use small values so a baseline for REINFORCE is not required
     */
    private static final double invalidMutation = -0.05;
    private static final double validMutation = 0.1;
    private static final double exceedMaxNode = -0.05;
    private static final double repeatedNodeAddition = -0.05;
    // apply to the termination of an episode (both in natural or self-termination)
    private static final double discriminatorAccept = 0.2;
    private static final double discriminatorReject = -0.1; // discourage early episode termination

    /**-----------------------------------------------------------------------------------------------------------------
     * GAN hyper-parameters
     */
    private static final int K = 10; // k-top architectures
    private static final int codeDim = 16;
    private static final int epochs = 2;
    private static final int tryIterations = 5; // max episodic time horizon
    private static final int nodeIndices = MAX_NODE + 1; // IN starts at 1
    private static final double gamma = 0.99; // discount factor
    private static final double epsilon = 0.5, epsilonDecay = 0.5, epsilonMin = 0.01; // exploration rate
    private static final double alpha = 1e-4; // learning rate

    /**-----------------------------------------------------------------------------------------------------------------
     * GAN submodules
     */
    private final Generator G;
    private final Discriminator D;
    private final GenePool genePool;
    private final Translator translator;

    /**-----------------------------------------------------------------------------------------------------------------
     * cache components
     */
    private Translator.OCX[] realMH;
    private Translator.OCX[] realDH;

    public GAN(GenePool genePoolRef) {
        G = new Generator(featureDim, codeDim, 32, nodeIndices, 2, alpha);
        D = new Discriminator(featureDim, codeDim, 32, nodeIndices, 2, alpha);
        genePool = genePoolRef;
        translator = new Translator(nodeIndices, genePoolRef);
    }

    /**
     * IMPORTANT: should only be invoked by an uninformed supervisor due to infinite loop risk
     * This method will change referenced individual in-place
     * The mutations generated by the method is time-independent
     * @param individual - the individual to be forced mutation(s) on
     * @param numMutations - the number of mutations we want to force
     * @param isMH - are we forcing G-mutation(S) on memory head or decision head genome
     */
    public void forceMutate(Individual individual, int numMutations, boolean isMH) {
        Translator.OCX ocx = translator.translate(individual, isMH);
        SimpleMatrix X = ocx.X;
        SimpleMatrix A = ocx.origA.copy(); // copy because genome parsing needs original A unchanged
        int topIN = isMH ? genePool.getMH_cap() : genePool.getDH_cap(); // extract highest IN for proximal changes
        int[] checksum = new int[] {0, 0, 0};
        int count = 0;
        G.train(); // test
        while (count < numMutations) { // loop until satisfies number of mutations
            int[] result = runG(0, X, A, topIN, isMH);
            checksum[0] += result[0];
            checksum[1] += result[1];
            topIN = result[2];
            if (result[0] > 0) count++;
        }
        G.doneTrain(); // no training happen
        ocx.changedA = A;
        if (isMH) translator.parseMH(individual.getMH(), checksum, ocx);
        else translator.parseDH(individual.getDH(), checksum, ocx);
    }

    /**
     * Train the Discriminator on recognizing between top and G-mutated architectures
     * @param pplt - this population needs to have already been sorted in ascending order by score
     * @param toBeMutated - let Generator pseudo-mutate this individual for "fake" train-set
     * @param MH_CELRef - reference to MH CEL array for method to populate
     * @param DH_CELRef - reference to DH CEL array for method to populate
     */
    public void trainD(SecuredList<Individual> pplt, Individual toBeMutated,
                       ArrayList<Double> MH_CELRef, ArrayList<Double> DH_CELRef) {

        // collect K number of top architectures and cache them for Generator training later
        realMH = new Translator.OCX[K];
        realDH = new Translator.OCX[K];
        for (int N = pplt.size(), i = N - K; i < N; ++i) {
            Individual individual = pplt.get(i);
            // fill MH_TrueOCXs bottom-up with ascending score collection
            realMH[i-N+K] = translator.translate(individual, true);
            realDH[i-N+K] = translator.translate(individual, false);
        }

        subDTrain(toBeMutated, MH_CELRef, true);
        subDTrain(toBeMutated, DH_CELRef, false);

    }

    private void subDTrain(Individual toBeMutated, ArrayList<Double> CELArrRef, boolean isMH) {

        // should already be collected
        Translator.OCX[] real = isMH ? realMH : realDH;

        // collect K number of G-mutations on to-be-mutated genomes
        Translator.OCX[] fake = new Translator.OCX[K];
        for (int k = 0; k < K; ++k) {

            Translator.OCX ocx = translator.translate(toBeMutated, isMH);
            SimpleMatrix X = ocx.X;
            SimpleMatrix A = ocx.origA; // use directly because no genome parsing

            int topIN = isMH ? genePool.getMH_cap() : genePool.getDH_cap(); // extract highest IN for proximal changes
            G.train(); // test
            for (int t = 0; t < tryIterations; ++t) {
                int[] result = runG(t, X, A, topIN, isMH);
                topIN = result[2];
            }
            G.doneTrain(); // no training happen

            fake[k] = new Translator.OCX(A.copy(), X.copy());

        }

        // construct training set for Discriminator
        ArrayList<Translator.OCXPair> trainSet = new ArrayList<>();
        for (int i = 0; i < K; ++i) {
            Translator.OCX realInstance = real[i];
            for (int j = 0; j < K; ++j) {
                // positive pairs
                if (j != i) trainSet.add(new Translator.OCXPair(realInstance, real[j], 1));
                // negative pairs
                trainSet.add(new Translator.OCXPair(realInstance, fake[j], 0));
            }
        }

        // train Discriminator
        for (int e = 0; e < epochs; ++e) {
            Collections.shuffle(trainSet);
            for (Translator.OCXPair pair : trainSet) {
                D.train();
                SimpleMatrix S = D.discriminate(pair.OCX1.X, pair.OCX1.origA, pair.OCX2.X, pair.OCX2.origA);
                SimpleMatrix Y = new SimpleMatrix(1, 2);
                Y.set(0, pair.c, 1.0);
                CELArrRef.add(Math.round(D.getCEL(S, Y) * 1000) / 1000.0);
                D.improve(S, Y);
                D.doneTrain();
            }
        }

    }

    /**
     * Train the Generator by using Discriminator's classification in the REINFORCE method
     * @param toBeMutated - the to-be-mutated genome
     * @param isMH - are we training G on memory head or decision head genome
     * @return the reward progression of G training
     */
    public HashMap<Integer, ArrayList<Double>> trainG(Individual toBeMutated, boolean isMH) {

        HashMap<Integer, ArrayList<Double>> rewardProgression = new HashMap<>();
        for (int epoch = 0; epoch < epochs; ++epoch) {

            double epsl = epsilon;

            // transcribe genome as adj matrix and feature matrix
            Translator.OCX ocx = translator.translate(toBeMutated, isMH);
            SimpleMatrix X = ocx.X; // is to be changed
            SimpleMatrix mutatedA = ocx.origA; // no genome parsing would happen so no need copy
            TimeStream stream = new TimeStream(mutatedA);

            // extract highest IN for proximal changes in this context
            int topIN = isMH ? genePool.getMH_cap() : genePool.getDH_cap();
            G.train();
            ArrayList<Double> rewards = new ArrayList<>();
            for (int t = 0; t < tryIterations; ++t) {

                if (epsl > epsilonMin) epsl *= epsilonDecay;
                stream.prematureAdd(G.generate(t, X, mutatedA));

                int maxI = 0;
                double maxP = stream.peek().getAProbs()[0];
                for (int i = 1; i < stream.peek().getAProbs().length; ++i) {
                    if (stream.peek().getAProbs()[i] > maxP) {
                        maxI = i;
                        maxP = stream.peek().getAProbs()[i];
                    }
                }
//                System.out.print("a choose " + maxI + " with prob=" + Math.round(maxP*100));
                maxI = 0;
                maxP = stream.peek().getBProbs()[0];
                for (int i = 1; i < stream.peek().getBProbs().length; ++i) {
                    if (stream.peek().getBProbs()[i] > maxP) {
                        maxI = i;
                        maxP = stream.peek().getBProbs()[i];
                    }
                }
//                System.out.print(". b choose " + maxI + " with prob=" + Math.round(maxP*100) + "\n");

                int A_IN = Math.random() < epsl ? (int) (Math.random() * nodeIndices) : sample(stream.peek().getAProbs());
                int B_IN = Math.random() < epsl ? (int) (Math.random() * nodeIndices) : sample(stream.peek().getBProbs());

                stream.peek().setSelected(A_IN, B_IN);

                // start reward accumulation
                double reward = 0.0;

                // at the last iteration or zero is sampled, no further mutation is needed
                if (A_IN == 0 || B_IN == 0 || t == tryIterations - 1) {
                    // collect reward signal from the Discriminator
                    reward += runD(X, mutatedA, isMH) ? discriminatorAccept : discriminatorReject;
                    stream.peek().setReward(reward);
                    rewards.add(reward);
                    break;
                }

                // check validity of selected node
                boolean invalid = (mutatedA.get(A_IN, A_IN) == 0.0) || (mutatedA.get(B_IN, B_IN) == 0.0) ||
                        (Math.abs(X.get(A_IN, X_INDEX) - X.get(B_IN, X_INDEX)) < 1e-10);

                if (invalid) {
                    reward += invalidMutation;
                    stream.peek().setReward(reward);
                    rewards.add(reward);
                    continue; // no changes is made so same adj matrix
                }

                reward += validMutation;

                // update adj matrix
                if (X.get(A_IN, X_INDEX) > X.get(B_IN, X_INDEX)) { // we want A-to-B
                    int temp = A_IN; A_IN = B_IN; B_IN = temp;
                }

                // case where no connection A-to-B, add it
                if (mutatedA.get(A_IN, B_IN) == 0.0) mutatedA.set(A_IN, B_IN, 1.0);
                else { // case where a connection exists, try add new node
                    if (topIN == MAX_NODE) reward += exceedMaxNode;
                    else {
                        if (nodeInBetween(A_IN, B_IN, X) != -1) reward += repeatedNodeAddition;
                        else {
                            ConGene conGene = isMH ?
                                    new MHcg(new MHng(A_IN), new MHng(B_IN), 'p') :
                                    new DHcg(new DHng(A_IN), new DHng(B_IN), 'p');
                            GenePool.NodeType type = isMH ? GenePool.NodeType.MH : GenePool.NodeType.DH;
                            int proposedIN = genePool.getBetweenNodeIN(conGene, type); // this creates a slight error because we won't update gene pool
                            if (proposedIN == -1) addGNode(++topIN, A_IN, B_IN, X, mutatedA, isMH);
                            else addGNode(proposedIN, A_IN, B_IN, X, mutatedA, isMH);
                        }
                    }
                }

                stream.overrideA(mutatedA);
                stream.peek().setReward(reward);
                rewards.add(reward);

            }

            rewardProgression.put(epoch, rewards);

            // train the Generator
            SimpleMatrix dL_dH = new SimpleMatrix(1, codeDim);
            stream.shave();
            int T = stream.size();
            for (int t = T - 1; t >= 0; --t) {
                TimeStep unit = stream.get(t);
                // compute monte-carlo discounted reward
                double R = 0.0;
                for (int i = t; i < T; ++i) R += Math.pow(gamma, i - t) * stream.get(i).getReward();
                // compute policy gradient
                double[] aProbs = unit.getAProbs();
                double[] bProbs = unit.getBProbs();
                double[][] dL_dAZ = new double[1][nodeIndices];
                double[][] dL_dBZ = new double[1][nodeIndices];
                // use log trick for softmax derivative
                for (int i = 0, ac = unit.getSelectedA(), bc = unit.getSelectedB(); i < nodeIndices; ++i) {
                    // negative gradient because of gradient descend in dense layers
                    dL_dAZ[0][i] = -R * ((i != ac) ? (-aProbs[i]) : (1 - aProbs[ac]));
                    dL_dBZ[0][i] = -R * ((i != bc) ? (-bProbs[i]) : (1 - bProbs[bc]));
                }
                dL_dH = G.improveThroughTime(t, new SimpleMatrix(dL_dAZ), new SimpleMatrix(dL_dBZ), dL_dH);
            }
            G.doneTrain();

        }

        return rewardProgression;

    }

    private int nodeInBetween(int A_IN, int B_IN, SimpleMatrix X) {
        for (int IN = 0; IN < nodeIndices; ++IN)
            if ((int) X.get(IN, FROM_INDEX) == A_IN && (int) X.get(IN, TO_INDEX) == B_IN)
                return IN;
        return -1;
    }

    /**
     * After G is trained, it can be used to mutate an individual. D will tell if
     * the complete mutation is good or not. If not, the mutation would not be applied
     * @param toBeMutated - the individual to be mutated
     * @param isMH - are we mutating the memory head or the decision head
     */
    public void GMutate(Individual toBeMutated, boolean isMH) {

        Translator.OCX ocx = translator.translate(toBeMutated, isMH);
        SimpleMatrix X = ocx.X;
        SimpleMatrix A = ocx.origA.copy(); // copy because genome parsing needs original A unchanged

        int topIN = isMH ? genePool.getMH_cap() : genePool.getDH_cap(); // extract highest IN for proximal changes
        int[] checksum = new int[] {0, 0, 0};

        G.train(); // test
        for (int t = 0; t < tryIterations; ++t) {
            int[] result = runG(t, X, A, topIN, isMH);
            checksum[0] += result[0];
            checksum[1] += result[1];
            topIN = result[2];
        }
        G.doneTrain(); // no training happen

        // if accepted by the Discriminator, the mutated adj matrix is parsed onto the genome
        if (runD(X, A, isMH)) {
//            System.out.println("accept mutation, parsing onto " + (isMH ? "MH." : "DH."));
            ocx.changedA = A;
            if (isMH) translator.parseMH(toBeMutated.getMH(), checksum, ocx);
            else translator.parseDH(toBeMutated.getDH(), checksum, ocx);
        }
//        else System.out.println("reject mutation onto " + (isMH ? "MH." : "DH."));

    }

    /**
     * Run Generator on feature matrix X and adjacency matrix A.
     * This method will change referenced matrices in-place
     * IMPORTANT: training of G must be invoked at the caller's level in order
     * for correct internal state caching
     * @param time - the time step of this invocation
     * @param X - feature matrix
     * @param A - adjacency matrix
     * @param topIN - max IN
     * @param isMH - are we letting Generator decides on memory head or decision head
     * @return number of connections and nodes that has been added, and updated top IN
     */
    private int[] runG(int time, SimpleMatrix X, SimpleMatrix A, int topIN, boolean isMH) {

        TimeStep unit = G.generate(time, X, A); // because latent GRU starts at time -1
        int A_IN = sample(unit.getAProbs());
        int B_IN = sample(unit.getBProbs());

        // check validity of selected endpoints
        if (A_IN == 0 || B_IN == 0 || Math.abs(X.get(A_IN, X_INDEX) - X.get(B_IN, X_INDEX)) < 1e-10 ||
                A.get(A_IN, A_IN) == 0.0 || A.get(B_IN, B_IN) == 0.0)
            return new int[] {0, 0, topIN};

        // reverse order if necessary because we need A-to-B
        if (X.get(A_IN, X_INDEX) > X.get(B_IN, X_INDEX)) {
            int temp = A_IN; A_IN = B_IN; B_IN = temp;
        }

        // check validity if a new node is added
        if ((A.get(A_IN, B_IN) != 0.0 && topIN == MAX_NODE) ||
                (A.get(A_IN, B_IN) != 0.0 && nodeInBetween(A_IN, B_IN, X) != -1))
            return new int[] {0, 0, topIN};

        // case: no connection A-to-B, add it
        if (A.get(A_IN, B_IN) == 0.0) {
            A.set(A_IN, B_IN, 1.0);
            return new int[] {1, 0, topIN};
        }
        // case: a connection A-to-B exists, add new node
        ConGene conGene = isMH ?
                new MHcg(new MHng(A_IN), new MHng(B_IN), 'p') :
                new DHcg(new DHng(A_IN), new DHng(B_IN), 'p');
        GenePool.NodeType type = isMH ? GenePool.NodeType.MH : GenePool.NodeType.DH;
        int proposedIN = genePool.getBetweenNodeIN(conGene, type); // this creates a slight error because we won't update gene pool
        if (proposedIN == -1) addGNode(++topIN, A_IN, B_IN, X, A, isMH);
        else addGNode(proposedIN, A_IN, B_IN, X, A, isMH);
        return new int[] {2, 1, topIN};

    }

    /**
     * This method will change referenced matrices in-place
     */
    private void addGNode(int IN, int A_IN, int B_IN, SimpleMatrix X, SimpleMatrix A, boolean isMH) {
        // update node features matrix
        X.set(IN, IN_INDEX, IN);
        X.set(IN, FROM_INDEX, A_IN);
        X.set(IN, TO_INDEX, B_IN);
        X.set(IN, X_INDEX, (X.get(A_IN, X_INDEX) + X.get(B_IN, X_INDEX)) / 2.0);
        X.set(IN, Y_INDEX, (X.get(A_IN, Y_INDEX) + X.get(B_IN, Y_INDEX)) / 2.0);
        X.set(IN, TYPE_INDEX, isMH ? MH_CODE : DH_CODE);
        // add self-loop to denote existence of the new node AND for GCN self-feature consideration
        A.set(IN, IN, 1.0);
        // there is now a connection from A to new node and new node to B
        A.set(A_IN, IN, 1.0);
        A.set(IN, B_IN, 1.0);
    }

    /**
     * Generate a voting count by using D on the genome and every top architectures
     * The vote that is equal or greater than half of the total will win
     * @param X - the feature matrix of the genome
     * @param A - the adjacency matrix of the genome
     * @param isMH - are we considering memory head or decision head
     * @return true if votes agree with the genome being a top architecture, false if not
     */
    private boolean runD(SimpleMatrix X, SimpleMatrix A, boolean isMH) {

        D.train(); // we don't actually train D, just to prepare caches
        int agreeVotes = 0;
        for (Translator.OCX TOCX : (isMH ? realMH : realDH)) {
            SimpleMatrix classification = D.discriminate(TOCX.X, TOCX.origA, X, A);
//            System.out.print("D: ");
//            for (double d : classification.getDDRM().getData()) System.out.print(Math.round(d*100) + " ");
//            System.out.println();
            if (sample(classification.getDDRM().getData()) == 1) agreeVotes++;
        }
        D.doneTrain(); // no training happen

        return (agreeVotes >= K / 2.0);

    }

    private int sample(double[] probs) {
        double p = Math.random(), cumulative = 0.0;
        for (int i = 0, n = probs.length; i < n; ++i) {
            cumulative += probs[i];
            if (p < cumulative) return i;
        }
        throw new IllegalStateException("Invalid probability distribution to sample from.");
    }

}
