package NEAT.DataStructures;

import java.util.ArrayList;

public class MemStream {

    private int maxTime; // equivalent to latest valid index, can only be incremented over lifespan of stream
    private boolean shaved; // is the [t = -1] unit shaved off?
    private MemUnit beginUnit; // to store the [t = -1] unit when it's shaved off
    private MemUnit endUnit; // store the end padding of memory stream
    private final ArrayList<MemUnit> memUnits;

    public MemStream(int dim) {
        MemUnit initActorUnit = new MemUnit(-1, null, new double[dim], new double[dim], null, 'a');
        MemUnit initCriticUnit = new MemUnit(-1, null, new double[dim], new double[dim], null, 'c');
        MemUnit initSeerUnit = new MemUnit(-1, null, new double[dim], new double[dim], null, 's');
        initActorUnit.complete(initCriticUnit, initSeerUnit);
        memUnits = new ArrayList<>() {{
            add(initActorUnit);
        }};
        maxTime = 0; // index 0 is the [t = -1] unit above
        shaved = false; // always initially put [t = -1] unit in
    }

    public void clear() {
        memUnits.clear();
        maxTime = -1;
        shaved = true; // because no [t = -1]
        beginUnit = null;
    }

    public int maxTime() { return maxTime; }

    public MemUnit recent() { return memUnits.get(maxTime); }

    public void shave() { // shave off [t = -1] unit and fix max time
        beginUnit = memUnits.remove(0); // shave off first unit and cache it for BPTT
        // create end unit having the same state as the terminal seer unit
        // because observations don't change after end time
        // end unit does not have to be complete
        endUnit = new MemUnit(maxTime--, recent().X('s').clone(), null, null, null, 's');
        if (beginUnit.t() != -1)
            throw new IllegalStateException("Illegal unit has been shaved off.");
        shaved = true;
    }

    public MemUnit get(int t) {
        if (!shaved)
            throw new IllegalStateException("Cannot invoke this method on unshaven stream.");
        return memUnits.get(t);
    }

    public void push(MemUnit memUnit) {
        if (!memUnit.isComplete()) throw new IllegalStateException("Cannot add incomplete unit to stream.");
        if (memUnit.t() != maxTime++) throw new IllegalStateException("Memory should not be segmented.");
        memUnits.add(memUnit);
    }

    public MemUnit beginUnit() {
        if (beginUnit == null)
            throw new IllegalStateException("Begin unit has not been collected. Maybe stream is unshaven.");
        return beginUnit;
    }

    public MemUnit endUnit() {
        if (endUnit == null)
            throw new IllegalStateException("End unit has not been created. Maybe stream is unshaven.");
        return endUnit;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (MemUnit u : memUnits) str.append(u).append("\n");
        return str.toString();
    }

}
