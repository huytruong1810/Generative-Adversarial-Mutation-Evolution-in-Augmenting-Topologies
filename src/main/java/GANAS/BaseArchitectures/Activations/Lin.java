package GANAS.BaseArchitectures.Activations;

import org.ejml.simple.SimpleMatrix;

public class Lin implements Activation {

    @Override
    public SimpleMatrix activate(SimpleMatrix M) {
        return M; /* lin(x) = x */
    }

    @Override
    public SimpleMatrix derive(SimpleMatrix linM) {
        SimpleMatrix O = linM.copy();
        O.fill(1.0);
        return O; /* dlin(x)/dx = 1 */
    }

}
