package NEAT.Genome.MRU;

import NEAT.DataStructures.GeneSet;
import NEAT.DataStructures.MemUnit;

import java.util.Arrays;
import java.util.HashMap;

import static NEAT.NEAT.hiddenNodeX;
import static NEAT.NEAT.inputNodeX;

public class LSTM {

    private final int inputNum, outputNum;
    private final MRUNodeGene[] inputL, hiddenL, outputL;

    public LSTM(MRUGenome g) {

        inputNum = g.getInputNum();
        outputNum = g.getOutputNum();

        GeneSet<MRUNodeGene> nodes = g.getNodes();
        int numNodes = nodes.size(), inIndex = 0, hidIndex = 0, outIndex = 0;

        MRUNodeGene[] ins = new MRUNodeGene[inputNum];
        MRUNodeGene[] hids = new MRUNodeGene[numNodes - inputNum - outputNum];
        MRUNodeGene[] outs = new MRUNodeGene[outputNum];
        for (int i = 0; i < numNodes; ++i) {
            MRUNodeGene n = nodes.get(i);
            double region = n.getX();
            if (region == inputNodeX) ins[inIndex++] = n;
            else if (region > inputNodeX && region < hiddenNodeX) hids[hidIndex++] = n;
            else if (region == hiddenNodeX) outs[outIndex++] = n;
        }
        Arrays.sort(hids);
        inputL = ins; hiddenL = hids; outputL = outs;

    }

    public MemUnit forward(MemUnit prevUnit, double[] input) {

        double[] Ct = new double[outputNum], ht = new double[outputNum], tanhCt = new double[outputNum];
        double[] prevCt = prevUnit.C();

        // collect gate's outputs
        HashMap<Character, double[]> gateOutputs = forwardGates(input, prevUnit.h());

        for (int i = 0; i < outputNum; ++i) {
            Ct[i] = gateOutputs.get('f')[i] * prevCt[i] +
                    gateOutputs.get('i')[i] * gateOutputs.get('c')[i]; // compute new cell state
            tanhCt[i] = 2 / (1 + Math.exp(-2 * Ct[i])) - 1;
            ht[i] = gateOutputs.get('o')[i] * tanhCt[i]; // compute new hidden state
        }

        /** TESTING----------------------------------------------------------------------------------------------------=
        System.out.println("Ct:");
        for (double d : Ct) System.out.print(d + " ");
        System.out.println("\nht:");
        for (double d : ht) System.out.print(d + " ");
        System.out.println("\n____________");
         /** TESTING----------------------------------------------------------------------------------------------------=
         */

        return new MemUnit(prevUnit.t() + 1, input, ht, Ct, tanhCt); // return the new memory unit

    }

    private HashMap<Character, double[]> forwardGates(double[] xt, double[] prevHt) {

        HashMap<Character, double[]> gateOutputs = new HashMap<>() {{
            put('f', new double[outputNum]);
            put('i', new double[outputNum]);
            put('c', new double[outputNum]);
            put('o', new double[outputNum]);
        }};

        // prepare input for all gates
        int lenX = inputNum - outputNum;
        for (int i = 0; i < lenX; ++i) inputL[i].setOutput(xt[i]);
        for (int i = 0; i < outputNum; ++i) inputL[i + lenX].setOutput(prevHt[i]);
        // activate hidden nodes of all gates
        for (MRUNodeGene node : hiddenL) node.feedAll();
        // activate output nodes of all gates and collect those outputs
        for (int i = 0; i < outputNum; ++i) outputL[i].feedAndCollect(gateOutputs, i);

        return gateOutputs;

    }

    /**
     * apply Back Propagate Through Time for all gates
     */
    public void BPTT(MemUnit memUnit, MemUnit prevUnit, double[] dL_dh_acu, double[] dL_dCnext, double[] dL_dhnext,
                     HashMap<Integer, Double> fConGrads, HashMap<Integer, Double> iConGrads,
                     HashMap<Integer, Double> cConGrads, HashMap<Integer, Double> oConGrads) {

        double[] tanhCt = memUnit.tanhC();
        double[] Cprevt = prevUnit.C();

        // get gate's outputs and refresh hidden outputs on relevant time step
        HashMap<Character, double[]> gateOutputs = forwardGates(memUnit.X(), prevUnit.h());

        // for collecting dL/dhx
        HashMap<Integer, Double> backGrads = new HashMap<>();

        // for each output nodes
        for (int i = 0; i < outputNum; ++i) {

            double dL_dhi = dL_dh_acu[i] + dL_dhnext[i];
            double dL_dCi = dL_dhi * gateOutputs.get('o')[i] * (1 - Math.pow(tanhCt[i], 2)) + dL_dCnext[i];

            outputL[i].gradientFlow(
                    dL_dCi * Cprevt[i], dL_dCi * gateOutputs.get('c')[i],
                    dL_dCi * gateOutputs.get('i')[i], dL_dhi * tanhCt[i],
                    backGrads, fConGrads, iConGrads, cConGrads, oConGrads
            );

            // prepare dL/dC_next in place of old one
            dL_dCnext[i] = dL_dCi * gateOutputs.get('f')[i];

        }

        // dL/dxh_next should be completely accumulated at the entry of each gate by now
        // we only need dL/dh_next so keep only hidden part in place of old one
        int lenX = inputNum - outputNum;
        for (int i = 0; i < outputNum; ++i) {
            int IN = i + lenX + 1; // these are innovation numbers at input layer of MRU, so start from 1
            dL_dhnext[i] = (backGrads.containsKey(IN)) ? backGrads.get(IN) : 0;
        }

    }

}
