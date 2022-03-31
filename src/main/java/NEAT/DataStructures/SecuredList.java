package NEAT.DataStructures;

import NEAT.Genome.Gene;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class SecuredList<T> {

    private final HashSet<T> set; // supports efficient gene look up
    private final List<T> data; // supports gene ordered retrieval

    public SecuredList() {
        set = new HashSet<>();
        data = new ArrayList<>();
    }

    public void add(T o) {
        if (set.contains(o)) throw new IllegalStateException(o + " already in " + set + ".");
        set.add(o);
        data.add(o);
    }

    public void addInOrder(T o) {
        if (set.contains(o)) throw new IllegalStateException(o + " already in " + set + ".");
        for (int i = 0; i < size(); ++i) {
            int in = ((Gene)data.get(i)).getIN();
            if (((Gene)o).getIN() < in) {
                data.add(i, o);
                set.add(o);
                return;
            }
            else if (((Gene)o).getIN() == in)
                throw new IllegalStateException(o + " already in " + set + ".");
        }
        add(o); // when container is empty or object is the largest
    }

    public boolean contains(T o) {
        return set.contains(o);
    }

    public T getRandom() {
        if (set.size() > 0)
            return data.get((int)(Math.random()*size()));
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
        return data.get(i);
    }

    public T get(T o) {
        for (int i = 0; i < size(); ++i) {
            if (data.get(i).equals(o)) return data.get(i);
        }
        throw new IllegalStateException("Invalid search for " + o + " in " + data + ".");
    }

    public void remove(int i) {
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
