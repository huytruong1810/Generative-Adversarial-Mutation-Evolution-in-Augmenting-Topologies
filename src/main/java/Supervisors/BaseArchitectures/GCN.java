package Supervisors.BaseArchitectures;

import Supervisors.BaseArchitectures.Activations.Activation;
import org.ejml.simple.SimpleMatrix;

public class GCN {

    private final int nIndices;
    private final MLP hiddenAgg, graphAgg;

    public GCN(int numNodeIndices, int[] layerWidths, double learningRate) {

        nIndices = numNodeIndices;

        int nLayers = layerWidths.length;
        int nInputs = layerWidths[0];
        int nOutputs = layerWidths[nLayers - 1];

        hiddenAgg = new MLP(nInputs, nInputs);
        graphAgg = new MLP(numNodeIndices * nInputs, nOutputs);

        for (int i = 1; i < nLayers; ++i) {
            // hidden aggregation NS has nLayers-many hidden layers but not the specific width
            hiddenAgg.add(nInputs, learningRate, Activation.Type.TANH);
            graphAgg.add(layerWidths[i], learningRate, Activation.Type.TANH);
        }

        hiddenAgg.compile();
        graphAgg.compile();

    }

    public void train() {
        hiddenAgg.train();
        graphAgg.train();
    }

    public void doneTrain() {
        hiddenAgg.doneTrain();
        graphAgg.doneTrain();
    }

    public SimpleMatrix forward(int t, SimpleMatrix A_hat, SimpleMatrix X) { // [nodes_indices x n_features]
        // A_hat = A + I (self-loop for self-feature consideration)

        int N = A_hat.numCols();
        // degree matrix of A_hat, called D_hat
        double[] degrees = new double[N];
        // inverse of a diag matrix is just a diag matrix with reciprocal values
        // note that in our adj matrix, there exists noses that doesn't exist, and 1/0 = inf
        for (int i = 0; i < N; ++i) {
            double val = A_hat.extractVector(true, i).elementSum();
            degrees[i] = (val == 0) ? 0 : 1 / val;
        }
        // normalized A_hat = D_hat**-1 dot A_hat
        A_hat = SimpleMatrix.diag(degrees).mult(A_hat); // [nodes_indices x nodes_indices]

        // scale down X because xy-coordinates are huge values
        X = X.scale(0.01);

        // do neighbor message passing-aggregating only once
        SimpleMatrix H = hiddenAgg.forward(t, A_hat.mult(X)); // [nodes_indices x n_features]

        H.reshape(1, X.getNumElements()); // [1 x (node_indices * n_features)]

        return graphAgg.forward(t, H); // [1 x n_outputs]

    }

    public void BPTT(int t, SimpleMatrix dL_dG) { // [1 x n_outputs]
        // in this application, GCN is always the input structure so no
        // need to return back gradient

        SimpleMatrix dL_dH = graphAgg.backward_dL_dY(-1, dL_dG); // [1 x (node_indices * n_features)]
        dL_dH.reshape(nIndices, dL_dH.numCols() / nIndices); // [nodes_indices x n_features]
        hiddenAgg.backward_dL_dY(t, dL_dH); // [nodes_indices x n_features]

    }

}
