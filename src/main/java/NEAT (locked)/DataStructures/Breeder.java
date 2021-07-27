package NEAT.DataStructures;

import java.util.ArrayList;

public class Breeder<T> {

    private ArrayList<T> objects = new ArrayList<T>();
    private ArrayList<Double> scores = new ArrayList<>();
    private double total = 0;

    public void add(T o, double score) {
        double exp = Math.exp(score);
        objects.add(o);
        scores.add(exp);
        total += exp;
    }

    public T random() {
        double num = Math.random(), c = 0;
        for (int i = 0, n = objects.size(); i < n; ++i) {
            c += scores.get(i) / total;
            if (num < c) return objects.get(i);
        }
        throw new IllegalStateException("Random selector fails.");
    }

    public void clear() {
        objects.clear();
        scores.clear();
        total = 0;
    }

}
