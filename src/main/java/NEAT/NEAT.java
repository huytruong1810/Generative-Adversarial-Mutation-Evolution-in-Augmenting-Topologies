package NEAT;

import Environment.Controllers.Utils;
import NEAT.DataStructures.AncestorTree;
import NEAT.DataStructures.SecuredList;
import NEAT.DataStructures.Ranker;
import NEAT.Genome.DecisionHead.DHg;
import NEAT.Genome.DecisionHead.DHng;
import NEAT.Genome.MemoryHead.MHg;
import NEAT.Genome.MemoryHead.MHng;

import static NEAT.GenePool.*;
import static NEAT.Lambdas.Graphics.*;

import java.util.*;

public class NEAT {

    /**
     * This is a central control class, it should not be initialized by run-time processes
     */
    private NEAT() { }

    /**-----------------------------------------------------------------------------------------------------------------
     * NEAT hyper-parameters
     */
    public static int numTrEps, numTeEps, worldDim, timeHorizon;
    public static int[] obsII, infII, actorOI, criticOI, seerOI;
    public static char[][][] bluePrint;
    public static final int NULL_SPECIES = -1, IMPOSSIBLE_VAL = (int) Double.NEGATIVE_INFINITY;
    public static final double inputNodeX = 15, outputNodeX = 1325, hiddenNodeX = (inputNodeX + outputNodeX) / 2,

                               GAMMA = 0.98, // discount factor in reward aggregation, 0.0 (immediate reward) - 1.0 (planing)
                               LAMBDA = 0.92, // TD(lambda), 0.0 (is TD[0]) - 1.0 (is Monte-Carlo)

                               C1 = 1.6, C2 = 1.8, // genomic distance const: excess and disjoint
                               compThres = 2, // max distance to be consider the same species
                               evictRate = 0.2, // percentage of eviction from the population
                               extinctThres = 5; // minimum number of individuals to maintain a species existence

    /**-----------------------------------------------------------------------------------------------------------------
     * NEAT ancestry archive
     */
    private static GenePool genePool;
    private static Incubator incubator;
    private static Mutator mutator;

    private static SecuredList<Individual> generalPopulation;
    private static SecuredList<Species> ecosystem;
    private static AncestorTree ancestorTree;

    public static List<Individual> listPopulation() { return generalPopulation.getData(); }
    public static List<Species> listEcosystem() { return ecosystem.getData(); }
    public static AncestorTree getAncestorTree() { return ancestorTree; }

    public static double getAvgScore() {
        double sum = 0.0;
        int count = 0;
        for (Individual i : listPopulation()) {
            double score = i.getScore();
            if (Double.isFinite(score)) {
                sum += score;
                count++;
            }
        }
        return sum / count;
    }

    /**-----------------------------------------------------------------------------------------------------------------
     * Set up the NEAT class with population specifications
     * and shared train/test data
     * @param worldSize - the dimension of grid world
     * @param timeSteps - the number of time units in each episode
     * @param bp - the environment blueprint
     * @param observationIndices - the indices of observation input nodes
     * @param inflectionIndices - the indices of inflection input nodes
     * @param numHidden - number of hidden nodes
     * @param actorIndices - the indices of actor output nodes
     * @param criticIndices - the indices of critic output nodes
     * @param seerIndices - the indices of seer output nodes
     * @param maxPop - max population
     * @param trEps - number of training episodes
     * @param teEps - number of testing episodes
     */
    public static void setUp(int worldSize, int timeSteps, char[][][] bp,
                             int[] observationIndices, int[] inflectionIndices, int numHidden,
                             int[] actorIndices, int[] criticIndices, int[] seerIndices,
                             int maxPop, int trEps, int teEps) {

        worldDim = worldSize;
        timeHorizon = timeSteps;

        obsII = observationIndices;
        infII = inflectionIndices;
        actorOI = actorIndices;
        criticOI = criticIndices;
        seerOI = seerIndices;

        genePool = new GenePool();
        incubator = new Incubator();
        mutator = new Mutator(genePool);

        generalPopulation = new SecuredList<>();
        ecosystem = new SecuredList<>();
        ancestorTree = new AncestorTree();

        bluePrint = bp;
        numTrEps = trEps;
        numTeEps = teEps;

        reset(obsII.length + infII.length, numHidden,
                actorOI.length + criticOI.length + seerOI.length, maxPop);

    }

    private static boolean contains(int[] arr, int i) {
        for (int index : arr) if (index == i) return true;
        return false;
    }

    /**
     * Reset and initialize the population including the initial gene pool
     * @param numInput - number of input nodes
     * @param numHidden - number of hidden nodes
     * @param numOutput - number of output nodes
     * @param maxPop - max number of individual in a population
     */
    private static void reset(int numInput, int numHidden, int numOutput, int maxPop) {

        genePool.clear();
        ecosystem.clear();
        generalPopulation.clear();

        int inputN = numInput + numHidden;

        // set up common ancestries (the input-hidden-output frame)
        // we ignore the returned node here
        for (int i = 0; i < inputN; ++i) {
            double y = initY.apply(i, inputN);
            if (contains(obsII, i)) OBS_NODES_Y.add(y);
            else if (contains(infII, i)) INF_NODES_Y.add(y);
            genePool.logNode(-1, inputNodeX, y, NodeType.MRU, true); // MRU: 1..(inN + hidN)  innovation record
        }
        for (int i = 0; i < numHidden; ++i) {
            double y = initY.apply(i, numHidden);
            genePool.logNode(-1, hiddenNodeX, y, NodeType.MRU, true); // MRU: (inN + hidN + 1)..(inN + 2 * hidN) innovation record
            genePool.logNode(-1, hiddenNodeX, y, NodeType.ACU, true); // ACU: 1..hidN innovation record
        }
        for (int i = 0; i < numOutput; ++i) {
            double y = initY.apply(i, numOutput);
            if (contains(criticOI, i)) CRITIC_NODES_Y.add(y);
            else if (contains(seerOI, i)) SEER_NODES_Y.add(y);
            genePool.logNode(-1, outputNodeX, y, NodeType.ACU, true); // ACU: (hidN + 1)..(hidN + outN) innovation record
        }

        // initial population
        for (int i = 0; i < maxPop; ++i) {

            MHg mru = new MHg(inputN, numHidden);
            DHg acu = new DHg(numHidden, numOutput);

            SecuredList<MHng> mrunodes = mru.getNodes();
            SecuredList<DHng> acunodes = acu.getNodes();

            // build base frame by inserting base ancestors
            for (int j = 0; j < (inputN + numHidden); ++j) mrunodes.add((MHng) genePool.logNode(j, 0, 0, NodeType.MRU, true));
            for (int j = 0; j < (numHidden + numOutput); ++j) acunodes.add((DHng) genePool.logNode(j, 0, 0, NodeType.ACU, true));

            Individual individual = new Individual(mru, acu);
            individual.express(); // express the initial genomes

            generalPopulation.add(individual); // none have a species yet

        }

    }

    /**-----------------------------------------------------------------------------------------------------------------
     * Evolve the general population. This includes:
     * - Elect a representative for each species
     * - Assign each individual into appropriate species
     * - Let the population complete within their species
     * - Evict low performance individual
     * - Remove extinct species
     * - Generate offsprings to fill in population
     */
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
            if (bestComp < compThres) bestFit.recruit(i);
            else { // no good fit, branch new species
                Species p = i.getParentSpecies(); // this new species deviates from the original one
                Species newSpecies = new Species(i, (p != null) ? p.getID() : NULL_SPECIES);
                ecosystem.add(newSpecies);
                try { // ancestor tree does cloning of representative to save structural changes
                    ancestorTree.add(p, newSpecies);
                }
                catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
            }
        }

        // species regulation

        // sample a backup blueprint in case the universal blueprint is null (random mode)
        char[][][] backupBlueprint = Utils.makeBlueprint(5, true, true, 2, 1, null, null);

        Ranker<Species> ranker = new Ranker<>();
        for (int i = 0, n = ecosystem.size(); i < n; ++i) {
            Species s = ecosystem.get(i);
            // every individual is trained and tested on a unified blueprint
            s.compete(Objects.requireNonNullElse(bluePrint, backupBlueprint)); // also compute species score for ranker
            s.evict(); // evict low performance individuals (these could include representatives)
            if (s.size() <= extinctThres) { // if species population get too low
                s.extinct();
                ecosystem.remove(s); // remove it
                ancestorTree.stop(s);
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
                mutator.mutateStructure(i);
                i.express(); // express the individual once after its birth due to structural mutation
            }
        } // newborn individuals are species-unassigned [alive]

    } // after this step, a bunch of individuals will have no species, they are the newborns

}
