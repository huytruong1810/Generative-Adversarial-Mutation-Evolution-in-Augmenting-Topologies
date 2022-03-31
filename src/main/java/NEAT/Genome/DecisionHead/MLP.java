package NEAT.Genome.DecisionHead;

import NEAT.DataStructures.SecuredList;

import java.util.Arrays;
import java.util.HashMap;

import static NEAT.Lambdas.Activations.Type.*;
import static NEAT.NEAT.*;

public class MLP {

    private final DHng[] inputL, hiddenL, outputL;

    public MLP(DHg g) {

        int inputNum = g.getInputNum();
        int outputNum = g.getOutputNum();

        SecuredList<DHng> nodes = g.getNodes();
        int numNodes = nodes.size(), inIndex, hidIndex, outIndex;

        inputL = new DHng[inputNum];
        hiddenL = new DHng[numNodes - inputNum - outputNum];
        outputL = new DHng[outputNum];

        inIndex = hidIndex = outIndex = 0;
        for (int i = 0; i < numNodes; ++i) {
            DHng n = nodes.get(i);
            double region = n.getX(); // classify them to respective layers
            if (region == hiddenNodeX) {
                // this layer one-to-one connects with the MRU
                inputL[inIndex++] = n;
            }
            else if (region > hiddenNodeX && region < outputNodeX) {
                // actor and critic both use tanh activation for hidden nodes
                if (n.getA('a') == LINEAR) n.setActivation(RELU, 'a');
                if (n.getA('c') == LINEAR) n.setActivation(RELU, 'c');
                if (n.getA('s') == LINEAR) n.setActivation(RELU, 's');
                hiddenL[hidIndex++] = n;
            }
            else if (region == outputNodeX) {
                // actor uses softmax activation for classification
                // while leaving critic and seer to use linear activation for regression
                if (n.getA('a') == LINEAR) n.setActivation(SOFTMAX, 'a');
                outputL[outIndex++] = n;
            }
        }
        Arrays.sort(hiddenL);

    }

    public double[] forward(int t, double[] x, char select) {

        for (int i = 0, n = x.length; i < n; ++i) inputL[i].overrideOutput(t, x[i], select);
        for (DHng hiddenNode : hiddenL) hiddenNode.feed(t, select);

        switch (select) {
            case 'a': // softmax activation of actor output nodes
                double[] probs = new double[actorOI.length];
                double S = 0;
                double m = Double.NEGATIVE_INFINITY; // stable softmax
                for (int aI : actorOI) {
                    double out = outputL[aI].weightedSum(t, 'a');
                    probs[aI] = out;
                    if (out > m) m = out;
                }
                for (int aI : actorOI) {
                    double stable_exp = Math.exp(probs[aI] - m);
                    probs[aI] = stable_exp;
                    S += stable_exp; // accumulate sum of exp
                }
                for (int aI : actorOI) probs[aI] /= S; // normalize
                return probs;
            case 'c': // only consider 1 critic output node
                int cI = criticOI[0];
                outputL[cI].feed(t, 'c');
                return new double[] {outputL[cI].getOutput(t, 'c')};
            case 's': // consider all seer output nodes
                int cut = actorOI.length + criticOI.length;
                double[] preds = new double[seerOI.length];
                for (int sI : seerOI) {
                    outputL[sI].feed(t, 's');
                    preds[sI - cut] = outputL[sI].getOutput(t, 's');
                }
                return preds;
            default: throw new IllegalStateException("Unknown select.");
        }

    }

    /**
     * use Softmax gradient to update network's parameters
     * @param t - the time step for BPTT
     * @param A - advantage value
     * @param probs - the action probability distribution
     * @param c - the correct class, that is, the taken-action-index at t
     * @param backGrads - for collecting MRU gradients at t
     */
    public void actorBP(int t, double A, double[] probs, int c, HashMap<Integer, Double> backGrads) {
        for (int aI : actorOI)
            outputL[aI].backPropdL_dz(t, A * (aI != c ? -probs[aI] : 1 - probs[c]), 'a', backGrads);
    }

    /**
     * use Mean Squared Error gradient to update network's parameters
     * @param t - the time step for BPTT
     * @param dE_da - guaranteed to be only for the critic regression node at t
     * @param backGrads - for collecting MRU gradients at t
     */
    public void criticBP(int t, double dE_da, HashMap<Integer, Double> backGrads) {
        outputL[criticOI[0]].backProp(t, dE_da, 'c', backGrads);
    }

    /**
     * use Mean Squared Error gradient to update network's parameters
     * @param t - the time step for BPTT
     * @param dE_da - guaranteed to be only for the seer regression nodes at t
     * @param backGrads - for collecting MRU gradients at t
     */
    public void seerBP(int t, double[] dE_da, HashMap<Integer, Double> backGrads) {
        int cut = actorOI.length + criticOI.length;
        for (int sI : seerOI)
            outputL[sI].backProp(t, dE_da[sI - cut], 's', backGrads);
    }

}
