package NEAT;

import Environment.Simulators.TestRoom;
import Environment.Simulators.TrainRoom;
import NEAT.DataStructures.SecuredList;
import NEAT.Genome.DecisionHead.DHcg;
import NEAT.Genome.MemoryHead.MHcg;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static NEAT.NEAT.*;

public class Species {

    private static int GlobalID = 1;
    private final int ID, predID;

    private double score;
    private Individual repr;
    private final SecuredList<Individual> population;

    private Individual champion;

    public Species(Individual i, int pred) {
        ID = GlobalID++;
        predID = pred;
        score = 0;
        population = new SecuredList<>();
        champion = null; // only become relevant after compete
        i.setSpecies(this); repr = i; population.add(i);
    }

    public int size() { return population.size(); }
    public int getID() { return ID; }
    public double getScore() { return score; }
    public Individual getRepr() { return repr; }
    public Individual getChampion() { return champion; }

    public void recruit(Individual i) {
        i.setSpecies(this);
        population.add(i);
    }

    public void compete(char[][][] unifiedBlueprint) {

        int n = size();
        ExecutorService threadPool = Executors.newWorkStealingPool();
        ArrayList<Future<Double>> futures = new ArrayList<>(n);

        for (int i = 0; i < n; ++i) { // submit concurrent evaluators for work distribution among cores
            TrainRoom trainRoom = new TrainRoom(worldDim, timeHorizon);
            TestRoom testRoom = new TestRoom(worldDim, timeHorizon);
            futures.add(i, threadPool.submit(new Evaluator(population.get(i), trainRoom, testRoom, unifiedBlueprint)));
        }

        // all competitors are equal before the competition
        champion = null;
        double speciesSum = 0; // accumulate each individual's score
        try {
            for (int i = 0; i < n; ++i) {
                double avgScore = futures.get(i).get(); // blocking call until evaluator i-th finishes
                // change champion when better is found or first time announcing one
                if (champion == null) champion = population.get(i);
                else { // repr can also become the champion
                    if (champion.getScore() <= avgScore) champion = population.get(i); // bubble champion up last because evict
                }
                speciesSum += avgScore;
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        score = speciesSum / size(); // species score is average population scores

    }

    public void extinct() { // removal of the species should happen right after
        // keep the corpses for replacement
        for (Individual i : population.getData()) i.setSpecies(null);
        repr = null;
        champion = null; // the only case where champion is gone
        population.clear();
    }

    public void evict() { // champion is never evicted because of its max score
        population.getData().sort(Comparator.comparingDouble(Individual::getScore));
        int part = (int) (NEAT.evictRate * size());
        for (int i = 0; i < part; ++i) {
            Individual toBeEvicted = population.get(0); // this could be repr
            if (toBeEvicted == champion) throw new IllegalStateException("Champion cannot be evicted.");
            toBeEvicted.setSpecies(null);
            population.remove(0);
            // repr is gone so re-elect another within species if needed
            if (toBeEvicted == repr) repr = population.getRandom();
        }
    }

    /**-----------------------------------------------------------------------------------------------------------------
     * Compute and return the genomic distance between the representative
     * and the individual basing on the connection genes
     * @param individual - the individual to check with
     * @return their genomic distance
     */
    public double calComp(Individual individual) {

        // only happen when the ind was asexually reproduced from the representative
        // the chance of it is very small
        if (repr == individual) return 0.0;

        int D = 0; // number of genetic disjoints
        int N = 1; // largest number of gene
        int E = 0; // number of excess gene

        // compute genomic distance between 2 MRUs
        List<MHcg> mruc1 = individual.getMRU().getCons().getData(), mruc2 = repr.getMRU().getCons().getData();
        int i = 0, j = 0, s1 = mruc1.size(), s2 = mruc2.size();
        N += Math.max(s1, s2);
        while (i < s1 && j < s2) {
            MHcg c1 = mruc1.get(i), c2 = mruc2.get(j);
            int in1 = c1.getIN(), in2 = c2.getIN();
            if (in1 == in2) { i++; j++; }
            else if (in1 > in2) { D++; j++; }
            else { D++; i++; }
        }
        E += (s1 > s2) ? (s1 - i) : (s2 - j);

        // compute genomic distance between 2 ACUs
        List<DHcg> acuc1 = individual.getACU().getCons().getData(), acuc2 = repr.getACU().getCons().getData();
        i = 0; j = 0; s1 = acuc1.size(); s2 = acuc2.size();
        N += Math.max(s1, s2);
        while (i < s1 && j < s2) {
            DHcg c1 = acuc1.get(i), c2 = acuc2.get(j);
            int in1 = c1.getIN(), in2 = c2.getIN();
            if (in1 == in2) { i++; j++; }
            else if (in1 > in2) { D++; j++; }
            else { D++; i++; }
        }
        E += (s1 > s2) ? (s1 - i) : (s2 - j);

        return (C1 * E + C2 * D) / N;

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
