package Neat;

import Environment.Simulation;
import Neat.DataStructures.RandomHashSet;
import Neat.DataStructures.RandomSelector;
import Neat.Genome.ConGene;
import Neat.Genome.Genome;
import Neat.Genome.NodeGene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Neat {

    /**
     * NEAT hyper-parameters
     */
    public static boolean converged = false;
    public static int inputN = -1, outputN = -1, numEps = -1; // impossible values for debug
    public static char[][][] bluePrint;
    public static Simulation simulator; // simulator for reinforcement learning
    public static final int MAXNODE = (int)Math.pow(2, 20), mutateAttempts = 100;
    public static final double inputNodeX = 10, outputNodeX = 1000,
                               P_MAC = 0.3, // probability of an add connection mutation occurs
                               P_MAN = 0.03, // add node mutation
                               P_MW = 0.8, // weight mutation
                               P_MWS = 0.9, // weight shift chance -> 0.1 random initialize
                               P_MFE = 0.4, // enable flip mutation
                               P_DW = 0.75, // inherited disable chance
                               WSS = 0.8, // weight shift strength
                               WRS = 1, // random weight strength
                               LR = 0.01, // network learning rate
                               GAMMA = 0.9, // discount factor for reinforcement learning
                               C1 = 1, C2 = 1, C3 = 0.4, // tuning const for calculating genetic difference
                               compThres = 4, // max distance to be consider the same species
                               convThres = 0.0000001, // convergence threshold
                               evictRate = 0.4, // percentage of eviction from the population
                               ISMR = 0.001; // inter-species mating rate

    /**
     * NEAT private attributes
     */
    private static double prevFittestScore;
    private static HashMap<ConGene, ConGene> conAncestry;
    private static RandomHashSet<NodeGene> nodeAncestry;
    private static RandomHashSet<Genome> generalPopulation;
    private static RandomHashSet<Species> ecosystem;

    public static List<Genome> getPopulation() { return generalPopulation.getData(); }

    /**
     * Set up the NEAT class with population specifications
     * and shared train/test data
     * @param s - the environment simulator
     * @param numInput - number of input nodes
     * @param numOutput - number of output nodes
     * @param maxPop - max population
     */
    public static void setUp(Simulation s, char[][][] bp, int numInput, int numOutput, int maxPop, int eps) {
        conAncestry = new HashMap<>();
        nodeAncestry = new RandomHashSet<>();
        generalPopulation = new RandomHashSet<>();
        ecosystem = new RandomHashSet<>();
        simulator = s;
        bluePrint = bp;
        numEps = eps;
        reset(numInput, numOutput, maxPop);
    }

    /**
     * generate the simplest genome, root the input and output
     * nodes of this genome with the node ancestry record
     * @return simplest genome
     */
    public static Genome initGenome() {
        Genome g = new Genome();
        // innovation no. starts from 1
        for (int i = 0; i < inputN + outputN; ++i) g.getNodes().add(originateNode(i+1));
        return g;
    }

    /**
     * reset the population including the gene's ancestries
     * @param inN - number of input nodes
     * @param outN - number of output nodes
     * @param maxPop - max number of individual in a population
     */
    public static void reset(int inN, int outN, int maxPop) {

        inputN = inN; outputN = outN;

        conAncestry.clear();
        nodeAncestry.clear();
        generalPopulation.clear();

        for (int i = 0; i < inN; ++i) {
            NodeGene n = initNode();
            n.setX(inputNodeX);
            n.setY((i + 1)/(double)(inN + 1) * 1000);
        }

        for (int i = 0; i < outN; ++i) {
            NodeGene n = initNode();
            n.setX(outputNodeX);
            n.setY((i + 1)/(double)(outN + 1) * 1000);
        }

        for (int i = 0; i < maxPop; ++i) {
            Genome g = initGenome();
            generalPopulation.add(g);
        }

    }

    /**
     * make a new node gene with new innovation number and record it in the ancestry
     * @return the new node gene
     */
    public static NodeGene initNode() {
        NodeGene n = new NodeGene(nodeAncestry.size() + 1);
        nodeAncestry.add(n);
        return n;
    }

    /**
     * make a new connection gene, if this gene has already existed in
     * the ancestry, give this new gene its ancestral ID (innovation number)
     * else, make a new ancestry record
     * @param f - from-gene
     * @param t - to-gene
     * @return the new connection gene
     */
    public static ConGene initCon(NodeGene f, NodeGene t) {
        ConGene cg = new ConGene(f, t);
        if (conAncestry.containsKey(cg)) cg.setIN(conAncestry.get(cg).getIN());
        else { // connection don't exist so add it
            cg.setIN(conAncestry.size() + 1);
            conAncestry.put(cg, cg);
        }
        return cg;
    }

    /**
     * if the ancestral ID exists, return the equivalent node gene
     * else, make an entirely new node gene ancestry
     * @param IN - innovation number
     * @return a node gene that matches the innovation number
     */
    public static NodeGene originateNode(int IN) {
        if (IN > 0 && IN <= nodeAncestry.size())
            return nodeAncestry.get(IN - 1).clone();
        return initNode();
    }

    /**
     * compute and return the genetic distance between two genomes
     * @param g1 - first genome
     * @param g2 - second genome
     * @return their genetic distance
     */
    public static double geneticDistance(Genome g1, Genome g2) {

        RandomHashSet<ConGene> c1 = g1.getCons(), c2 = g2.getCons();
        int i = 0, j = 0, s1 = c1.size(), s2 = c2.size();
        int D = 0, S = 0; // disjoint, number of same-origin gene
        double N, DW = 0; // largest number of gene, total weight difference

        int g1MaxIN = 0, g2MaxIN = 0;
        if (s1 != 0) g1MaxIN = c1.get(s1-1).getIN();
        if (s2 != 0) g2MaxIN = c2.get(s2-1).getIN();
        if (g1MaxIN < g2MaxIN) { Genome g = g1; g1 = g2; g2 = g; }

        c1 = g1.getCons(); c2 = g2.getCons();
        s1 = c1.size(); s2 = c2.size();

        while (i < s1 && j < s2) {
            ConGene cg1 = c1.get(i), cg2 = c2.get(j);
            int in1 = cg1.getIN(), in2 = cg2.getIN();
            if (in1 == in2) {
                // consider only the actor
                DW += Math.abs(cg1.getWeight(true) - cg2.getWeight(true));
                S++; i++; j++;
            }
            else if (in1 > in2) { D++; j++; }
            else { D++; i++; }
        }

        if ((N = Math.max(s1, s2)) < 20) N = 1;
        return C1*(s1 - i)/N + C2*D/N + C3*((S != 0) ? (DW / S) : 0);

    }

    /**
     * mutate a genome, there are 5 types of mutations:
     * - Mutate add connection with Prob(MAC)
     * - Mutate add node with Prob(MAN)
     * - Mutate flip enable with Prob(MFE)
     * - Mutate weight with Prob(MW)
     * @param g - the genome
     */
    public static void mutate(Genome g) {

        ConGene c;
        RandomHashSet<ConGene> cons = g.getCons();
        RandomHashSet<NodeGene> nodes = g.getNodes();

        if (P_MAC > Math.random()) { // add connection

            for (int i = 0; i < mutateAttempts; ++i) {
                NodeGene a = nodes.getRandom(), b = nodes.getRandom();
                // no same level connection allowed
                if (a.getX() == b.getX()) continue;
                if (a.getX() < b.getX()) c = initCon(a, b);
                else c = initCon(b, a);
                // don't make connection if already exist
                if (cons.contains(c)) {
                    if (a.getX() < b.getX()) b.removeCon(c);
                    else a.removeCon(c);
                    continue;
                }
                double w = Math.random() * 2 - 1; // a number from -1 to 1
                c.setWeight(w, true); // both actor and critic starts with same weight
                c.setWeight(w, false);
                cons.addInOrder(c);
                break; // add only 1 new connection
            }

        }
        if (P_MAN > Math.random()) { // add node (and two new connections)

            // this operation affects both actor and critic
            c = cons.getRandom();
            if (c != null) {
                NodeGene a = c.getFG(), b = c.getTG();
                NodeGene m = initNode();
                m.setX((a.getX() + b.getX()) / 2);
                m.setY((a.getY() + b.getY()) / 2 + Math.random() * 500 - 250);

                ConGene am = initCon(a, m);
                ConGene mb = initCon(m, b);
                am.setWeight(1, true);
                am.setWeight(1, false);
                mb.setWeight(c.getWeight(true), true);
                mb.setWeight(c.getWeight(false), false);
                mb.setEnabled(c.isEnabled());
                c.setEnabled(false);

                cons.add(am);
                cons.add(mb);
                nodes.add(m);
            }

        }
        if (P_MFE > Math.random()) { // flip enable
            c = cons.getRandom();
            if (c != null) c.setEnabled(!c.isEnabled());
        }
        if (P_MW > Math.random()) { // weight mutation
            c = cons.getRandom();
            if (c == null) return; // return early if no connection found
            if (P_MWS > Math.random()) {
                // same weight direction for both actor and critic
                double w = (Math.random() * 2 - 1) * WSS;
                c.setWeight(c.getWeight(true) + w, true);
                c.setWeight(c.getWeight(false) + w, false);
            }
            else {
                double w = (Math.random() * 2 - 1) * WRS;
                c.setWeight(w, true);
                c.setWeight(w, false);
            }
        }

    }

    /**
     * align two parent genome and perform cross over on them to
     * produce an offspring. The offspring will randomly inherit
     * a trait from either one of its parents until its genome is
     * completed
     * @param p1 - parent 1 (fitter than parent 2)
     * @param p2 - parent 2
     * @return the offspring genome
     */
    public static Genome crossBreed(Genome p1, Genome p2) {

        Genome child = initGenome();
        RandomHashSet<ConGene> c1 = p1.getCons(), c2 = p2.getCons();
        int in1, in2, i = 0, j = 0;
        int s1 = c1.size(), s2 = c2.size();

        ArrayList<ConGene> childCons = new ArrayList<>();

        // build child's skeleton from parents
        while (i < s1 && j < s2) {
            ConGene cg1 = c1.get(i), cg2 = c2.get(j);
            in1 = cg1.getIN(); in2 = cg2.getIN();
            if (in1 == in2) {
                if (Math.random() > 0.5) childCons.add(cg1);
                else childCons.add(cg2);
                i++; j++;
            }
            else if (in1 > in2) j++;
            else { childCons.add(cg1); i++; }
        }
        while (i < s1) childCons.add(c1.get(i++));

        // give child its own body that is separate from parents
        RandomHashSet<NodeGene> nodes = child.getNodes();
        RandomHashSet<ConGene> cons = child.getCons();
        for (ConGene c : childCons) {

            NodeGene n1 = c.getFG().clone();
            if (nodes.contains(n1)) n1 = nodes.get(n1);
            NodeGene n2 = c.getTG().clone();
            if (nodes.contains(n2)) n2 = nodes.get(n2);

            ConGene childCon = initCon(n1, n2);
            // child inherit both actor and critic weights
            childCon.setWeight(c.getWeight(true), true);
            childCon.setWeight(c.getWeight(false), false);
            if (!c.isEnabled()) {
                if (P_DW > Math.random()) childCon.setEnabled(false);
                else childCon.setEnabled(true);
            }
            else childCon.setEnabled(true);

            nodes.add(n1);
            nodes.add(n2);
            cons.add(childCon);

        }

        return child;

    }

    /**
     * evolve the population. This includes:
     * - Elect a representative for each species
     * - Assign each individual into appropriate species
     * - Let the population complete within their species
     * - Evict low performance individual
     * - Remove extinct species
     * - Generate offsprings to fill in population
     * - Check if population's performance has converged
     */
    public static void evolve() {

        // elect a random representative for each species and speciation
        for (Species s : ecosystem.getData()) s.randElect();
        for (Genome g : generalPopulation.getData()) {
            if (g.getSpecies() != null) continue;
            boolean fitted = false;
            for (Species s : ecosystem.getData()) if (s.fit(g)) { fitted = true; break; }
            // no good fit, make new species
            if (!fitted) ecosystem.add(new Species(g));
        }

        for (Species s : ecosystem.getData()) {
            s.compete(); // individuals compete within its own species
            s.evict(); // evict low performance individuals
        }

        // remove extinct species
        int n = ecosystem.size();
        for (int i = 0; i < n; ++i) {
            Species s = ecosystem.get(i);
            if (s.size() <= 1) {
                s.extinct();
                ecosystem.remove(i);
                n--;
            }
        }

        // generate offspring from random fittest species and replace evicted individuals
        RandomSelector<Species> mator = new RandomSelector<>();
        for (Species s : ecosystem.getData()) mator.add(s, s.getScore());
        for (Genome g : generalPopulation.getData()) {
            if (g.getSpecies() == null) {
                Species s1 = mator.random();
                // random inter-species mating
                g.cast((ISMR <= Math.random()) ? s1.reproduce() : crossBreed(s1.getFittest(), mator.random().getFittest()));
                mutate(g);
                s1.forceFit(g);
            }
        }

        // convergence check
        double fittestScore = getFittest().getScore();
        if (Math.abs(fittestScore - prevFittestScore) < convThres) converged = true;
        else prevFittestScore = fittestScore;

    }

    /**
     * find and return the fittest individual in the whole population
     * @return fittest individual
     */
    public static Genome getFittest() {
        Genome fittest = generalPopulation.get(0);
        double fittestScore = fittest.getScore();
        for (Genome g : generalPopulation.getData()) {
            if (g.getScore() > fittestScore) {
                fittestScore = g.getScore();
                fittest = g;
            }
        }
        return fittest;
    }

    /**
     * return the ecosystem information
     * @return ecosystem information
     */
    public static String printEcosystem() {
        StringBuilder rep = new StringBuilder("\n####################################\n");
        for (Species s:ecosystem.getData()) rep.append(s).append("\n");
        return rep.toString();
    }

}
