package Supervisors;

import Environment.Controllers.Utils;
import NEAT.*;
import NEAT.DataStructures.SecuredList;
import NEAT.Genome.DecisionHead.DHg;
import NEAT.Genome.DecisionHead.DHng;
import NEAT.Genome.MemoryHead.MHg;
import NEAT.Genome.MemoryHead.MHng;

import java.util.Objects;
import java.util.Scanner;

import static NEAT.Lambdas.Graphics.initY;
import static NEAT.NEAT.*;

public class MatrixMain {

    static SecuredList<Individual> generalPopulation = new SecuredList<>();
    static SecuredList<Species> ecosystem = new SecuredList<>();

    static GenePool genePool = new GenePool();
    static Mutator mutator = new Mutator(genePool);
    static GAN gan = new GAN(genePool);

    public static void main(String[] args) throws CloneNotSupportedException {

        Scanner scanner = new Scanner(System.in);

        reset();

        System.out.println("Enter: ");
        while (!scanner.nextLine().equals("q")) {
            evolve();
            gan.trainD_MRU(generalPopulation.getData());
            Individual chosen = generalPopulation.getRandom();
            System.out.println("Before GAN: " + chosen.getMRU());
            gan.generateGMutation_MRU(5, chosen.getMRU());
            System.out.println("After GAN: " + chosen.getMRU());
            System.out.println("Enter: ");
        }

    }

    public static void reset() {

        int inputN = 5 + 3;

        // set up common ancestries (the input-hidden-output frame)
        // we ignore the returned node here
        for (int i = 0; i < inputN; ++i) {
            genePool.logNode(-1, inputNodeX, initY.apply(i, inputN), GenePool.NodeType.MRU, true); // MRU: 1..(inN + hidN)  innovation record
        }
        for (int i = 0; i < 3; ++i) {
            genePool.logNode(-1, hiddenNodeX, initY.apply(i, 3), GenePool.NodeType.MRU, true); // MRU: (inN + hidN + 1)..(inN + 2 * hidN) innovation record
            genePool.logNode(-1, hiddenNodeX, initY.apply(i, 3), GenePool.NodeType.ACU, true); // ACU: 1..hidN innovation record
        }
        for (int i = 0; i < 2; ++i) {
            genePool.logNode(-1, outputNodeX, initY.apply(i, 2), GenePool.NodeType.ACU, true); // ACU: (hidN + 1)..(hidN + outN) innovation record
        }

        // initial population
        for (int i = 0; i < 3; ++i) {

            MHg mru = new MHg(inputN, 3);
            DHg acu = new DHg(3, 2);

            SecuredList<MHng> mrunodes = mru.getNodes();
            SecuredList<DHng> acunodes = acu.getNodes();

            // build base frame by inserting base ancestors
            for (int j = 0; j < (inputN + 3); ++j) mrunodes.add((MHng) genePool.logNode(j, 0, 0, GenePool.NodeType.MRU, true));
            for (int j = 0; j < (3 + 2); ++j) acunodes.add((DHng) genePool.logNode(j, 0, 0, GenePool.NodeType.ACU, true));

            Individual individual = new Individual(mru, acu);
            individual.express(); // express the initial genomes

            generalPopulation.add(individual); // none have a species yet

        }

    }

    public static void evolve() throws CloneNotSupportedException {

        // speciate newborn individuals (species-unassigned)
        for (Individual i : generalPopulation.getData()) {
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
            }
        }

        // species regulation

        // sample a backup blueprint in case the universal blueprint is null (random mode)
        char[][][] backupBlueprint = Utils.makeBlueprint(5, true, true, 2, 1, null, null);

        for (int i = 0, n = ecosystem.size(); i < n; ++i) {
            Species s = ecosystem.get(i);
            // every individual is trained and tested on a unified blueprint
            s.compete(Objects.requireNonNullElse(bluePrint, backupBlueprint)); // also compute species score for ranker
        } // after this, a bunch of individuals are species-unassigned (dead)

        // generate offspring from random best species and replace evicted individuals
        for (Individual i : generalPopulation.getData()) {
            mutator.mutateStructure(i);
            i.express(); // express the individual once after its birth due to structural mutation
            i.setSpecies(null);
        } // newborn individuals are species-unassigned [alive]

    }

}
