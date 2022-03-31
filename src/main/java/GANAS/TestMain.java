package GANAS;

import GANAS.BaseArchitectures.GCN;
import NEAT.DataStructures.Ranker;
import RL.Controllers.Utils;
import NEAT.*;
import NEAT.DataStructures.SecuredList;
import NEAT.Genome.DecisionHead.DHg;
import NEAT.Genome.DecisionHead.DHng;
import NEAT.Genome.MemoryHead.MHg;
import NEAT.Genome.MemoryHead.MHng;
import org.ejml.simple.SimpleMatrix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import static NEAT.Lambdas.Graphics.*;
import static NEAT.NEAT.*;

public class TestMain {

    static SecuredList<Individual> generalPopulation = new SecuredList<>();
    static SecuredList<Species> ecosystem = new SecuredList<>();
    static final GenePool genePool = new GenePool();
    static final Incubator incubator = new Incubator();

//    public static void main(String[] args) throws CloneNotSupportedException {
//
//        obsII = new int[] {0, 1, 2, 3, 4};
//        infII = new int[] {5, 6};
//        actorOI = new int[] {0, 1, 2, 3, 4, 5};
//        criticOI = new int[] {6};
//        seerOI = new int[] {7, 8, 9, 10, 11};
//        bluePrint = Utils.makeBlueprint(5, true, true, 2, 1, null, null);
//
//        Scanner scanner = new Scanner(System.in);
//
//        reset();
//
//        System.out.println("Enter: ");
//        while (!scanner.nextLine().equals("q")) {
//            evolve();
//            System.out.println("\nEnter: ");
//        }
//
//    }

//    public static void main(String[] args) {
//
//        Discriminator discriminator = new Discriminator(1, 8, 16, 5, 2, 0.001);
//
//        ArrayList<SimpleMatrix> graphs = new ArrayList<>() {{
//            add(new SimpleMatrix(new double[][] { // true
//                    //            A  B  C  D  E
//                    new double[] {1, 1, 0, 0, 0}, // A
//                    new double[] {0, 1, 1, 0, 0}, // B
//                    new double[] {0, 0, 1, 0, 0}, // C
//                    new double[] {1, 0, 0, 1, 0}, // D
//                    new double[] {0, 0, 0, 1, 1}  // E
//            }));
//            add(new SimpleMatrix(new double[][] { // true
//                    //            A  B  C  D  E
//                    new double[] {1, 1, 0, 0, 0}, // A
//                    new double[] {0, 1, 1, 0, 0}, // B
//                    new double[] {0, 0, 1, 0, 0}, // C
//                    new double[] {1, 0, 0, 1, 0}, // D
//                    new double[] {0, 1, 0, 1, 1}  // E
//            }));
//            add(new SimpleMatrix(new double[][] { // false
//                    //            A  B  C  D  E
//                    new double[] {1, 1, 0, 0, 1}, // A
//                    new double[] {0, 1, 1, 0, 0}, // B
//                    new double[] {0, 0, 1, 0, 0}, // C
//                    new double[] {1, 0, 0, 1, 0}, // D
//                    new double[] {0, 0, 0, 1, 1}  // E
//            }));
//            add(new SimpleMatrix(new double[][] { // false
//                    //            A  B  C  D  E
//                    new double[] {1, 1, 0, 0, 0}, // A
//                    new double[] {0, 1, 1, 0, 1}, // B
//                    new double[] {0, 0, 1, 0, 0}, // C
//                    new double[] {1, 0, 0, 1, 0}, // D
//                    new double[] {0, 0, 0, 1, 1}  // E
//            }));
//        }};
//        ArrayList<int[]> pairs = new ArrayList<>() {{
//            add(new int[]{0,0,1});
//            add(new int[]{0,1,1});
//            add(new int[]{0,2,0});
//            add(new int[]{0,3,0});
//            add(new int[]{1,0,1});
//            add(new int[]{1,1,1});
//            add(new int[]{1,2,0});
//            add(new int[]{1,3,0});
//            add(new int[]{2,0,0});
//            add(new int[]{2,1,0});
//            add(new int[]{2,2,1});
//            add(new int[]{2,3,1});
//            add(new int[]{3,0,0});
//            add(new int[]{3,1,0});
//            add(new int[]{3,2,1});
//            add(new int[]{3,3,1});
//        }};
//        SimpleMatrix X = new SimpleMatrix(new double[][] {
//                new double[] {1},
//                new double[] {2},
//                new double[] {3},
//                new double[] {4},
//                new double[] {5}
//        });
//
//        for (int i = 0; i < 50000; ++i) {
//            discriminator.train();
//            double MSE = 0;
//            Collections.shuffle(pairs);
//            for (int[] pair : pairs) {
//                SimpleMatrix TA = graphs.get(pair[0]);
//                SimpleMatrix OA = graphs.get(pair[1]);
//                SimpleMatrix Y_hat = discriminator.discriminate(X, TA, X, OA);
//                SimpleMatrix Y = new SimpleMatrix(1, 2);
//                Y.set(0, pair[2], 1.0);
//                MSE += Y.minus(Y_hat).elementPower(2).elementSum();
//                discriminator.improve(Y_hat, Y);
//            }
//            System.out.println(MSE);
//            discriminator.doneTrain();
//        }
//
//        System.out.println("\n-------------");
//        for (int[] pair : pairs) {
//            discriminator.train();
//            SimpleMatrix TA = graphs.get(pair[0]);
//            SimpleMatrix OA = graphs.get(pair[1]);
//            SimpleMatrix Y_hat = discriminator.discriminate(X, TA, X, OA);
//            System.out.println("should be " + pair[2] + "\n" + Y_hat + "\n-------------");
//            discriminator.doneTrain();
//        }
//
//
//    }

//    public static void main(String[] args) {
//
//        Generator generator = new Generator(1, 16, 32, 5 + 1, 1, 0.0001);
//
//        SimpleMatrix initialA = new SimpleMatrix(new double[][]{
//                //              1  2  3  4  5
//                new double[]{0, 0, 0, 0, 0, 0},
//                new double[]{0, 1, 0, 0, 0, 0}, // 1
//                new double[]{0, 0, 1, 0, 0, 0}, // 2
//                new double[]{0, 0, 0, 1, 0, 0}, // 3
//                new double[]{0, 0, 0, 0, 1, 0}, // 4
//                new double[]{0, 0, 0, 0, 0, 0}  // 5
//        });
//        SimpleMatrix finalA = new SimpleMatrix(new double[][]{
//                //               1  2  3  4  5
//                new double[] {0, 0, 0, 0, 0, 0},
//                new double[] {0, 1, 0, 1, 0, 1}, // 1
//                new double[] {0, 0, 1, 0, 0, 0}, // 2
//                new double[] {0, 0, 0, 1, 0, 0}, // 3
//                new double[] {0, 0, 0, 0, 1, 0}, // 4
//                new double[] {0, 0, 0, 1, 0, 1}  // 5
//        });
//        SimpleMatrix X = new SimpleMatrix(new double[][] {
//                new double[] {0},
//                new double[] {1},
//                new double[] {2},
//                new double[] {3},
//                new double[] {4},
//                new double[] {-999}
//        });
//
//        ArrayList<Integer> progress = new ArrayList<>();
//        for (int epoch = 0; epoch < 1000; ++epoch) {
//            double epsilon = 1;
//            int topIN = 4;
//            SimpleMatrix A = initialA.copy();
//            GRetStream stream = new GRetStream(A);
//            ArrayList<Integer> rewards = new ArrayList<>();
//            generator.train();
//            for (int t = 0; t < 5; ++t) {
//                epsilon *= 0.2;
//                int r = 0;
//                stream.prematureAdd(generator.generate(t, X, A));
//                double[] aProbs = stream.peek().getAProbs();
//                double[] bProbs = stream.peek().getBProbs();
//                int A_IN = (Math.random() < epsilon) ? (int)(Math.random()*6) : sample(aProbs);
//                int B_IN = (Math.random() < epsilon) ? (int)(Math.random()*6) : sample(bProbs);
//                System.out.print("a prob distribution: ");
//                for (double d : aProbs) System.out.print(Math.round(d*100) + " ");
//                System.out.println("select " + A_IN);
//                System.out.print("b prob distribution: ");
//                for (double d : bProbs) System.out.print(Math.round(d*100) + " ");
//                System.out.println("select " + B_IN);
//                stream.peek().setSelected(A_IN, B_IN);
//                boolean invalid = false;
//                if (A.get(A_IN, A_IN) == 0.0 || A.get(B_IN, B_IN) == 0.0) { r += -1;invalid = true; }
//                if (A_IN == 0 || B_IN == 0 || A_IN == B_IN) { r += -2; invalid = true; }
//                if (invalid) {
//                    if (t == 4) {
//                        r += -distance(A, finalA);
//                    }
//                    stream.peek().setReward(r);
//                    rewards.add(r);
//                    System.out.println("invalid mutation");
//                    continue; // no changes is made so same adj matrix
//                }
//                r += 3;
//                if (A.get(A_IN, B_IN) == 0.0) {
//                    System.out.println("add connection between " + A_IN + " to " + B_IN);
//                    A.set(A_IN, B_IN, 1.0);
//                } else {
//                    if (topIN == 5) {
//                        System.out.println("max node reached");
//                        r += -3;
//                    } else {
//                        System.out.println("add node between " + A_IN + " and " + B_IN);
//                        X.set(++topIN, 0, topIN);
//                        A.set(topIN, topIN, 1.0);
//                        A.set(A_IN, topIN, 1.0);
//                        A.set(topIN, B_IN, 1.0);
//                    }
//                }
//                stream.overrideA(A);
//                if (t == 4) {
//                    r += -distance(A, finalA);
//                }
//                stream.peek().setReward(r);
//                rewards.add(r);
//            }
//            SimpleMatrix dL_dH = new SimpleMatrix(1, 16);
//            stream.shave();
//            int T = stream.size();
//            for (int t = T - 1; t >= 0; --t) {
//                GRetUnit unit = stream.get(t);
//                double R = 0.0;
//                for (int i = t; i < T; ++i) R += Math.pow(0.99, i - t) * stream.get(i).getReward();
//                double[] aProbs = unit.getAProbs();
//                double[] bProbs = unit.getBProbs();
//                double[][] dL_dAZ = new double[1][6];
//                double[][] dL_dBZ = new double[1][6];
//                for (int i = 0, ac = unit.getSelectedA(), bc = unit.getSelectedB(); i < 6; ++i) {
//                    dL_dAZ[0][i] = R * ((i != ac) ? aProbs[i] : (aProbs[i] - 1));
//                    dL_dBZ[0][i] = R * ((i != bc) ? bProbs[i] : (bProbs[i] - 1));
//                }
//                dL_dH = generator.improveThroughTime(t, new SimpleMatrix(dL_dAZ), new SimpleMatrix(dL_dBZ), dL_dH);
//            }
//            generator.doneTrain();
//            int sum = 0;
//            for (int r : rewards) sum += r;
//            System.out.println("epoch " + epoch + ": " + rewards + "\nsum=" + sum);
//            for (int i = 0; i < 6; ++i) {
//                for (int j = 0; j < 6; ++j) System.out.print(A.get(i, j) == 1.0 ? 1 + " " : 0 + " ");
//                System.out.println();
//            }
//            progress.add(sum);
//        }
//        System.out.println(progress);
//
//    }

    private static int distance(SimpleMatrix A, SimpleMatrix B) {
        int d = 0;
        for (int i = 0; i < A.numRows(); ++i) {
            for (int j = 0; j < A.numCols(); ++j) {
                d += Math.abs(A.get(i, j) - B.get(i, j));
            }
        }
        return d;
    }

    private static int sample(double[] probs) {
        double p = Math.random(), cumulative = 0.0;
        for (int i = 0, n = probs.length; i < n; ++i) {
            cumulative += probs[i];
            if (p < cumulative) return i;
        }
        return 0;
    }

    public static void reset() {

        int numInput = 7;
        int numHidden = 8;
        int numOutput = 12;
        int maxPop = 10;

        int inputDim = numInput + numHidden;

        // set up common ancestries (the input-hidden-output frame)
        // we ignore the returned node here
        // IN = -1 to let gene pool initialize these roots and log them
        for (int i = 0; i < inputDim; ++i) {
            double y = initY.apply(i, inputDim);
            // 1..(inN + hidN)  IN record
            genePool.logNode(-1, null, inputNodeX, y, GenePool.NodeType.MH, true);
        }
        for (int i = 0; i < numHidden; ++i) {
            double y = initY.apply(i, numHidden);
            // (inN + hidN + 1)..(inN + 2 x hidN) IN record
            genePool.logNode(-1, null, hiddenNodeX, y, GenePool.NodeType.MH, true);
            // 1..hidN IN record
            genePool.logNode(-1, null, hiddenNodeX, y, GenePool.NodeType.DH, true);
        }
        for (int i = 0; i < numOutput; ++i) {
            double y = initY.apply(i, numOutput);
            // (hidN + 1)..(hidN + outN) IN record
            genePool.logNode(-1, null, outputNodeX, y, GenePool.NodeType.DH, true);
        }

        // initial population
        for (int i = 0; i < maxPop; ++i) {

            MHg mru = new MHg(inputDim, numHidden);
            DHg acu = new DHg(numHidden, numOutput);

            SecuredList<MHng> mrunodes = mru.getNodes();
            SecuredList<DHng> acunodes = acu.getNodes();

            // build base frame by sequentially retrieving base nodes from gene pool, IN starts at 1
            // this is retrieving so no need to provide x and y coordinates
            for (int IN = 1, maxIN = inputDim + numHidden + 1; IN < maxIN; ++IN) mrunodes.add((MHng)
                    genePool.logNode(IN, null, 0, 0, GenePool.NodeType.MH, true));
            for (int IN = 1, maxIN = numHidden + numOutput + 1; IN < maxIN; ++IN) acunodes.add((DHng)
                    genePool.logNode(IN, null, 0, 0, GenePool.NodeType.DH, true));

            Individual individual = new Individual(mru, acu);
            individual.express(); // express the initial genomes

            generalPopulation.add(individual); // none have a species yet

        }

    }

    public static void evolve() throws CloneNotSupportedException {

        // speciate newborn individuals (species-unassigned)
        for (Individual i : generalPopulation.getData()) {
            if (i.getSpecies() != null) continue; // don't care about species-assigned individuals
            double bestComp = Double.POSITIVE_INFINITY;
            Species bestFit = null;
            if (ecosystem.size() > 0) { // check compatibility with all existing species
                for (Species s : ecosystem.getData()) {
                    double compScore = s.calComp(i);
                    if (compScore < bestComp) { bestComp = compScore; bestFit = s; }
                }
            }
            if (bestComp < compThres) bestFit.add(i);
            else { // no good fit, branch new species
                Species p = i.getParentSpecies(); // this new species deviates from the original one
                Species newSpecies = new Species(i, (p != null) ? p.getID() : NULL_SPECIES, genePool);
                ecosystem.add(newSpecies);
            }
        }

        // sample a backup blueprint in case the universal blueprint is null (random mode)
        Ranker<Species> ranker = new Ranker<>();
        for (int i = 0, n = ecosystem.size(); i < n; ++i) {
            Species s = ecosystem.get(i);
            // every individual is trained and tested on a unified blueprint
            s.compete(bluePrint); // also compute species score for ranker
            s.evict(); // evict low performance individuals
            if (s.size() <= extinctThres) { // if species population get too low
                s.extinct();
                ecosystem.remove(s); // remove it
                --n; --i; // update size of ecosystem and iterator position or we would skip the next species
                continue; // extinct species do not reproduce so don't add to ranker
            }
            ranker.add(s, s.getScore());
        } // after this, a bunch of individuals are species-unassigned (dead)

        // generate offspring from random best species and replace evicted individuals
        for (Individual i : generalPopulation.getData()) {
            if (i.getSpecies() == null) { // species-unassigned [dead] individuals
                Species s1 = ranker.bestRandom(); // select best species randomly
                Species s2 = ranker.bestRandom(); // could be same as s1 so asexual reproduction
                i.replaceWith(incubator.crossBreed(s1.getChampion(), s2.getChampion()));
                i.setParentSpecies(s1);
                s1.mutate(i); // note that population of s1 has already been sorted by evict invocation
                i.express();
            }
        } // newborn individuals are species-unassigned [alive]

    }

}
