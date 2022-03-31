package GANAS.BaseArchitectures.Activations;

import org.ejml.simple.SimpleMatrix;

public class Sig implements Activation {

    @Override
    public SimpleMatrix activate(SimpleMatrix M) {
        SimpleMatrix O = M.copy();
        O.fill(1.0);
        return O.elementDiv(O.plus(M.negative().elementExp())); /* sig(x) = 1 / (1 + e^-x) */
    }

    @Override
    public SimpleMatrix derive(SimpleMatrix sigM) {
        SimpleMatrix O = sigM.copy();
        O.fill(1.0);
        return sigM.elementMult(O.minus(sigM)); /* dsig(x)/dx = sig(x) * (1 - sig(x)) */
    }

}
