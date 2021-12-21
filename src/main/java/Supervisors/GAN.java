package Supervisors;

import NEAT.GenePool;
import NEAT.Genome.MemoryHead.MHg;
import NEAT.Individual;
import org.ejml.simple.SimpleMatrix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static NEAT.GenePool.MAX_NODE;
import static Supervisors.GRewardConst.*;

public class GAN {

    public static class DescIndSorter implements Comparator<Individual> {
        @Override
        public int compare(Individual o1, Individual o2) {
            return Double.compare(o2.getScore(), o1.getScore());
        }
    }

    private static final int K = 2;
    private static final int codeDim = 16;
    private static final int epochs = 3;
    private static final int nodeIndices = MAX_NODE + 1; // IN starts at 1
    private static final double gamma = 0.98;
    private static final double alpha = 0.01;

    private final Generator G;
    private final Discriminator D;
    private final GenePool genePool;
    private final Translator translator;
    private final Translator.OCX[] MRU_TrueOCXs;
    private final Translator.OCX[] ACU_TrueOCXs;
    private final DescIndSorter descIndSorter;

    public GAN(GenePool genePoolRef) {

        G = new Generator(3, codeDim, 32, nodeIndices, alpha);
        D = new Discriminator(3, codeDim, 32, nodeIndices, 2, alpha);
        genePool = genePoolRef;
        translator = new Translator(nodeIndices, genePoolRef);
        MRU_TrueOCXs = new Translator.OCX[K];
        ACU_TrueOCXs = new Translator.OCX[K];
        descIndSorter = new DescIndSorter();

    }

    public ArrayList<Double> trainD_MRU(List<Individual> pplt) {

        // collect K amount of true architectures
        pplt.sort(descIndSorter);
        for (Individual individual : pplt) {
            System.out.println(individual);
        }
        for (int i = 0; i < K; ++i) {
            Individual individual = pplt.get(i);
            MRU_TrueOCXs[i] = translator.translateMRU(individual.getMRU());
            ACU_TrueOCXs[i] = translator.translateACU(individual.getACU());
        }

        Translator.OCX ocx = translator.translateMRU(pplt.get(0).getMRU());
        SimpleMatrix X = ocx.X;
        SimpleMatrix A = ocx.origA;
        int topIN = genePool.getMRU_cap();

        // generate K amount of G-mutations
        int k = 0;
        Translator.OCX[] MRU_GenOCXs = new Translator.OCX[K];

        G.train(); // we don't actually train G, just to prepare caches
        while (k < K) {

            GRetUnit unit = G.generate(0, X, A); // because latent GRU starts at time -1
            int A_IN = sample(unit.getAProbs());
            int B_IN = sample(unit.getBProbs());
            double AX = X.get(A_IN, 1);
            double BX = X.get(B_IN, 1);

            if (A_IN == 0 || B_IN == 0 || AX == BX || A.get(A_IN, A_IN) == 0.0 || A.get(B_IN, B_IN) == 0.0 ||
                    (A.get(A_IN, B_IN) != 0.0 && topIN == MAX_NODE)) // have to add new node but max node is reached
                continue; // invalid G-mutations so keep sampling from generator to get k-th example

            // case where no connection A-to-B, add it
            if (A.get(A_IN, B_IN) == 0.0) A.set(A_IN, B_IN, 1.0);
            else { // case where a connection exists and max node is not reached, add new node
                // update node features matrix
                X.set(++topIN, 0, topIN);
                X.set(topIN, 1, (AX + BX) / 2);
                X.set(topIN, 2, (X.get(A_IN, 2) + X.get(B_IN, 2)) / 2);
                // add self-loop to denote existence of the new node AND for GCN self-feature consideration
                A.set(topIN, topIN, 1.0);
                // there is now a connection from A to new node and new node to B
                A.set(A_IN, topIN, 1.0);
                A.set(topIN, B_IN, 1.0);
            }

            MRU_GenOCXs[k++] = new Translator.OCX(A.copy(), X.copy());

        }
        G.doneTrain(); // clear caches, no training happened

        ArrayList<Translator.OCXPair> trainSet = new ArrayList<>();
        for (int i = 0; i < K; ++i) {
            Translator.OCX trueOCX = MRU_TrueOCXs[i];
            for (int j = 0; j < K; ++j) {
                // add positive pairs
                if (j != i) trainSet.add(new Translator.OCXPair(trueOCX, MRU_TrueOCXs[j], 1));
                // add negative pairs
                trainSet.add(new Translator.OCXPair(trueOCX, MRU_GenOCXs[j], 0));
            }
        }

        ArrayList<Double> CELs = new ArrayList<>();
        for (int e = 0; e < epochs; ++e) {
            Collections.shuffle(trainSet);
            for (Translator.OCXPair pair : trainSet) {
                D.train();
                SimpleMatrix S = D.discriminate(pair.OCX1.X, pair.OCX1.origA, pair.OCX2.X, pair.OCX2.origA);
                SimpleMatrix Y = new SimpleMatrix(1, 2);
                Y.set(0, pair.c, 1.0);
                CELs.add(D.getCEL(S, Y));
                D.improve(S, Y);
                D.doneTrain();
            }
        }

        return CELs;

    }

    public void generateGMutation_MRU(int maxIter, MHg MRU) {

        // transcribe genome as adj matrix and feature matrix
        Translator.OCX ocx = translator.translateMRU(MRU);
        SimpleMatrix X = ocx.X; // is to be changed

        System.out.println("X:\n"+X);

        SimpleMatrix mutatedA = ocx.origA.copy();
        GRetStream stream = new GRetStream(mutatedA);

        int topIN = genePool.getMRU_cap(); // extract highest IN in MRU for proximal changes
        int numMutatedCons = 0; // checksum

        G.train();

        for (int t = 0; t < maxIter; ++t) {

            int GReward = 0;

            stream.prematureAdd(G.generate(t, X, mutatedA));

            double[] aProbs = stream.peek().getAProbs();
            double[] bProbs = stream.peek().getBProbs();

            System.out.println("a prob distribution:\n");
            for (double d : aProbs) System.out.print(d + " ");
            System.out.println();

            System.out.println("b prob distribution:\n");
            for (double d : bProbs) System.out.print(d + " ");
            System.out.println();

            int A_IN = sample(aProbs);
            int B_IN = sample(bProbs);

            stream.peek().setSelected(A_IN, B_IN);

            if (A_IN == 0 || B_IN == 0) {
                stream.peek().setReward(GReward);
                break; // done if a zero is sampled
            }

            // check validity of selected node
            boolean invalid = false;
            double AX = X.get(A_IN, 1);
            double BX = X.get(B_IN, 1);

            if (mutatedA.get(A_IN, A_IN) == 0.0) { GReward += nonExistNode; invalid = true; }
            if (mutatedA.get(B_IN, B_IN) == 0.0) { GReward += nonExistNode; invalid = true; }
            if (AX == BX) { GReward += cycleDetected; invalid = true; }

            if (invalid) {
                stream.peek().setReward(GReward);
                continue; // no changes is made so same adj matrix
            }

            // update adj matrix
            System.out.println("A:\n"+mutatedA);
            if (AX > BX) { // because we work with A-to-B
                int temp = A_IN;
                A_IN = B_IN;
                B_IN = temp;
            }

            // case where no connection A-to-B, add it
            if (mutatedA.get(A_IN, B_IN) == 0.0) {
                numMutatedCons++;
                mutatedA.set(A_IN, B_IN, 1.0);
            } else { // case where a connection exists, try add new node
                if (topIN == MAX_NODE) GReward += exceedMaxNode;
                else {
                    // update features matrix
                    X.set(++topIN, 0, topIN);
                    X.set(topIN, 1, (AX + BX) / 2);
                    X.set(topIN, 2, (X.get(A_IN, 2) + X.get(B_IN, 2)) / 2);
                    // add self-loop to denote existence of the new node AND for GCN self-feature consideration
                    mutatedA.set(topIN, topIN, 1.0);
                    // there is now a connection from A to new node and new node to B
                    mutatedA.set(A_IN, topIN, 1.0);
                    mutatedA.set(topIN, B_IN, 1.0);
                }
            }

            stream.overrideA(mutatedA);

            // calculate reward signal from the discriminator
            GReward += isDReject(X, mutatedA, true) ? discriminatorReject : discriminatorAccept;

            stream.peek().setReward(GReward);

        }

        // train the generator
        SimpleMatrix dL_dH = new SimpleMatrix(nodeIndices, codeDim);
        int T = stream.size();
        for (int t = T - 1; t >= 0; --t) {
            GRetUnit unit = stream.get(t);
            double R = 0.0;
            for (int i = t; i < T; ++i) R += Math.pow(gamma, i - t) * stream.get(i).getReward();
            double[] aProbs = unit.getAProbs();
            double[] bProbs = unit.getBProbs();
            int ac = unit.getSelectedA(), bc = unit.getSelectedB();
            double[][] dL_dAZ = new double[1][nodeIndices];
            double[][] dL_dBZ = new double[1][nodeIndices];
            for (int i = 0; i < nodeIndices; ++i) {
                dL_dAZ[0][i] = R * ((i != ac) ? -aProbs[i] : (1 - aProbs[i]));
                dL_dBZ[0][i] = R * ((i != bc) ? -bProbs[i] : (1 - bProbs[i]));
            }
            dL_dH = G.improveThroughTime(t, new SimpleMatrix(dL_dAZ), new SimpleMatrix(dL_dBZ), dL_dH);
        }
        G.doneTrain();

        // if accepted by the discriminator, the mutated adj matrix is parsed onto the genome
        if (!isDReject(X, mutatedA, true)) {
            ocx.changedA = mutatedA;
            translator.parseMRU(MRU, numMutatedCons, ocx);
        }

    }

    private boolean isDReject(SimpleMatrix X, SimpleMatrix A, boolean isMRU) {

        D.train(); // we don't actually train D, just to prepare caches
        int agreeVotes = 0;
        for (Translator.OCX TOCX : (isMRU ? MRU_TrueOCXs : ACU_TrueOCXs)) {
            SimpleMatrix classification = D.discriminate(TOCX.X, TOCX.origA, X, A);
            System.out.println("classification:\n");
            for (double d : classification.getDDRM().getData()) System.out.print(d + " ");
            System.out.println();
            if (sample(classification.getDDRM().getData()) == 1) agreeVotes++;
        }
        D.doneTrain(); // clear caches, no training happened

        return (agreeVotes < K / 2.0);

    }

    private int sample(double[] probs) {
        double p = Math.random(), cumulative = 0.0;
        for (int i = 0, n = probs.length; i < n; ++i) {
            cumulative += probs[i];
            if (p < cumulative) return i;
        }
        return 0;
    }

}
