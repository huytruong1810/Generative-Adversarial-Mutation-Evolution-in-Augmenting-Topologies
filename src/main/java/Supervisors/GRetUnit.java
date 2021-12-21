package Supervisors;

import org.ejml.simple.SimpleMatrix;

public class GRetUnit {

    private final int time;
    private int reward, selectedA, selectedB;
    private final double[] aProbs, bProbs;
    private SimpleMatrix A;

    public GRetUnit(int t, double[] aP, double[] bP, SimpleMatrix ARef) {
        time = t;
        aProbs = aP;
        bProbs = bP;
        A = ARef;
    }

    public int getTime() {
        return time;
    }

    public int getReward() {
        return reward;
    }

    public int getSelectedA() {
        return selectedA;
    }

    public int getSelectedB() {
        return selectedB;
    }

    public double[] getAProbs() {
        return aProbs;
    }

    public double[] getBProbs() {
        return bProbs;
    }

    public SimpleMatrix getA() {
        return A;
    }

    public void setReward(int value) {
        reward = value;
    }

    public void setSelected(int aVal, int bVal) {
        selectedA = aVal;
        selectedB = bVal;
    }

    public void setA(SimpleMatrix ARef) {
        A = ARef;
    }

}
