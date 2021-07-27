package NEAT;

import Environment.Controllers.Utils;
import Environment.Simulators.EnsembleRoom;
import Environment.Simulators.TestRoom;
import Environment.Simulators.TrainRoom;
import NEAT.DataStructures.AncestorTree;
import NEAT.DataStructures.GeneSet;
import NEAT.DataStructures.Breeder;
import NEAT.Genome.ACU.ACUConGene;
import NEAT.Genome.ACU.ACUGenome;
import NEAT.Genome.ACU.ACUNodeGene;
import NEAT.Genome.MRU.MRUConGene;
import NEAT.Genome.MRU.MRUGenome;
import NEAT.Genome.MRU.MRUNodeGene;

import java.util.*;

public class NEAT {

    /**
     * This is a central control class, it should not be initialized by run-time processes
     */
    private NEAT() { }

    /**-----------------------------------------------------------------------------------------------------------------
     * NEAT hyper-parameters
     */
    public static int numTrEps, numTeEps;
    public static char[][][] bluePrint;
    public static TrainRoom trainSimulator;
    public static TestRoom testSimulator;
    public static EnsembleRoom ensembleSimulator;
    public static final int MAX_NODE = (int) Math.pow(2, 20), MUTATE_ATTEMPTS = 5, NULL_SPECIES = -1;
    public static final double inputNodeX = 15, outputNodeX = 1325, hiddenNodeX = (inputNodeX + outputNodeX) / 2,

                               MRU_P_MAC = 0.7, ACU_P_MAC = 0.6, // Prob(add connection mutation)                      //DEBUG
                               MRU_P_MAN = 0.2, ACU_P_MAN = 0.1, // Prob(add node mutation)                           //DEBUG
                               MRU_P_MW = 0.3, ACU_P_MW = 0.8, // Prob(weight mutation)
                               MRU_P_MWS = 0.7, ACU_P_MWS = 0.7, // Prob(weight shift chance) -> Prob(random initialize) = 1 - P(MWS)
                               MRU_P_MFE = 0.5, ACU_P_MFE = 0.5, // Prob(enable flip mutation)
                               P_DW = 0.75, // Prob(inherited disable flip)
                               MRU_WSS = 1, ACU_WSS = 1, // weight shift strength
                               MRU_WRS = 1, ACU_WRS = 1, // random weight strength

                               MRU_LR = 0.00001, // WRU gate's network learning rate
                               ACU_LR = 0.00001, // ACU actor-critic network learning rate
                               IER = 0.5, // initial agent's exploration rate
                               ER_RR = 0.8, // retention rate of exploration rate: ER(t) = ER(t-1) * ER_RR
                               GAMMA = 0.9, // discount factor for reinforcement learning

                               C1 = 1, C2 = 1, C3 = 0.8, // tuning const for calculating genetic distance
                               compThres = 2.3, // max distance to be consider the same species
                               evictRate = 0.2, // percentage of eviction from the population
                               extinctThres = 5, // minimum number of individuals to maintain a species existence       //DEBUG
                               ISMR = 0.001; // inter-species mating rate

    /**-----------------------------------------------------------------------------------------------------------------
     * NEAT ancestry archive
     * Use hashmap to efficiently search for and return existing connections, this
     * also supports efficiently storing a large number of connection genes
     */
    private static GeneSet<MRUConGene> MRUConAnc;
    private static GeneSet<MRUNodeGene> MRUNodeAnc;

    private static GeneSet<ACUConGene> ACUConAnc;
    private static GeneSet<ACUNodeGene> ACUNodeAnc;

    private static GeneSet<Individual> generalPopulation;
    private static GeneSet<Species> ecosystem;
    private static AncestorTree ancestorTree;

    public static List<Individual> listPopulation() { return generalPopulation.getData(); }
    public static List<Species> listEcosystem() { return ecosystem.getData(); }
    public static AncestorTree getAncestorTree() { return ancestorTree; }

    /**-----------------------------------------------------------------------------------------------------------------
     * Set up the NEAT class with population specifications
     * and shared train/test data
     * @param worldSize - the dimension of grid world
     * @param timeSteps - the number of time units in 1 episode
     * @param bp - the environment blueprint
     * @param numInput - number of input nodes
     * @param numHidden - number of hidden nodes
     * @param numOutput - number of output nodes
     * @param maxPop - max population
     * @param trEps - number of training episodes
     * @param teEps - number of testing episodes
     */
    public static void setUp(int worldSize, int timeSteps, char[][][] bp, int numInput, int numHidden, int numOutput, int maxPop, int trEps, int teEps) {

        trainSimulator = new TrainRoom(worldSize, timeSteps);
        testSimulator = new TestRoom(worldSize, timeSteps);
        ensembleSimulator = new EnsembleRoom(worldSize, timeSteps);

        MRUConAnc = new GeneSet<>();
        MRUNodeAnc = new GeneSet<>();

        ACUConAnc = new GeneSet<>();
        ACUNodeAnc = new GeneSet<>();

        generalPopulation = new GeneSet<>();
        ecosystem = new GeneSet<>();
        ancestorTree = new AncestorTree();

        bluePrint = bp;
        numTrEps = trEps;
        numTeEps = teEps;

        reset(numInput, numHidden, numOutput, maxPop);

    }

    /**
     * Reset and initialize the population including the gene's ancestries
     * @param numInput - number of input nodes
     * @param numHidden - number of hidden nodes
     * @param numOutput - number of output nodes
     * @param maxPop - max number of individual in a population
     */
    private static void reset(int numInput, int numHidden, int numOutput, int maxPop) {

        MRUNodeAnc.clear();
        MRUConAnc.clear();
        ACUNodeAnc.clear();
        ACUConAnc.clear();
        ecosystem.clear();
        generalPopulation.clear();

        int inputN = numInput + numHidden;

        // set up top-level ancestries
        for (int i = 0; i < inputN; ++i) {
            initMRUNode(inputNodeX, (i + 1)/(double)(inputN + 1) * 1000); // MRU: 1..(inN + hidN)  innovation record
        }
        for (int i = 0; i < numHidden; ++i) {
            double Y = (i + 1)/(double)(numHidden + 1) * 1000;
            initMRUNode(hiddenNodeX, Y); // MRU: (inN + hidN + 1)..(inN + 2 * hidN) innovation record
            initACUNode(hiddenNodeX, Y); // ACU: 1..hidN innovation record
        }
        for (int i = 0; i < numOutput; ++i) {
            initACUNode(outputNodeX, (i + 1)/(double)(numOutput + 1) * 1000); // ACU: (hidN + 1)..(hidN + outN) innovation record
        }

        // populate initial population of NEAT
        for (int i = 0; i < maxPop; ++i) {
            Individual individual = initIndividual(inputN, numHidden, numOutput);
            individual.express(); // express the initial genomes
            generalPopulation.add(individual); // none have a species yet
        }

    }

    /**
     * NOTE: The common ancestries should already exist when this is invoked
     * Generate the simplest individual, root the input, hidden (same as input)
     * and output nodes of this individual with the node ancestry record
     * @return the simplest individual
     */
    private static Individual initIndividual(int inputN, int hiddenN, int outputN) {

        MRUGenome mru = new MRUGenome(inputN, hiddenN);
        ACUGenome acu = new ACUGenome(hiddenN, outputN);

        GeneSet<MRUNodeGene> mrunodes = mru.getNodes();
        GeneSet<ACUNodeGene> acunodes = acu.getNodes();

        // insert all top-level-MRU-ancestor genes
        for (int i = 0; i < (inputN + hiddenN); ++i) mrunodes.add(MRUNodeAnc.get(i).clone());
        // insert all top-level-ACU-ancestor genes
        for (int i = 0; i < (hiddenN + outputN); ++i) acunodes.add(ACUNodeAnc.get(i).clone());

        return new Individual(mru, acu);

    }

    /**
     * NOTE: Innovation numbers start from 1 but index 0 in ancestry record
     * Make a new node gene with new innovation number and record it in the ancestry
     * @return the new node gene
     */
    private static MRUNodeGene initMRUNode(double x, double y) {
        MRUNodeGene n = new MRUNodeGene(MRUNodeAnc.size() + 1);
        n.setX(x);
        n.setY(y);
        MRUNodeAnc.add(n);
        return n.clone(); // return a clone because ancestor need to stay pure
    }
    private static ACUNodeGene initACUNode(double x, double y) {
        ACUNodeGene n = new ACUNodeGene(ACUNodeAnc.size() + 1);
        n.setX(x);
        n.setY(y);
        ACUNodeAnc.add(n);
        return n.clone(); // return a clone because ancestor need to stay pure
    }

    /**
     * NOTE: Innovation numbers start from 1 but index 0 in ancestry record
     * Make a new connection gene, if this gene has already existed in
     * the ancestry, give it its ancestral innovation number,
     * else, make a new ancestral entry
     * @param f - from-gene
     * @param t - to-gene
     * @return a new pure connection gene
     */
    private static MRUConGene logMRUCon(MRUNodeGene f, MRUNodeGene t) {
        MRUConGene c = new MRUConGene(f, t);
        if (MRUConAnc.contains(c)) c.setIN(MRUConAnc.get(c).getIN());
        else { // connection don't exist in ancestry so add it
            c.setIN(MRUConAnc.size() + 1);
            MRUConAnc.add(c);
        }
        return c;
    }
    private static ACUConGene logACUCon(ACUNodeGene f, ACUNodeGene t) {
        ACUConGene c = new ACUConGene(f, t); // a new pure connection in case adding to ancestry
        if (ACUConAnc.contains(c)) c.setIN(ACUConAnc.get(c).getIN());
        else { // connection don't exist in ancestry so add it
            c.setIN(ACUConAnc.size() + 1);
            ACUConAnc.add(c);
        }
        return c;
    }

    /**-----------------------------------------------------------------------------------------------------------------
     * Compute and return the genetic distance between two individuals based on
     * their MRU connections and ACU connections
     * @param i1 - first individual
     * @param i2 - second individual
     * @return their genetic distance
     */
    public static double geneticDistance(Individual i1, Individual i2) {

        if (i1 == i2) return 0; // distance is zero for the same individual

        int D = 0; // number of genetic disjoints
        int S = 0; // number of same-origin gene
        int N = 1; // largest number of gene
        int E = 0; // number of excess gene
        double DW = 0; // total weight difference

        // compute distance between 2 MRU genome
        List<MRUConGene> mruc1 = i1.getMRU().getCons().getData(), mruc2 = i2.getMRU().getCons().getData();
        int i = 0, j = 0, s1 = mruc1.size(), s2 = mruc2.size();
        N += Math.max(s1, s2);
        while (i < s1 && j < s2) {
            MRUConGene c1 = mruc1.get(i), c2 = mruc2.get(j);
            int in1 = c1.getIN(), in2 = c2.getIN();
            if (in1 == in2) {
                // sum difference of all gates
                DW += (Math.abs(c1.getWeight('f') - c2.getWeight('f')) +
                        Math.abs(c1.getWeight('i') - c2.getWeight('i')) +
                        Math.abs(c1.getWeight('c') - c2.getWeight('c')) +
                        Math.abs(c1.getWeight('o') - c2.getWeight('o')));
                S++; i++; j++;
            }
            else if (in1 > in2) { D++; j++; }
            else { D++; i++; }
        }
        E += (s1 > s2) ? (s1 - i) : (s2 - j);

        // compute distance between 2 ACU genome
        List<ACUConGene> acuc1 = i1.getACU().getCons().getData(), acuc2 = i2.getACU().getCons().getData();
        i = 0; j = 0; s1 = acuc1.size(); s2 = acuc2.size();
        N += Math.max(s1, s2);
        while (i < s1 && j < s2) {
            ACUConGene c1 = acuc1.get(i), c2 = acuc2.get(j);
            int in1 = c1.getIN(), in2 = c2.getIN();
            if (in1 == in2) {
                // sum difference of actor and critic
                DW += (Math.abs(c1.getWeight(true) - c2.getWeight(true)) +
                        Math.abs(c1.getWeight(false) - c2.getWeight(false)));
                S++; i++; j++;
            }
            else if (in1 > in2) { D++; j++; }
            else { D++; i++; }
        }
        E += (s1 > s2) ? (s1 - i) : (s2 - j);

        // return final distance
        DW = (S != 0) ? (DW / S) : 0; // if no (matching) connections exist, S = 0
        return C1 * E / N + C2 * D / N + C3 * DW;

    }

    /**-----------------------------------------------------------------------------------------------------------------
     * Mutate an individual, there are 5 types of mutations:
     * - Mutate add connection with Prob(MAC)
     * - Mutate add node with Prob(MAN)
     * - Mutate flip enable with Prob(MFE)
     * - Mutate weight with Prob(MW)
     * @param individual - the individual to be mutated
     */
    public static void mutate(Individual individual) {

        MRUGenome mru = individual.getMRU();
        ACUGenome acu = individual.getACU();

        GeneSet<MRUNodeGene> mrunodes = mru.getNodes();
        GeneSet<MRUConGene> mrucons = mru.getCons();

        GeneSet<ACUNodeGene> acunodes = acu.getNodes();
        GeneSet<ACUConGene> acucons = acu.getCons();

        if (MRU_P_MAC > Math.random()) { // add 1 connection (can be unique or not)
            for (int i = 0; i < MUTATE_ATTEMPTS; ++i) { // mutate MRU
                MRUNodeGene a = mrunodes.getRandom(), b = mrunodes.getRandom();
                double ax = a.getX(), bx = b.getX();
                if (ax == bx) continue; // no same level connection allowed
                MRUConGene c = (ax < bx) ? logMRUCon(a, b) : logMRUCon(b, a);
                if (mrucons.contains(c)) continue; // if this genome already has this connection, try again
                double w = Math.random() * 2 - 1; // a random number from -1 to 1
                // all gates start with same weight
                c.setWeight(w, 'f');
                c.setWeight(w, 'i');
                c.setWeight(w, 'c');
                c.setWeight(w, 'o');
                ((ax < bx) ? b : a).getInCons().add(c);
                mrucons.addInOrder(c);
                break; // add only 1 new connection
            }
        }
        if (ACU_P_MAC > Math.random()) {
            for (int i = 0; i < MUTATE_ATTEMPTS; ++i) { // mutate ACU
                ACUNodeGene a = acunodes.getRandom(), b = acunodes.getRandom();
                double ax = a.getX(), bx = b.getX();
                if (ax == bx) continue;
                ACUConGene c = (ax < bx) ? logACUCon(a, b) : logACUCon(b, a);
                if (acucons.contains(c)) continue;
                double w = Math.random() * 2 - 1;
                c.setWeight(w, true);
                c.setWeight(w, false);
                ((ax < bx) ? b : a).getInCons().add(c);
                acucons.addInOrder(c);
                break;
            }
        }
        if (MRU_P_MAN > Math.random()) { // add 1 new node (and, thus, 2 new connections)
            // this operation affects all gates in MRU
            MRUConGene mruc = mrucons.getRandom();
            if (mruc != null) { // only add when a connection exist
                MRUNodeGene a = mruc.getFG(), b = mruc.getTG(); // extract the two sides
                MRUNodeGene m = initMRUNode((a.getX() + b.getX()) / 2, (a.getY() + b.getY()) / 2 + Math.random() * 500 - 250); // make an entirely new node
                MRUConGene a_m = logMRUCon(a, m); // this will return a new connection that just got added to ancestry
                MRUConGene m_b = logMRUCon(m, b);
                a_m.setWeight(1, 'f');
                a_m.setWeight(1, 'i');
                a_m.setWeight(1, 'c');
                a_m.setWeight(1, 'o');
                m_b.setWeight(mruc.getWeight('f'), 'f');
                m_b.setWeight(mruc.getWeight('i'), 'i');
                m_b.setWeight(mruc.getWeight('c'), 'c');
                m_b.setWeight(mruc.getWeight('o'), 'o');
                m_b.setEnabled(mruc.isEnabled());
                mruc.setEnabled(false); // disable the old connection
                m.getInCons().add(a_m);
                b.getInCons().add(m_b);
                mrucons.add(a_m); // connection just got added to ancestry so do not need to add in order
                mrucons.add(m_b);
                mrunodes.add(m);
            }
        }
        if (ACU_P_MAN > Math.random()) {
            // this operation affects both actor and critic in ACU
            ACUConGene acuc = acucons.getRandom();
            if (acuc != null) {
                ACUNodeGene a = acuc.getFG(), b = acuc.getTG();
                ACUNodeGene m = initACUNode((a.getX() + b.getX()) / 2, (a.getY() + b.getY()) / 2 + Math.random() * 500 - 250);
                ACUConGene a_m = logACUCon(a, m);
                ACUConGene m_b = logACUCon(m, b);
                a_m.setWeight(1, true);
                a_m.setWeight(1, false);
                m_b.setWeight(acuc.getWeight(true), true);
                m_b.setWeight(acuc.getWeight(false), false);
                m_b.setEnabled(acuc.isEnabled());
                acuc.setEnabled(false);
                m.getInCons().add(a_m);
                b.getInCons().add(m_b);
                acucons.add(a_m);
                acucons.add(m_b);
                acunodes.add(m);
            }
        }

        if (MRU_P_MFE > Math.random()) { // flip enable mutation
            MRUConGene mruc = mrucons.getRandom();
            if (mruc != null) mruc.setEnabled(!mruc.isEnabled());
        }
        if (ACU_P_MFE > Math.random()) {
            ACUConGene acuc = acucons.getRandom();
            if (acuc != null) acuc.setEnabled(!acuc.isEnabled());
        }
        if (MRU_P_MW > Math.random()) { // weight mutation
            MRUConGene mruc = mrucons.getRandom();
            if (mruc != null) {
                if (MRU_P_MWS > Math.random()) { // shift mutation
                    // same weight direction for all gates
                    double w = (Math.random() * 2 - 1) * MRU_WSS;
                    mruc.setWeight(mruc.getWeight('f') + w, 'f');
                    mruc.setWeight(mruc.getWeight('i') + w, 'i');
                    mruc.setWeight(mruc.getWeight('c') + w, 'c');
                    mruc.setWeight(mruc.getWeight('o') + w, 'o');
                } else { // reset mutation
                    // same reset for all gates
                    double w = (Math.random() * 2 - 1) * MRU_WRS;
                    mruc.setWeight(w, 'f');
                    mruc.setWeight(w, 'i');
                    mruc.setWeight(w, 'c');
                    mruc.setWeight(w, 'o');
                }
            }
        }
        if (ACU_P_MW > Math.random()) {
            ACUConGene acuc = acucons.getRandom();
            if (acuc != null) {
                if (ACU_P_MWS > Math.random()) {
                    double w = (Math.random() * 2 - 1) * ACU_WSS;
                    acuc.setWeight(acuc.getWeight(true) + w, true);
                    acuc.setWeight(acuc.getWeight(false) + w, false);
                } else {
                    double w = (Math.random() * 2 - 1) * ACU_WRS;
                    acuc.setWeight(w, true);
                    acuc.setWeight(w, false);
                }
            }
        }

    }

    /**-----------------------------------------------------------------------------------------------------------------
     * Align two parents and perform cross over on them to
     * produce an offspring. The offspring will randomly inherit
     * a trait from either one of its parents (50 percent chance)
     * until its genomes are completed
     * @param p1 - parent 1 (fitter than parent 2)
     * @param p2 - parent 2
     * @return the offspring
     */
    public static Individual crossBreed(Individual p1, Individual p2) {

        Individual child = initIndividual(
                p1.getMRU().getInputNum(), // inherit input layer architecture
                p1.getMRU().getOutputNum(), // inherit hidden layer architecture
                p1.getACU().getOutputNum() // inherit output layer architecture
        );

        // mark MRU skeleton on parents
        GeneSet<MRUConGene> mruc1 = p1.getMRU().getCons(), mruc2 = p2.getMRU().getCons();
        int i = 0, j = 0, s1 = mruc1.size(), s2 = mruc2.size();
        ArrayList<MRUConGene> MRUSkeleton = new ArrayList<>();
        while (i < s1 && j < s2) {
            MRUConGene c1 = mruc1.get(i), c2 = mruc2.get(j);
            int in1 = c1.getIN(), in2 = c2.getIN();
            if (in1 == in2) { MRUSkeleton.add((Math.random() > 0.5) ? c1 : c2); i++; j++; }
            // collect at parent 1's disjoint
            else if (in1 < in2) { MRUSkeleton.add(c1); i++; }
            else j++;
        }
        while (i < s1) MRUSkeleton.add(mruc1.get(i++)); // collect all parent 1's excess

        // build child's MRU body that is separate from parents
        GeneSet<MRUNodeGene> mrunodes = child.getMRU().getNodes();
        GeneSet<MRUConGene> mrucons = child.getMRU().getCons();
        for (MRUConGene c : MRUSkeleton) {

            MRUNodeGene n1 = c.getFG().clone(), n2 = c.getTG().clone();
            // if nodes in this connection already existed,
            // get the existed node instead
            if (mrunodes.contains(n1)) n1 = mrunodes.get(n1);
            if (mrunodes.contains(n2)) n2 = mrunodes.get(n2);
            MRUConGene childCon = logMRUCon(n1, n2); // make a new connection that is same with parent's
            // child inherit all gate's weights
            childCon.setWeight(c.getWeight('f'), 'f');
            childCon.setWeight(c.getWeight('i'), 'i');
            childCon.setWeight(c.getWeight('c'), 'c');
            childCon.setWeight(c.getWeight('o'), 'o');
            childCon.setEnabled((c.isEnabled()) || !(P_DW > Math.random()));
            mrunodes.add(n1); // random hash set check if object already existed
            mrunodes.add(n2);
            n2.getInCons().add(childCon);
            mrucons.add(childCon); // parents already have their connections in order

        }

        // mark ACU skeleton on parents
        GeneSet<ACUConGene> acuc1 = p1.getACU().getCons(), acuc2 = p2.getACU().getCons();
        i = 0; j = 0; s1 = acuc1.size(); s2 = acuc2.size();
        ArrayList<ACUConGene> ACUSkeleton = new ArrayList<>();
        while (i < s1 && j < s2) {
            ACUConGene c1 = acuc1.get(i), c2 = acuc2.get(j);
            int in1 = c1.getIN(), in2 = c2.getIN();
            if (in1 == in2) { ACUSkeleton.add((Math.random() > 0.5) ? c1 : c2); i++; j++; }
            else if (in1 < in2) { ACUSkeleton.add(c1); i++; }
            else j++;
        }
        while (i < s1) ACUSkeleton.add(acuc1.get(i++));

        // build child's ACU body that is separate from parents
        GeneSet<ACUNodeGene> acunodes = child.getACU().getNodes();
        GeneSet<ACUConGene> acucons = child.getACU().getCons();
        for (ACUConGene c : ACUSkeleton) {

            ACUNodeGene n1 = c.getFG().clone(), n2 = c.getTG().clone();
            if (acunodes.contains(n1)) n1 = acunodes.get(n1);
            if (acunodes.contains(n2)) n2 = acunodes.get(n2);
            ACUConGene childCon = logACUCon(n1, n2);
            childCon.setWeight(c.getWeight(true), true);
            childCon.setWeight(c.getWeight(false), false);
            childCon.setEnabled((c.isEnabled()) || !(P_DW > Math.random()));
            acunodes.add(n1);
            acunodes.add(n2);
            n2.getInCons().add(childCon);
            acucons.add(childCon);

        }

        return child;

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
    public static void evolve() {

        // elect a random representative for each existing species
        //for (Species s : ecosystem.getData()) s.elect();

        // speciate all newborn individuals into existing species
        for (Individual i : generalPopulation.getData()) {
            if (i.getSpecies() != null) continue; // don't care about species-assigned individuals
            double bestComp = Double.POSITIVE_INFINITY;
            Species bestFit = ecosystem.get(0);
            for (Species s : ecosystem.getData()) { // check compatibility with all species
                double comp = geneticDistance(s.getRepr(), i);
                if (comp < bestComp) { bestComp = comp; bestFit = s; }
            }
            if (bestComp < compThres) bestFit.parentFit(i);
            else { // no good fit, branch new species
                Species p = i.getParentSpecies(); // this new species deviates from the original one
                Species newSpecies = new Species(i, (p != null) ? p.getID() : NULL_SPECIES);
                ecosystem.add(newSpecies);
                try { // ancestor tree performs cloning of representative for structural changes marking
                    ancestorTree.add(p, newSpecies);
                }
                catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
            }
        }

        // species regulation
        Breeder<Species> breeder = new Breeder<>(); // prepare a mator
        for (int i = 0, n = ecosystem.size(); i < n; ++i) {
            Species s = ecosystem.get(i);
            s.compete(); // individuals compete within their own species and compute species score
            s.evict(); // evict low performance individuals
            if (s.size() <= extinctThres) { // if species population get too low
                s.extinct();
                ecosystem.remove(s); // extinct and remove it
                ancestorTree.stop(s);
                --n;
                continue; // extinct species are unfit for mating
            }
            breeder.add(s, s.getScore());
        } // after this, a bunch of individuals are species-unassigned (dead)

        // generate offspring from random fittest species and replace evicted individuals
        for (Individual i : generalPopulation.getData()) {
            if (i.getSpecies() == null) { // look at species-unassigned (dead) individuals                           //DEBUG
                Species s1 = breeder.random();
                // random inter-species mating and replace the dead with the newborn
                i.replace(
                        (ISMR <= Math.random()) ? s1.reproduce() :
                        crossBreed(
                                Utils.getFittest(s1.getPopulation().getData()),
                                Utils.getFittest(breeder.random().getPopulation().getData())
                        )
                );                                                                                                      //DEBUG
                i.setParentSpecies(s1);                                                                                //DEBUG
                mutate(i);
                i.express(); // express the individual only once after its birth
            }                                                                                                         //DEBUG
        } // this newborn individual is species-unassigned (alive)

        //System.out.println(generalPopulation.get(0));                                                                   // DEBUG
        //System.out.println(generalPopulation.get(1));
        //System.out.println(generalPopulation.get(2));

    } // after this step, a bunch of individuals will have no species, they are the newborns                // TRY MUTATE ALL POPULATION

}
