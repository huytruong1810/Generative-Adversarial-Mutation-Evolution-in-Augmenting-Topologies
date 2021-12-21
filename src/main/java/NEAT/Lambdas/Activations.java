package NEAT.Lambdas;

import java.util.function.Function;

public class Activations {

    public enum Type {
        LINEAR,
        RELU,
        SIGMOID,
        TANH,
        SOFTMAX // doesn't do anything because the implementation implicitly address this
    }

    public static final Function<Double, Double> reLU = x -> Math.max(0, x);
    public static final Function<Double, Double> dreLU = y -> (y >= 0.0) ? 1.0 : 0.0;

    public static final Function<Double, Double> sig = x -> 1 / (1 + Math.exp(-x));
    public static final Function<Double, Double> dsig = y -> y * (1 - y);

    public static final Function<Double, Double> tanh = Math::tanh;
    public static final Function<Double, Double> dtanh = y -> 1 - Math.pow(y, 2);

}
