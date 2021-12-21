package Supervisors.BaseArchitectures.Activations;

import org.ejml.simple.SimpleMatrix;

public class Tanh implements Activation {

    @Override
    public SimpleMatrix activate(SimpleMatrix M) {
        SimpleMatrix E = M.scale(2).elementExp();
        SimpleMatrix O = M.copy();
        O.fill(1.0);
        return E.minus(O).elementDiv(E.plus(O)); /* tanh(x) = (e^2x - 1) / (e^2x + 1) */
    }

    @Override
    public SimpleMatrix derive(SimpleMatrix tanhM) {
        SimpleMatrix O = tanhM.copy();
        O.fill(1.0);
        return O.minus(tanhM.elementPower(2.0)); /* dtanh(x)/dx = 1 - tanh(x)^2 */
    }

}
