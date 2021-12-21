package Supervisors.BaseArchitectures.Activations;

import org.ejml.simple.SimpleMatrix;

public interface Activation {

    enum Type {
        SIG,
        TANH,
        SOFTMAX
    }

    SimpleMatrix activate(SimpleMatrix M);
    SimpleMatrix derive(SimpleMatrix aM);

}