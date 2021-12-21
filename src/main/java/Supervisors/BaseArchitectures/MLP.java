package Supervisors.BaseArchitectures;

import Supervisors.BaseArchitectures.Activations.Activation;
import org.ejml.simple.SimpleMatrix;

import java.util.ArrayList;

public class MLP {

    private boolean compiled;
    private final int outputSize;

    private final ArrayList<Integer> layerSizes;
    private final ArrayList<Dense> layers;

    public MLP(int inputDim, int outputDim) {
        compiled = false;
        outputSize = outputDim;
        layers = new ArrayList<>();
        layerSizes = new ArrayList<>();
        layerSizes.add(inputDim);
    }

    public void train() {
        for (Dense layer : layers) layer.train();
    }

    public void doneTrain() {
        for (Dense layer : layers) layer.doneTrain();
    }

    public void add(int nNeurons, double learningRate, Activation.Type activation) {
        layers.add(new Dense(layerSizes.get(layerSizes.size() - 1), nNeurons, learningRate, activation));
        layerSizes.add(nNeurons);
    }

    public void compile() {
        if (layerSizes.get(layerSizes.size() - 1) != outputSize)
            throw new IllegalStateException("Final layer does not match output size. Neural stack compilation fails.");
        compiled = true;
    }

    public SimpleMatrix forward(int t, SimpleMatrix matrix) {
        if (!compiled) throw new IllegalStateException("MLP is not compiled.");
        for (Dense layer : layers) matrix = layer.feedFor(t, matrix);
        return matrix;
    }

    public SimpleMatrix backward_dL_dY(int t, SimpleMatrix dL_dY) {
        for (int i = layers.size() - 1; i >= 0; --i)
            dL_dY = layers.get(i).feedBack_dL_dY(t, dL_dY);
        return dL_dY;
    }

    public SimpleMatrix backward_dL_dZ(int t, SimpleMatrix dL_dZ) {
        int i = layers.size() - 1;
        SimpleMatrix dL_dY = layers.get(i--).feedBack_dL_dZ(t, dL_dZ);
        while (i >= 0) dL_dY = layers.get(i--).feedBack_dL_dY(t, dL_dY);
        return dL_dY;
    }

}
