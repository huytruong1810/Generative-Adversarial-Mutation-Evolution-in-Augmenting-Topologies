package NEAT.DataStructures;

import NEAT.Genome.Gene;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class GeneSet<T> {

    HashSet<T> set; // supports efficient data look up
    List<T> data; // supports data retrieval

    public GeneSet() {
        set = new HashSet<>();
        data = new ArrayList<>();
    }

    public void add(T o) {
        if (!set.contains(o)) {
            set.add(o);
            data.add(o);
        }
    }

    public void addAll(T ...objects) { for (T o : objects) add(o); }

    public void addInOrder(T o) { // only for gene
        for (int i = 0; i < size(); ++i) {
            int n = ((Gene)data.get(i)).getIN();
            if (((Gene)o).getIN() < n) {
                data.add(i, o);
                set.add(o);
                return;
            }
        }
        add(o); // container is empty
    }

    public boolean contains(T o) {
        return set.contains(o);
    }

    public T getRandom() {
        if (set.size() > 0) {
            return data.get((int)(Math.random()*size()));
        }
        return null;
    }

    public int size() {
        return data.size();
    }

    public void clear() {
        set.clear();
        data.clear();
    }

    public T get(int i) {
        if (i < 0 || i >= size()) {
            return null;
        }
        return data.get(i);
    }

    public T get(T o) {
        for (int i = 0; i < size(); ++i) {
            if (data.get(i).equals(o)) return data.get(i);
        }
        return null;
    }

    public void remove(int i) {
        if (i < 0 || i >= size()) {
            return;
        }
        set.remove(data.get(i));
        data.remove(i);
    }

    public void remove(T o) {
        set.remove(o);
        data.remove(o);
    }

    public List<T> getData() {
        return data;
    }

}
