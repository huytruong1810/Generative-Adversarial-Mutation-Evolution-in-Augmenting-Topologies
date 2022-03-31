package NEAT.DataStructures;

import java.util.HashMap;

public class SecuredMap<K, V> {

    private final HashMap<K, V> map;
    private final HashMap<Integer, V> rootMap;

    public SecuredMap() {
        map = new HashMap<>();
        rootMap = new HashMap<>();
        rootMap.put(0, null); // add initial entry because IN starts at 1
    }

    public void clear() {
        map.clear();
        rootMap.clear();
        rootMap.put(0, null);
    }

    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    public V get(K key) {
        if (!map.containsKey(key)) throw new IllegalStateException("Key " + key.toString() + " doesn't exist.");
        return map.get(key);
    }

    public void put(K key, V value) {
        if (map.containsKey(key)) throw new IllegalStateException("Map already contains key " + key.toString() + ".");
        map.put(key, value);
    }

    public boolean containsRoot(int key) {
        return rootMap.containsKey(key);
    }

    public V getRoot(int key) {
        if (!rootMap.containsKey(key)) throw new IllegalStateException("Key " + key + " doesn't exist.");
        return rootMap.get(key);
    }

    public void putRoot(int key, V value) {
        if (!rootMap.containsKey(key - 1)) throw new IllegalStateException("New root must be sequential to previous roots.");
        rootMap.put(key, value);
    }

}
