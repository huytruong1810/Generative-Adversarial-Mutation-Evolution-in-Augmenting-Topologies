package Neat.DataStructures;

import java.util.ArrayList;

public class RandomSelector<T> {

    private ArrayList<T> objects = new ArrayList<T>();
    private ArrayList<Double> scores = new ArrayList<>();

    private double total_score = 0;

    public void add(T o, double score) {
        objects.add(o);
        scores.add(score);
        total_score += score;
    }

    public T random() {
        double v = Math.random() * total_score;
        double c = 0;
        for (int i = 0; i < objects.size(); ++i) {
            c += scores.get(i);
            if (c > v)
                return objects.get(i);
        }
        return null;
    }

    public void clear() {
        objects.clear();
        scores.clear();
        total_score = 0;
    }

}
