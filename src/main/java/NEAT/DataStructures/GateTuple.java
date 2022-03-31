package NEAT.DataStructures;

import NEAT.Genome.Adam;

public class GateTuple {

    private double forgetValue;
    private double inputValue;
    private double candidateValue;
    private double outputValue;

    private Adam.Moment forgetMoment;
    private Adam.Moment inputMoment;
    private Adam.Moment candidateMoment;
    private Adam.Moment outputMoment;

    public GateTuple() {
        // initialize each value independently and uniformly from -1 to 1
        forgetValue = Math.random() * 2 - 1;
        inputValue = Math.random() * 2 - 1;
        candidateValue = Math.random() * 2 - 1;
        outputValue = Math.random() * 2 - 1;
        // these moments start at 0 according to Moment constructor
        forgetMoment = new Adam.Moment();
        inputMoment = new Adam.Moment();
        candidateMoment = new Adam.Moment();
        outputMoment = new Adam.Moment();
    }

    public GateTuple(GateTuple old, double p) {
        // p = 1 means memory past parameters, p = 0 means new random parameters
        forgetValue = p * (old != null ? old.forgetValue : 0) + (1 - p) * (Math.random() * 2 - 1);
        inputValue = p * (old != null ? old.inputValue : 0) + (1 - p) * (Math.random() * 2 - 1);
        candidateValue = p * (old != null ? old.candidateValue : 0) + (1 - p) * (Math.random() * 2 - 1);
        outputValue = p * (old != null ? old.outputValue : 0) + (1 - p) * (Math.random() * 2 - 1);
        // start new moments
        forgetMoment = new Adam.Moment();
        inputMoment = new Adam.Moment();
        candidateMoment = new Adam.Moment();
        outputMoment = new Adam.Moment();
    }

    public void ones() { // need not reset moments
        forgetValue = inputValue = candidateValue = outputValue = 1.0;
    }

    public double getVal(char selectGate) {
        switch (selectGate) {
            case 'f': return forgetValue;
            case 'i': return inputValue;
            case 'c': return candidateValue;
            case 'o': return outputValue;
            default: throw new IllegalStateException("Connection doesn't have gate specified.");
        }
    }

    public void optimizeValues(double f_dv, double i_dv, double c_dv, double o_dv) {
        forgetValue += Adam.optimize(f_dv, forgetMoment);
        inputValue += Adam.optimize(i_dv, inputMoment);
        candidateValue += Adam.optimize(c_dv, candidateMoment);
        outputValue += Adam.optimize(o_dv, outputMoment);
    }

    public String inspect() {
        return "\n***Forget gate: " + forgetValue +
                "\n***Input gate: " + inputValue +
                "\n***Candidate gate: " + candidateValue +
                "\n***Output gate: " + outputValue +
                "\n**Moments:" +
                "\n***Forget gate: " + forgetMoment.m + "/" + forgetMoment.v + "@" + forgetMoment.t +
                "\n***Input gate: " + inputMoment.m + "/" + inputMoment.v + "@" + inputMoment.t +
                "\n***Candidate gate: " + candidateMoment.m + "/" + candidateMoment.v + "@" + candidateMoment.t +
                "\n***Output gate: " + outputMoment.m + "/" + outputMoment.v + "@" + outputMoment.t;
    }

    @Override
    public GateTuple clone() throws CloneNotSupportedException {

        GateTuple theClone = new GateTuple();

        theClone.forgetValue = forgetValue;
        theClone.inputValue = inputValue;
        theClone.candidateValue = candidateValue;
        theClone.outputValue = outputValue;

        theClone.forgetMoment = forgetMoment.clone();
        theClone.inputMoment = inputMoment.clone();
        theClone.candidateMoment = candidateMoment.clone();
        theClone.outputMoment = outputMoment.clone();

        return theClone;

    }

    @Override
    public String toString() {
        return "(f" + (Math.round(forgetValue * 10.0) / 10.0) +
                ",i" + (Math.round(inputValue * 10.0) / 10.0) +
                ",c" + (Math.round(candidateValue * 10.0) / 10.0) +
                ",o" + (Math.round(outputValue * 10.0) / 10.0) + ")";
    }

}
