package NEAT.Genome.MRU;

import NEAT.Genome.Gene;
import java.util.ArrayList;
import java.util.HashMap;

import static NEAT.NEAT.*;

public class MRUNodeGene extends Gene implements Comparable<MRUNodeGene> {

    private double x, y;
    private double forgetO, inputO, candidateO, outputO,
            forgetB, inputB, candidateB, outputB;
    private ArrayList<MRUConGene> inCons;

    public MRUNodeGene(int IN) {
        super(IN);
        inCons = new ArrayList<>();
    }

    public void feedAll() {

        double fWeightedSum, iWeightedSum, cWeightedSum, oWeightedSum;
        fWeightedSum = iWeightedSum = cWeightedSum = oWeightedSum = 0;

        for (MRUConGene c : inCons) {
            if (c.isEnabled()) {
                fWeightedSum += (c.getFG().forgetO * c.getWeight('f'));
                iWeightedSum += (c.getFG().inputO * c.getWeight('i'));
                cWeightedSum += (c.getFG().candidateO * c.getWeight('c'));
                oWeightedSum += (c.getFG().outputO * c.getWeight('o'));
            }
        }

        forgetO = 1 / (1 + Math.exp(-(fWeightedSum + forgetB)));
        inputO = 1 / (1 + Math.exp(-(iWeightedSum + inputB)));
        candidateO = 2 / (1 + Math.exp(-2 * (cWeightedSum + candidateB))) - 1;
        outputO = 1 / (1 + Math.exp(-(oWeightedSum + outputB)));

    }

    public void feedAndCollect(HashMap<Character, double[]> gateOutputs, int index) {
        feedAll();
        gateOutputs.get('f')[index] = forgetO;
        gateOutputs.get('i')[index] = inputO;
        gateOutputs.get('c')[index] = candidateO;
        gateOutputs.get('o')[index] = outputO;
    }

    public void gradientFlow(double dL_df, double dL_di, double dL_dc, double dL_do, HashMap<Integer, Double> backGrads,
                             HashMap<Integer, Double> wfGrads, HashMap<Integer, Double> wiGrads,
                             HashMap<Integer, Double> wcGrads, HashMap<Integer, Double> woGrads) {

        if (x == inputNodeX) {
            int IN = this.IN; // accumulate back gradient for previous dL_dh
            double dL_da = dL_df + dL_di + dL_dc + dL_do;
            backGrads.put(IN, backGrads.containsKey(IN) ? (backGrads.get(IN) + dL_da) : dL_da);
            return;
        }

        double df_dzf = forgetO * (1 - forgetO);
        double di_dzi = inputO * (1 - inputO);
        double dc_dzc = 1 - Math.pow(candidateO, 2);
        double do_dzo = outputO * (1 - outputO);

        double dL_dzf = dL_df * df_dzf;
        double dL_dzi = dL_di * di_dzi;
        double dL_dzc = dL_dc * dc_dzc;
        double dL_dzo = dL_do * do_dzo;

        for (MRUConGene c : inCons) {

            if (!c.isEnabled()) continue;

            MRUNodeGene from = c.getFG();

//            double dL_dbf = dL_dzf * MRU_LR;
//            double dL_dbi = dL_dzi * MRU_LR;
//            double dL_dbc = dL_dzc * MRU_LR;
//            double dL_dbo = dL_dzo * MRU_LR;

            double dL_dwf = dL_dzf * from.forgetO * MRU_LR;
            double dL_dwi = dL_dzi * from.inputO * MRU_LR;
            double dL_dwc = dL_dzc * from.candidateO * MRU_LR;
            double dL_dwo = dL_dzo * from.outputO * MRU_LR;

            double dL_dxf = dL_dzf * c.getWeight('f');
            double dL_dxi = dL_dzi * c.getWeight('i');
            double dL_dxc = dL_dzc * c.getWeight('c');
            double dL_dxo = dL_dzo * c.getWeight('o');

//            // NOT SAFE because forward in invoked many times during training to refresh outputs on a specific time step
//             forgetB += dL_dbf;
//             inputB += dL_dbi;
//             candidateB += dL_dbc;
//             outputB += dL_dbo;

            int IN = c.getIN(); // accumulate weight gradient
            wfGrads.put(IN, wfGrads.containsKey(IN) ? (wfGrads.get(IN) + dL_dwf) : dL_dwf);
            wiGrads.put(IN, wiGrads.containsKey(IN) ? (wiGrads.get(IN) + dL_dwi) : dL_dwi);
            wcGrads.put(IN, wcGrads.containsKey(IN) ? (wcGrads.get(IN) + dL_dwc) : dL_dwc);
            woGrads.put(IN, woGrads.containsKey(IN) ? (woGrads.get(IN) + dL_dwo) : dL_dwo);

            from.gradientFlow(dL_dxf, dL_dxi, dL_dxc, dL_dxo, backGrads, wfGrads, wiGrads, wcGrads, woGrads);

        }

    }

    public double getX() { return x; }
    public double getY() { return y; }
    public ArrayList<MRUConGene> getInCons() { return inCons; }
    public void setX(double val) { x = val; }
    public void setY(double val) { y = val; }
    public void setOutput(double val) { forgetO = inputO = candidateO = outputO = val; }

    public boolean equals(Object o) {
        if (!(o instanceof MRUNodeGene)) return false;
        return IN == ((MRUNodeGene) o).IN;
    }

    @Override
    public String inspect() {
        return "\nGene Type: MRU-NodeGene" +
                "\nInnovation No.: " + IN +
                "\nX: " + x + "\nY: " + y +
                "\n*Parameters" +
                "\n**Biases" +
                "\n***Forget gate: " + forgetB +
                "\n***Input gate: " + inputB +
                "\n***Candidate gate: " + candidateB +
                "\n***Output gate: " + outputB;
    }

    @Override
    public int hashCode() { return IN; }

    @Override
    public int compareTo(MRUNodeGene o) {
        if (x > o.x) return -1;
        if (x < o.x) return 1;
        return 0;
    }

    @Override
    public MRUNodeGene clone() {
        MRUNodeGene theClone = new MRUNodeGene(IN);
        theClone.x = x;
        theClone.y = y;
        theClone.forgetB = forgetB;
        theClone.inputB = inputB;
        theClone.candidateB = candidateB;
        theClone.outputB = outputB;
        return theClone; // don't copy in connections
    }

    @Override
    public String toString() { return "(" + IN + ")"; }

}
