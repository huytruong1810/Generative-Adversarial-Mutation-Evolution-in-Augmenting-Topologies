package NEAT.DataStructures;

import NEAT.Individual;
import NEAT.Species;

import java.util.ArrayList;
import java.util.HashSet;

public class AncestorTree {

    public class Node {
        private final Species value;
        private final Individual structure;
        private boolean active;
        private final HashSet<Node> successors; // unordered set of unique successor nodes
        public Node(Species v, Individual s) { value = v; structure = s; active = true; successors = new HashSet<>(); }
        public Species getValue() { return value; }
        public Individual getStructure() { return structure; }
        public boolean getActive() { return active; }
        public HashSet<Node> getSuccessors() { return successors; }
        private void stop() { active = false; }
    }

    private Node root;

    public AncestorTree() { reset(); }

    public void reset() { root = new Node(null, null); }

    public Node getRoot() { return root; }

    public Node find(Species value) { // no need to care about visited nodes because of tree structure
        ArrayList<Node> toExplore = new ArrayList<>();
        toExplore.add(root);
        while (!toExplore.isEmpty()) {
            Node cur = toExplore.remove(0);
            if (cur.value == value) return cur;
            toExplore.addAll(cur.successors);
        }
        throw new IllegalStateException("Key " + value + " doesn't exist.");
    }

    public void add(Species pred, Species cur) throws CloneNotSupportedException {
        find(pred).successors.add(new Node(cur, cur.getRepr().clone()));
    }

    public void stop(Species value) { find(value).stop(); }

}
