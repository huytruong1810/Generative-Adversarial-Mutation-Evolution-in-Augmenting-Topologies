package GANAS;

import org.ejml.simple.SimpleMatrix;

import java.util.ArrayList;

public class TimeStream {

    private boolean shaven;
    private final ArrayList<TimeStep> retUnits;

    public TimeStream(SimpleMatrix initialA) {
        retUnits = new ArrayList<>() {{
            add(new TimeStep(-1, null, null, initialA.copy()));
        }};
        shaven = false;
    }

    public void shave() {
        // Generator keeps its own internal hidden states so -1 unit can be thrown away safely
        TimeStep unit = retUnits.remove(0); // shave off the -1 unit
        if (unit.getTime() != -1) throw new IllegalStateException("Incorrect unit has been shaved.");
        shaven = true;
    }

    public TimeStep get(int i) {
        if (!shaven) throw new IllegalStateException("Cannot freestyle access stream if not shaven.");
        return retUnits.get(i);
    }

    public TimeStep peek() {
        return retUnits.get(retUnits.size() - 1);
    }

    public void prematureAdd(TimeStep unit) {
        // assume that the adj matrix hasn't changed since last time step
        TimeStep prev = peek();
        if (unit.getTime() != prev.getTime() + 1)
            throw new IllegalStateException("Segmentation in Generator Return Stream.");
        unit.setA(prev.getA().copy());
        retUnits.add(unit);
    }

    public void overrideA(SimpleMatrix ARef) {
        // this method is called when the adj matrix is actually changed
        peek().setA(ARef.copy());
    }

    public int size() {
        return retUnits.size();
    }

}
