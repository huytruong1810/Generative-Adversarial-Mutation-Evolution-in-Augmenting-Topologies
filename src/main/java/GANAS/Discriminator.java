package GANAS;

import GANAS.BaseArchitectures.Activations.Activation;
import GANAS.BaseArchitectures.GCN;
import GANAS.BaseArchitectures.MLP;
import org.ejml.simple.SimpleMatrix;

public class Discriminator {

    private final int codeLength;
    private final static int STATIC_TIME = -1;

    // training data includes N genomes from the top individuals and N genomes generated by G
    // positive pairs: each true genomes with every other true genomes -> (N x (N - 1)) datapoint
    // negative pairs: each true genomes with every G genomes -> (N x N) datapoint
    // total of (N x (2N - 1)) datapoint with shuffling
    private final GCN trueEncoder, otherEncoder;
    private final MLP classifier;

    public Discriminator(int featureDim, int codeDim, int hiddenDim, int nodeIndices, int numLayers, double learningRate) {

        codeLength = codeDim;
        int N = 2 + numLayers;
        int[] GCNWidths = new int[N];
        GCNWidths[0] = featureDim;
        for (int i = 1; i < N - 1; ++i) GCNWidths[i] = hiddenDim;
        GCNWidths[N - 1] = codeDim;

        trueEncoder = new GCN(nodeIndices, GCNWidths, learningRate);
        otherEncoder = new GCN(nodeIndices, GCNWidths, learningRate);

        classifier = new MLP(codeDim * 2, 2);
        for (int i = 0; i < numLayers; ++i)
            classifier.add(hiddenDim, learningRate, Activation.Type.TANH);
        classifier.add(2, learningRate, Activation.Type.SOFTMAX);
        classifier.compile();

    }

    public void train() {
        classifier.train();
        trueEncoder.train();
        otherEncoder.train();
    }

    public void doneTrain() {
        classifier.doneTrain();
        trueEncoder.doneTrain();
        otherEncoder.doneTrain();
    }

    public SimpleMatrix discriminate(SimpleMatrix TX, SimpleMatrix TA, SimpleMatrix OX, SimpleMatrix OA) {
        SimpleMatrix TEncodings = trueEncoder.forward(STATIC_TIME, TA, TX);
        SimpleMatrix OEncodings = otherEncoder.forward(STATIC_TIME, OA, OX);
        double[][] encodings = new double[1][codeLength * 2];
        for (int i = 0; i < codeLength; ++i) { // concatenate two encodings
            encodings[0][i] = TEncodings.get(0, i);
            encodings[0][i + codeLength] = OEncodings.get(0, i);
        }
        return classifier.forward(STATIC_TIME, new SimpleMatrix(encodings));
    }

    public double getCEL(SimpleMatrix S, SimpleMatrix Y) {
        return -Y.elementMult(S.elementLog()).elementSum();
    }

    public void improve(SimpleMatrix S, SimpleMatrix Y) {
        SimpleMatrix dL_dZ = S.minus(Y);
        SimpleMatrix dL_dE = classifier.backward_dL_dZ(STATIC_TIME, dL_dZ);
        trueEncoder.BPTT(STATIC_TIME, dL_dE.cols(0, codeLength));
        otherEncoder.BPTT(STATIC_TIME, dL_dE.cols(codeLength, codeLength * 2));
    }

}
