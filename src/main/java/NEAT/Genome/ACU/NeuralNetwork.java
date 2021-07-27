package NEAT.Genome.ACU;

import NEAT.DataStructures.GeneSet;

import java.util.Arrays;
import java.util.HashMap;

import static NEAT.NEAT.*;

public class NeuralNetwork {

    private final int inputNum, outputNum;
    private final ACUNodeGene[] inputL, hiddenL, outputL;

    public NeuralNetwork(ACUGenome g) {

        inputNum = g.getInputNum();
        outputNum = g.getOutputNum();

        GeneSet<ACUNodeGene> nodes = g.getNodes();
        int numNodes = nodes.size(), inIndex = 0, hidIndex = 0, outIndex = 0;

        ACUNodeGene[] ins = new ACUNodeGene[inputNum], hids = new ACUNodeGene[numNodes - inputNum - outputNum], outs = new ACUNodeGene[outputNum];

        for (int i = 0; i < numNodes; ++i) {
            ACUNodeGene n = nodes.get(i);
            double region = n.getX();
            if (region == hiddenNodeX) {
                // this layer one-to-one connects with the MRU
                ins[inIndex++] = n;
            }
            else if (region > hiddenNodeX && region < outputNodeX) {
                // actor and critic both use sigmoid activation for hidden nodes
                if (n.getFa(true) == ' ') n.setActivation('s', true);
                if (n.getFa(false) == ' ') n.setActivation('s', false);
                hids[hidIndex++] = n;
            }
            else if (region == outputNodeX) {
                // actor uses softmax activation for classification
                // while critic uses linear activation for regression
                if (n.getFa(true) == ' ') n.setActivation('o', true);
                if (n.getFa(false) == ' ') n.setActivation('l', false);
                outs[outIndex++] = n;
            }
        }
        Arrays.sort(hids);

        inputL = ins; hiddenL = hids; outputL = outs;

    }

    public double[] forward(double[] x, boolean selectActor) {

        // 0..n-1 are classification nodes, n is regression node
        int n = outputNum - 1;

        for (int i = 0; i < inputNum; ++i) inputL[i].setOutput(x[i]);
        for (ACUNodeGene hiddenNode : hiddenL) hiddenNode.feed(selectActor);

        if (selectActor) { // only consider classification nodes
            // [0..n-1] for sm(z) and [n..2n-1] for exp(z) and [2n] for sum of exp(z)
            double[] outputs = new double[n * 2 + 1];
            double S = 0;
            for (int i = n; i < n * 2; ++i) { // save each exp
                outputs[i] = outputL[i - n].expWeightedSum();
                S += outputs[i]; // accumulate sum of exp(z)
            }
            for (int i = 0; i < n; ++i) outputs[i] = outputs[i + n] / S;
            outputs[n * 2] = S; // save the sum of exp(z)
            return outputs;
        }
        else { // only consider regression node
            outputL[n].feed(false);
            return new double[]{outputL[n].getOutput()};
        }

    }

    /**
     * use Softmax gradient to update network's parameters
     * relating to classification, should only be used on actor
     * @param x - input vector to network
     * @param dL_da - guaranteed to be of only the correct class
     * @param c - correct class == taken action
     */
    public void classificationFit(double[] x, double dL_da, int c,
                                  HashMap<Integer, Double> conGrads, HashMap<Integer, Double> backGrads) {

        // refresh network node's outputs on relevant input for actor
        double[] outputs = forward(x, true);

        int n = outputNum - 1;
        double S = outputs[n * 2];
        double S_2 = Math.pow(S, 2);
        double e_xc = outputs[c + n];

        outputL[c].setSMG(e_xc * (S - e_xc) / S_2);
        for (int i = 0; i < n; ++i) { // only train nodes relating to sm nodes
            ACUNodeGene outNode = outputL[i];
            if (i != c) outNode.setSMG(-e_xc * outputs[i + n] / S_2);
            outNode.gradientFlow(dL_da, true, backGrads, conGrads);
        }

        /** TESTING----------------------------------------------------------------------------------------------------=
        System.out.println("actor weight gradient: " + conGrads);
        System.out.println("actor back gradient: " + backGrads);
         /** TESTING----------------------------------------------------------------------------------------------------=
         */

    }

    /**
     * use Mean Squared Error gradient to update network's parameters
     * relating to regression, should only be used on critic
     * @param x - input vector to network
     * @param dE_da - guaranteed to be only for the regression node
     */
    public void regressionFit(double[] x, double dE_da,
                              HashMap<Integer, Double> conGrads, HashMap<Integer, Double> backGrads) {

        // refresh network node's outputs on relevant input for critic
        forward(x, false);

        // train only last node
        outputL[outputNum - 1].gradientFlow(dE_da, false, backGrads, conGrads);

        /** TESTING----------------------------------------------------------------------------------------------------=
        System.out.println("critic weight gradient: " + conGrads);
        System.out.println("critic back gradient: " + backGrads);
        /** TESTING----------------------------------------------------------------------------------------------------=
         */

    }

}
