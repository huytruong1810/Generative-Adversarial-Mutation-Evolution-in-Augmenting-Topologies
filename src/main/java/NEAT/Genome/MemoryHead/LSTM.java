package NEAT.Genome.MemoryHead;

import NEAT.DataStructures.SecuredList;
import NEAT.DataStructures.MemUnit;

import java.util.Arrays;
import java.util.HashMap;

import static NEAT.Lambdas.Activations.dtanh;
import static NEAT.Lambdas.Activations.tanh;
import static NEAT.NEAT.hiddenNodeX;
import static NEAT.NEAT.inputNodeX;

public class LSTM {

    private final int inputNum, outputNum;
    private final MHng[] inputL, hiddenL, outputL;

    private final HashMap<Integer, HashMap<Integer, double[]>> actorGateOutputsOverTime;
    private final HashMap<Integer, HashMap<Integer, double[]>> criticGateOutputsOverTime;
    private final HashMap<Integer, HashMap<Integer, double[]>> seerGateOutputsOverTime;

    public LSTM(MHg g) {

        inputNum = g.getInputNum();
        outputNum = g.getOutputNum();

        SecuredList<MHng> nodes = g.getNodes();
        int numNodes = nodes.size(), inIndex = 0, hidIndex = 0, outIndex = 0;

        inputL = new MHng[inputNum];
        hiddenL = new MHng[numNodes - inputNum - outputNum];
        outputL = new MHng[outputNum];
        for (int i = 0; i < numNodes; ++i) {
            MHng n = nodes.get(i);
            double region = n.getX();
            if (region == inputNodeX) inputL[inIndex++] = n;
            else if (region > inputNodeX && region < hiddenNodeX) hiddenL[hidIndex++] = n;
            else if (region == hiddenNodeX) outputL[outIndex++] = n;
        }
        Arrays.sort(hiddenL);

        actorGateOutputsOverTime = new HashMap<>();
        criticGateOutputsOverTime = new HashMap<>();
        seerGateOutputsOverTime = new HashMap<>();

    }

    private HashMap<Integer, HashMap<Integer, double[]>> getGateHashMap(char select) {
        switch (select) {
            case 'a': return actorGateOutputsOverTime;
            case 'c': return criticGateOutputsOverTime;
            case 's': return seerGateOutputsOverTime;
            default: throw new IllegalStateException("Unknown select.");
        }
    }

    public MemUnit forward(MemUnit prevUnit, double[] input, char select) {

        int t = prevUnit.t() + 1;

        // this is safe because output layer is always in order
        double[] Ct = new double[outputNum], ht = new double[outputNum], tanhCt = new double[outputNum];
        double[] prevCt = prevUnit.C(select);
        double[] prevHt = prevUnit.h(select);

        // for collecting gate's outputs of each output nodes
        HashMap<Integer, double[]> outputsOverNodes = new HashMap<>();

        // h_prev is stack onto x
        for (int i = 0; i < outputNum; ++i) inputL[i].overrideGatesOutputs(t, prevHt[i], select);
        for (int i = outputNum; i < inputNum; ++i) inputL[i].overrideGatesOutputs(t, input[i - outputNum], select);
        // activate hidden nodes of all gates
        for (MHng node : hiddenL) node.feedAll(t, select);
        // activate output nodes of all gates and collect those outputs at equivalent nodes
        for (int i = 0; i < outputNum; ++i) {
            outputL[i].feedAllAndPutInto(outputsOverNodes, t, select);
            double[] outputs = outputsOverNodes.get(outputL[i].getIN());
            // c = f * c_prev + i * c_bar
            Ct[i] = outputs[0] * prevCt[i] + outputs[1] * outputs[2];
            tanhCt[i] = tanh.apply(Ct[i]); // cache for BPTT
            // h = o * np.tanh(c)
            ht[i] = outputs[3] * tanhCt[i];
        }

        getGateHashMap(select).put(t, outputsOverNodes); // time log for BPTT

        return new MemUnit(t, input, ht, Ct, tanhCt, select); // return the new memory unit

    }

    /**
     * apply Back Propagate Through Time to all gates for actor, critic, and seer at the same time
     */
    public void BPTT(MemUnit memUnit, MemUnit prevUnit,
                     double[] dL_dh_actor, double[] dL_dh_critic, double[] dL_dh_seer,
                     double[] dL_dCnext, double[] dL_dhnext) {

        int t = memUnit.t();

        double[] atanhCt = memUnit.tanhC('a');
        double[] ctanhCt = memUnit.tanhC('c');
        double[] stanhCt = memUnit.tanhC('s');

        double[] aCprevt = prevUnit.C('a');
        double[] cCprevt = prevUnit.C('c');
        double[] sCprevt = prevUnit.C('s');

        HashMap<Integer, double[]> actorGateOutputs = actorGateOutputsOverTime.get(t);
        HashMap<Integer, double[]> criticGateOutputs = criticGateOutputsOverTime.get(t);
        HashMap<Integer, double[]> seerGateOutputs = seerGateOutputsOverTime.get(t);

        // for collecting dhprev_x
        HashMap<Integer, double[]> backGrads = new HashMap<>();

        int seerCut = outputNum * 2;

        // for each output nodes
        for (int i = 0; i < outputNum; ++i) {
            int IN = outputL[i].getIN();
            double[] actorOutputs = actorGateOutputs.get(IN);
            double[] criticOutputs = criticGateOutputs.get(IN);
            double[] seerOutputs = seerGateOutputs.get(IN);
            // dh = dy + dh_next
            double adL_dhi = dL_dh_actor[i] + dL_dhnext[i];
            double cdL_dhi = dL_dh_critic[i] + dL_dhnext[i + outputNum];
            double sdL_dhi = dL_dh_seer[i] + dL_dhnext[i + seerCut];
            // dc = dh * o * (1 - tanh(c)^2) + dc_next
            double adL_dCi = adL_dhi * actorOutputs[3] * dtanh.apply(atanhCt[i]) + dL_dCnext[i];
            double cdL_dCi = cdL_dhi * criticOutputs[3] * dtanh.apply(ctanhCt[i]) + dL_dCnext[i + outputNum];
            double sdL_dCi = sdL_dhi * seerOutputs[3] * dtanh.apply(stanhCt[i]) + dL_dCnext[i + seerCut];
            // do = dh * np.tanh(c)
            // dc_bar = dc * i
            // di = dc * c_bar
            // df = dc * c_prev
            outputL[i].backProp(
                    adL_dCi * aCprevt[i], adL_dCi * actorOutputs[2], adL_dCi * actorOutputs[1], adL_dhi * atanhCt[i],
                    cdL_dCi * cCprevt[i], cdL_dCi * criticOutputs[2], cdL_dCi * criticOutputs[1], cdL_dhi * ctanhCt[i],
                    sdL_dCi * sCprevt[i], sdL_dCi * seerOutputs[2], sdL_dCi * seerOutputs[1], sdL_dhi * stanhCt[i],
                    t, backGrads);
            // dc_prev = f * dc
            dL_dCnext[i] = adL_dCi * actorOutputs[0];
            dL_dCnext[i + outputNum] = cdL_dCi * criticOutputs[0];
            dL_dCnext[i + seerCut] = sdL_dCi * seerOutputs[0];
        }
        // at this point,
        // dL/dhprev_x is completely accumulated at the input layer of each gate
        for (int i = 0; i < outputNum; ++i) { // only collect dhprev
            int in = inputL[i].getIN();
            double[] gradTuple = backGrads.containsKey(in) ? backGrads.get(in) : new double[] {0.0, 0.0, 0.0};
            dL_dhnext[i] = gradTuple[0];
            dL_dhnext[i + outputNum] = gradTuple[1];
            dL_dhnext[i + seerCut] = gradTuple[2];
        }

        actorGateOutputsOverTime.get(t).clear(); // don't need this anymore
        criticGateOutputsOverTime.get(t).clear();
        seerGateOutputsOverTime.get(t).clear();

    }

}
