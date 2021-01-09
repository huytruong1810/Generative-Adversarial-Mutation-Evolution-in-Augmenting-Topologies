package Neat.Genome;

import Neat.DataStructures.RandomHashSet;
import java.util.Arrays;
import static Neat.Neat.*;

public class NeuralNetwork {

    private NodeGene[] inputL, hiddenL, outputL;

    public NeuralNetwork(Genome g) { // THIS DOES NOT NEED TO BE DONE ALL THE TIME

        RandomHashSet<NodeGene> nodes = g.getNodes();
        int numNodes = nodes.size(), inIndex = 0, hidIndex = 0, outIndex = 0;

        NodeGene[] ins = new NodeGene[inputN], outs = new NodeGene[outputN], hids = new NodeGene[numNodes - inputN - outputN];

        for (int i = 0; i < numNodes; ++i) {
            NodeGene n = nodes.get(i);
            if (n.getX() <= inputNodeX) ins[inIndex++] = n;
            else if (n.getX() >= outputNodeX) {
                // actor uses softmax activation for classification
                // while critic uses linear activation for regression
                if (n.getFa(true) == ' ') n.setActivation('o', true);
                if (n.getFa(false) == ' ') n.setActivation('l', false);
                outs[outIndex++] = n;
            }
            else {
                // actor and critic both use sigmoid activation for hidden nodes
                if (n.getFa(true) == ' ') n.setActivation('s', true);
                if (n.getFa(false) == ' ') n.setActivation('s', false);
                hids[hidIndex++] = n;
            }
        }
        Arrays.sort(hids);

        inputL = ins; hiddenL = hids; outputL = outs;

    }

    public double[] forward(double[] inputs, boolean selectActor) {

        // 0..n-1 are classification nodes, n is regression node
        int n = outputN - 1;

        for (int i = 0; i < inputN; ++i) inputL[i].setOutput(inputs[i]);
        for (NodeGene hiddenNode : hiddenL) hiddenNode.feed(selectActor);

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
     * @param inputs - input vector to network
     * @param dL_da - guaranteed to be of only the correct class
     * @param c - correct class == taken action
     */
    public void classificationFit(double[] inputs, double dL_da, int c) {

        int n = outputN - 1;
        double[] outputs = forward(inputs, true);
        double S = outputs[n * 2];
        double S_2 = Math.pow(S, 2);
        double e_xc = outputs[c + n];

        outputL[c].setSMG(e_xc * (S - e_xc) / S_2);
        for (int i = 0; i < n; ++i) { // only train nodes relating to sm nodes
            NodeGene outNode = outputL[i];
            if (i != c) outNode.setSMG(-e_xc * outputs[i + n] / S_2);
            outNode.backProp(dL_da, true);
        }

    }

    /**
     * use Mean Squared Error gradient to update network's parameters
     * relating to regression, should only be used on critic
     * @param inputs - input vector to network
     * @param dE_da - guaranteed to be for the regression node
     */
    public void regressionFit(double[] inputs, double dE_da) {

        // refresh network's memory on relevant input
        forward(inputs, false);
        // train only last node
        outputL[outputN - 1].backProp(dE_da, false);

    }

}
