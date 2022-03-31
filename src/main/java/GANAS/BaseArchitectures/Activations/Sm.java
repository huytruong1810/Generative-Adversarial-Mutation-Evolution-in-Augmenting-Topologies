package GANAS.BaseArchitectures.Activations;

import org.ejml.simple.SimpleMatrix;

public class Sm implements Activation {

    @Override
    public SimpleMatrix activate(SimpleMatrix M) {
        SimpleMatrix Z = M.minus(max(M));
        SimpleMatrix EZ = Z.elementExp();
        return EZ.divide(EZ.elementSum()); /* sm(xi) = e^(xi-max(x)) / sum_j[e^(xj-max(x))] */
    }

    private double max(SimpleMatrix M) {
        double result = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < M.numCols(); ++i)
            result = Math.max(result, M.get(0, i));
        return result;
    }

    @Override
    public SimpleMatrix derive(SimpleMatrix aM) {
        throw new IllegalStateException("Derivation of Softmax should be computed by driver.");
    }

}
