package Supervisors.BaseArchitectures;

import Supervisors.BaseArchitectures.Activations.*;
import org.ejml.simple.SimpleMatrix;

import java.util.HashMap;
import java.util.Random;

public class Dense {

    private final int size;
    private final double LR;
    private final Activation A;

    private SimpleMatrix _W; // original weights because W will be changed each time step
    private SimpleMatrix W, B;
    private HashMap<Integer, SimpleMatrix> X, Y;

    public Dense(int nFeatures, int nNeurons, double learningRate, Activation.Type activation) {

        size = nNeurons;
        Random rand = new Random();

        LR = learningRate;
        switch (activation) {
            case TANH:
                A = new Tanh(); break;
            case SIG:
                A = new Sig(); break;
            default:
                A = new Lin(); break;
        }

        W = SimpleMatrix.random_DDRM(nFeatures, nNeurons, -1, 1, rand); // [n_features x n_neurons]
        B = new SimpleMatrix(1, nNeurons); // [1 x n_neurons]

    }

    public void train() {
        X = new HashMap<>();
        Y = new HashMap<>();
        _W = W.copy();
    }

    public void doneTrain() {
        X.clear();
        Y.clear();
        _W = null;
    }

    public SimpleMatrix feedFor(int t, SimpleMatrix inputMatrix) {

        int sampleSize = inputMatrix.numRows();
        X.put(t, inputMatrix.copy()); // [sample_size x n_features]

        SimpleMatrix BStack = new SimpleMatrix(0, size);
        for (int row = 0; row < sampleSize; ++row) BStack = BStack.concatRows(B); // [sample_size x n_neurons]

        // [sample_size x n_features] dot [n_features x n_neurons] + [sample_size x n_neurons] = [sample_size x n_neurons]
        SimpleMatrix V = X.get(t).mult(W).plus(BStack);

        Y.put(t, A.activate(V)); // [n_samples x n_neurons]

        return Y.get(t).copy();

    }

    public SimpleMatrix feedBack_dL_dY(int t, SimpleMatrix dL_dY) { // [sample_size x n_neurons]
        SimpleMatrix dL_dZ = dL_dY.elementMult(A.derive(Y.get(t))); // [sample_size x n_neurons] x [sample_size x n_neurons]
        return feedBack_dL_dZ(t, dL_dZ);
    }

    public SimpleMatrix feedBack_dL_dZ(int t, SimpleMatrix dL_dZ) {

        SimpleMatrix dL_dB = new SimpleMatrix(1, size); // [1 x n_neurons]
        for (int col = 0; col < size; ++col) dL_dB.set(0, col, dL_dZ.cols(col, col + 1).elementSum());
        // [n_features x sample_size] dot [sample_size x n_neurons] = [n_features x n_neurons]
        SimpleMatrix dL_dW = X.get(t).transpose().mult(dL_dZ);
        // [sample_size x n_neurons] dot [n_neurons x n_features] = [sample_size x n_features]
        SimpleMatrix dL_dX = dL_dZ.mult(_W.transpose());

        W = W.minus(dL_dW.scale(LR));
        B = B.minus(dL_dB.scale(LR));

        return dL_dX;

    }

}
