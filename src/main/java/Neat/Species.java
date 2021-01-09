package Neat;

import Neat.DataStructures.RandomHashSet;
import Neat.DataStructures.RandomSelector;
import Neat.Genome.Genome;

import java.util.Comparator;
import static Neat.Neat.*;

public class Species {

    private double score;
    private Genome representative;
    private RandomHashSet<Genome> population;

    public Species(Genome represent) {
        score = 0;
        population = new RandomHashSet<>();
        represent.setSpecies(this);
        representative = represent;
        population.add(represent);
    }

    public int size() { return population.size(); }
    public double getScore() {return score; }

    public boolean fit(Genome g) {
        if (Neat.geneticDistance(representative, g) < compThres) {
            g.setSpecies(this);
            population.add(g);
            return true;
        }
        return false;
    }

    public void forceFit(Genome g) {
        g.setSpecies(this);
        population.add(g);
    }

    public Genome getFittest() {
        Genome fittest = population.get(0);
        double fittestScore = fittest.getScore();
        for (Genome g : population.getData()) {
            if (g.getScore() > fittestScore) {
                fittestScore = g.getScore();
                fittest = g;
            }
        }
        return fittest;
    }

    public void extinct() {
        // un-assign the species classification of
        // the population, keep the corpse of individuals
        for (Genome g : population.getData()) g.setSpecies(null);
        population.clear();
    }

    public void compete() {

        double sum = 0; // accumulate each genome's score
        for (Genome g : population.getData()) {

            g.express(); // get most updated network

            for (int e = 0; e < numEps; ++e) {
                // reset environment and agents to sample initial state
                simulator.reset(bluePrint, g, null);
                // progress simulator until end of episode
                while (true) if (simulator.step() != 0) break;
            }

            int latest = simulator.getHScore(); // human's latest episode score
            g.setScore(latest);
            sum += latest;

        }
        score = sum / size(); // species score is average population scores

    }

    public void randElect() {
        // extinct the population and restart
        // the species with a new representative
        representative = population.getRandom();
        extinct();
        population.add(representative);
        representative.setSpecies(this);
        score = 0;
    }

    public void evict() {
        population.getData().sort(Comparator.comparingDouble(Genome::getScore));
        int part = (int)(Neat.evictRate * size());
        for (int i = 0; i < part; ++i) {
            population.get(0).setSpecies(null);
            population.remove(0);
        }
    }

    public Genome reproduce() {
        // generate offspring from random fittest genome
        RandomSelector<Genome> mator = new RandomSelector<>();
        for (Genome g : population.getData()) mator.add(g, g.getScore());
        Genome g1 = mator.random();
        Genome g2 = mator.random();
        Genome offspring;
        if (g1.getScore() > g2.getScore()) offspring = Neat.crossBreed(g1, g2);
        else offspring = Neat.crossBreed(g2, g1);
        return offspring;
    }

    @Override
    public String toString() {
        String rep = "<Species:/rep=" + representative + "/score=" + (Math.round(score * 100.0) / 100.0) + "/size=" + size();
        //for (int i = 0; i < size(); ++i) rep += "\n" + (i + 1) + ":" + population.get(i);
        return rep + "/>";
    }

}
