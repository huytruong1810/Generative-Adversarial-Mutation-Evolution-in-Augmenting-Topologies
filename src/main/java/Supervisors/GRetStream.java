package Supervisors;

import org.ejml.simple.SimpleMatrix;

import java.util.ArrayList;

public class GRetStream {

    private final ArrayList<GRetUnit> retUnits;

    public GRetStream(SimpleMatrix initialA) {
        retUnits = new ArrayList<>() {{
            add(new GRetUnit(-1, null, null, initialA.copy()));
        }};
    }

    public GRetUnit get(int i) {
        return retUnits.get(i);
    }

    public GRetUnit peek() {
        return retUnits.get(retUnits.size() - 1);
    }

    public void prematureAdd(GRetUnit unit) {
        // assume that the adj matrix hasn't changed since last time step
        GRetUnit prev = peek();
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
