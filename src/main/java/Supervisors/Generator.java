package Supervisors;

import Supervisors.BaseArchitectures.*;
import Supervisors.BaseArchitectures.Activations.Activation;
import org.ejml.simple.SimpleMatrix;

public class Generator {

    private final GCN encoder;
    // if there is no connection from a to b, mutate a connection in between
    // else if there already is one, mutate a node on that connection
    private final GRU latentGRU;
    private final MLP aSelector, bSelector;

    public Generator(int featureDim, int codeDim, int hiddenDim, int nodeIndices, double learningRate) {

        encoder = new GCN(nodeIndices, new int[] {featureDim, hiddenDim, codeDim}, learningRate);
        latentGRU = new GRU(new int[] {codeDim, hiddenDim, codeDim}, learningRate);

        aSelector = new MLP(codeDim, nodeIndices);
        for (int i = 0; i < 2; ++i)
            aSelector.add(hiddenDim, learningRate, Activation.Type.TANH);
        aSelector.add(nodeIndices, learningRate, Activation.Type.SOFTMAX);
        aSelector.compile();

        bSelector = new MLP(codeDim, nodeIndices);
        for (int i = 0; i < 2; ++i)
            bSelector.add(hiddenDim, learningRate, Activation.Type.TANH);
        bSelector.add(nodeIndices, learningRate, Activation.Type.SOFTMAX);
        bSelector.compile();

    }

    public void train() {
        encoder.train();
        latentGRU.train();
        aSelector.train();
        bSelector.train();
    }

    public void doneTrain() {
        encoder.doneTrain();
        latentGRU.doneTrain();
        aSelector.doneTrain();
        bSelector.doneTrain();
    }

    public GRetUnit generate(int t, SimpleMatrix X, SimpleMatrix A) {

        SimpleMatrix E = encoder.forward(t, A, X);
        SimpleMatrix H = latentGRU.forward(t, E);

        SimpleMatrix AP = aSelector.forward(t, H);
        SimpleMatrix BP = bSelector.forward(t, H);

        return new GRetUnit(t, AP.getDDRM().getData(), BP.getDDRM().getData(), null);

    }

    public SimpleMatrix improveThroughTime(int t, SimpleMatrix dL_dAZ, SimpleMatrix dL_dBZ, SimpleMatrix dL_dH) {

        SimpleMatrix dL_dYH = aSelector.backward_dL_dZ(t, dL_dAZ).plus(bSelector.backward_dL_dZ(t, dL_dBZ));
        GRU.BPRet bPRet = latentGRU.BPTT(t, dL_dYH, dL_dH);
        encoder.BPTT(t, bPRet.dL_dX);

        return bPRet.dL_dHprev;

    }

}
