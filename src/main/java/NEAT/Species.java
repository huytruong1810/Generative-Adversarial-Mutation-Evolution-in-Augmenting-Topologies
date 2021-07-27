package NEAT;

import Environment.Controllers.Utils;
import Environment.Environment;
import NEAT.DataStructures.GeneSet;
import NEAT.DataStructures.Breeder;

import java.util.Comparator;
import java.util.Objects;

import static NEAT.NEAT.*;

public class Species {

    private static int GlobalID = 1;
    private final int ID, predID;

    private double score;
    private Individual repr;
    private final GeneSet<Individual> population;

    public Species(Individual i, int pred) {
        ID = GlobalID++;
        predID = pred;
        score = 0;
        population = new GeneSet<>();
        i.setSpecies(this); repr = i; population.add(i);
    }

    public int size() { return population.size(); }
    public int getID() { return ID; }
    public int getPredID() { return predID; }
    public double getScore() { return score; }
    public Individual getRepr() { return repr; }
    public GeneSet<Individual> getPopulation() { return population; }

    public void parentFit(Individual i) {
        i.setSpecies(this);
        population.add(i);
    }

    public void compete() {

        double speciesSum = 0; // accumulate each genome's score
        // every individual is trained and tested on unified blueprints at each evolution step
        char[][][] trainBlueprint = Utils.makeBlueprint(5, true, true, 2, 1, null, null);
        char[][][] testBlueprint = Utils.makeBlueprint(5, true, true, 2, 1, null, null);
        for (Individual i : population.getData()) {

            // training
            for (int e = 0; e < numTrEps; ++e) {
                trainSimulator.reset( // reset to sample initial state
                        Objects.requireNonNullElse(bluePrint, trainBlueprint),
                        i, null
                );
                while (true) if (trainSimulator.step() != 0) break;
            }

            // testing and report evaluation score
            double sumScore = 0;
            for (int e = 0; e < numTeEps; ++e) {
                testSimulator.reset(
                        Objects.requireNonNullElse(bluePrint, testBlueprint),
                        i, null // no need to clone the genome because testing won't affect genome
                );
                while (true) if (testSimulator.step() != 0) break;
                sumScore += testSimulator.getHScore();                                                           // SPECIFICALLY FOR HUMAN ONLY RIGHT NOW
            }
            double avgScore = sumScore / numTeEps;
            i.setScore(avgScore);
            speciesSum += avgScore;

        }
        score = speciesSum / size(); // species score is average population scores

    }

    public void extinct() {
        // un-assign the species of all individuals in
        // the population, keep their corpses for replacement
        for (Individual i : population.getData()) i.setSpecies(null);
        population.clear();
        score = 0;
    }

    public void elect() {

        Individual centroid = population.get(0);
        double curDist = Double.POSITIVE_INFINITY;
        for (Individual cur : population.getData()) { // get the biologically centroid individual
            double dist = 0;
            for (Individual to : population.getData()) dist += NEAT.geneticDistance(cur, to);
            if (dist < curDist) { curDist = dist; centroid = cur; }
        }

        centroid.setSpecies(this); repr = centroid; population.add(centroid);

    }

    public void evict() {
        population.getData().sort(Comparator.comparingDouble(Individual::getScore));
        int part = (int)(NEAT.evictRate * size());
        for (int i = 0; i < part; ++i) {
            population.get(0).setSpecies(null);
            population.remove(0);
        }
    }

    public Individual reproduce() {
        // generate offspring from random fittest genome
        Breeder<Individual> mator = new Breeder<>();
        for (Individual i : population.getData()) mator.add(i, i.getScore());
        Individual i1 = mator.random();
        Individual i2 = mator.random();
        // parent 1 should be fitter than parent 2
        return (i1.getScore() > i2.getScore()) ? NEAT.crossBreed(i1, i2) : NEAT.crossBreed(i2, i1);
    }

    public boolean equals(Object o) {
        if (!(o instanceof Species))
            return false;
        return ID == ((Species) o).ID;
    }

    @Override
    public String toString() {
        return "<Species" + predID + "->" + ID + "/repr=" + repr + "/score=" + (Math.round(score * 100.0) / 100.0) + "/size=" + size() + "/>";
    }

}
