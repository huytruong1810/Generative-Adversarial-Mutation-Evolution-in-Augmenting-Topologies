package NEAT.DataStructures;

import static NEAT.Lambdas.Activations.sig;

import java.util.ArrayList;

public class Ranker<T> {

    private final ArrayList<T> objects;
    private final ArrayList<Double> scores;
    private double total;

    public Ranker() {
        objects = new ArrayList<>();
        scores = new ArrayList<>();
        total = 0;
    }

    public void add(T o, double score) {
        // sigmoid the score to map all reals to positives
        double expScore = Math.exp(sig.apply(score));
        objects.add(o);
        scores.add(expScore);
        total += expScore;
    }

    public T bestRandom() {
        double p = Math.random(), c = 0;
        for (int i = 0, n = objects.size(); i < n; ++i) {
            c += scores.get(i) / total;
            if (p < c) return objects.get(i);
        }
        throw new IllegalStateException("Random selector fails. Scores: " + scores);
    }

    public void clear() {
        objects.clear();
        scores.clear();
        total = 0;
    }

}
