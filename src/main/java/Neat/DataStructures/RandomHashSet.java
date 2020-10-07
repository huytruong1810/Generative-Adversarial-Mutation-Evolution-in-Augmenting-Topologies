package Neat.DataStructures;

import Neat.Genome.Gene;

import java.util.ArrayList;
import java.util.HashSet;

public class RandomHashSet<T> {

    HashSet<T> set;
    ArrayList<T> data;

    public RandomHashSet() {
        set = new HashSet<>();
        data = new ArrayList<>();
    }

    public void add(T o) {
        if (!set.contains(o)) {
            set.add(o);
            data.add(o);
        }
    }

    public void addInOrder(T o) { // only for gene
        for (int i = 0; i < size(); ++i) {
            int n = ((Gene)data.get(i)).getIN();
            if (((Gene)o).getIN() < n) {
                data.add(i, o);
                set.add(o);
                return;
            }
        }
        data.add(o);
        set.add(o);
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

    public ArrayList<T> getData() {
        return data;
    }

}
