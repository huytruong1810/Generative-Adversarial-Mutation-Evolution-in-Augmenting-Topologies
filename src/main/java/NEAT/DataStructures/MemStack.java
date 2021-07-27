package NEAT.DataStructures;

import java.util.ArrayList;

public class MemStack {

    private int top;
    private final ArrayList<MemUnit> memUnits;

    public MemStack() {
        top = -1;
        memUnits = new ArrayList<>();
    }

    public MemUnit peek() { return memUnits.get(top); }
    public MemUnit pop() { return memUnits.remove(top--); }
    public void push(MemUnit memUnit) {
        if (memUnit.t() != top++) throw new IllegalStateException("Memory should not be segmented.");
        memUnits.add(memUnit);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (MemUnit u : memUnits) str.append(u).append("\n");
        return str.toString();
    }

}
