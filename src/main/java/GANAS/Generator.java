package GANAS;

import GANAS.BaseArchitectures.*;
import GANAS.BaseArchitectures.Activations.Activation;
import org.ejml.simple.SimpleMatrix;

public class Generator {

    private final GCN encoder;
    // if there is no connection from a to b, mutate a connection in between
    // else if there already is one, mutate a node on that connection
    private final GRU latentGRU;
    private final MLP aSelector, bSelector;

    public Generator(int featureDim, int codeDim, int hiddenDim, int nodeIndices, int numLayers, double learningRate) {

        encoder = new GCN(nodeIndices, new int[] {featureDim, hiddenDim, codeDim}, learningRate);
        latentGRU = new GRU(new int[] {codeDim, hiddenDim, codeDim}, learningRate);

        aSelector = new MLP(codeDim, nodeIndices);
        for (int i = 0; i < numLayers; ++i)
            aSelector.add(hiddenDim, learningRate, Activation.Type.TANH);
        aSelector.add(nodeIndices, learningRate, Activation.Type.SOFTMAX);
        aSelector.compile();

        bSelector = new MLP(codeDim, nodeIndices);
        for (int i = 0; i < numLayers; ++i)
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

    public TimeStep generate(int t, SimpleMatrix X, SimpleMatrix A) {

        SimpleMatrix E = encoder.forward(t, A, X);
        SimpleMatrix H = latentGRU.forward(t, E);

        SimpleMatrix AP = aSelector.forward(t, H);
        SimpleMatrix BP = bSelector.forward(t, H);

        return new TimeStep(t, AP.getDDRM().getData(), BP.getDDRM().getData(), null);

    }

    public SimpleMatrix improveThroughTime(int t, SimpleMatrix dL_dAZ, SimpleMatrix dL_dBZ, SimpleMatrix dL_dH) {

        SimpleMatrix dL_dYH = aSelector.backward_dL_dZ(t, dL_dAZ).plus(bSelector.backward_dL_dZ(t, dL_dBZ));
        GRU.BPRet bpRet = latentGRU.BPTT(t, dL_dYH, dL_dH);
        encoder.BPTT(t, bpRet.dL_dX);

        return bpRet.dL_dHprev;

    }

}
