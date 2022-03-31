package GANAS.BaseArchitectures;

import GANAS.BaseArchitectures.Activations.Activation;
import org.ejml.simple.SimpleMatrix;

import java.util.HashMap;

public class GRU {

    public static class BPRet {
        public SimpleMatrix dL_dHprev, dL_dX;
        public BPRet(SimpleMatrix dL_dHprevRef, SimpleMatrix dL_dXRef) {
            dL_dHprev = dL_dHprevRef; dL_dX = dL_dXRef;
        }
    }

    private final int nInputs, nOutputs, MLPInputDim; // nInputs == num features

    private final MLP resetGate, updateGate, candidateHidden;
    private HashMap<Integer, SimpleMatrix> HSeq, RSeq, ZSeq, cHSeq; // are reset everytime train is called

    public GRU(int[] layerWidths, double learningRate) {

        int nLayers = layerWidths.length;
        nInputs = layerWidths[0];
        nOutputs = layerWidths[nLayers - 1];
        MLPInputDim = nInputs + nOutputs;

        resetGate = new MLP(MLPInputDim, nOutputs);
        updateGate = new MLP(MLPInputDim, nOutputs);
        candidateHidden = new MLP(MLPInputDim, nOutputs);

        for (int i = 1; i < nLayers; ++i) {
            int nHidden = layerWidths[i];
            resetGate.add(nHidden, learningRate, Activation.Type.SIG);
            updateGate.add(nHidden, learningRate, Activation.Type.SIG);
            candidateHidden.add(nHidden, learningRate, Activation.Type.TANH);
        }

        resetGate.compile();
        updateGate.compile();
        candidateHidden.compile();

    }

    public void train() {
        HSeq = new HashMap<>() {{ put(-1, new SimpleMatrix(1, nOutputs)); }};
        RSeq = new HashMap<>();
        ZSeq = new HashMap<>();
        cHSeq = new HashMap<>();
        resetGate.train();
        updateGate.train();
        candidateHidden.train();
    }

    public void doneTrain() {
        HSeq.clear();
        RSeq.clear();
        ZSeq.clear();
        cHSeq.clear();
        resetGate.doneTrain();
        updateGate.doneTrain();
        candidateHidden.doneTrain();
    }

    public SimpleMatrix forward(int t, SimpleMatrix Xt) { // [1 x n_inputs]

        if (Xt.numCols() != nInputs) throw new IllegalStateException("Input dimensions do not match.");

        // XHt = [Xt Ht-1]
        // Rt = NSr(XHt), Zt = NSz(XHt)
        // RHt = Rt x Ht-1
        // XRHt = [Xt RHt]
        // cHt = NSch(XRHt)
        // Ht = Zt x Ht-1 + (1 - Zt) x cHt
        SimpleMatrix prevHt = HSeq.get(t - 1); // [1 x n_outputs]
        SimpleMatrix XHt = Xt.concatColumns(prevHt); // [1 x (n_inputs + n_outputs)]

        SimpleMatrix Rt = resetGate.forward(t, XHt); // [1 x n_outputs]
        SimpleMatrix Zt = updateGate.forward(t, XHt); // [1 x n_outputs]
        SimpleMatrix cHt = candidateHidden.forward(t, Xt.concatColumns(prevHt.elementMult(Rt))); // [1 x n_outputs]

        SimpleMatrix Ht = Zt.elementMult(prevHt).plus(Zt.negative().plus(1).elementMult(cHt));

        HSeq.put(t, Ht);
        RSeq.put(t, Rt);
        ZSeq.put(t, Zt);
        cHSeq.put(t, cHt);

        return Ht; // [1 x n_outputs]

    }

    public BPRet BPTT(int t, SimpleMatrix dL_dY, SimpleMatrix dL_dHnext) {

        SimpleMatrix dL_dH = dL_dHnext.plus(dL_dY);

        // dL_dXRHt = dNSch(dL_dcHt) and dL_dcHt = dL_dHt x dHt_dcHt and dHt_dcHt = 1 - Zt
        SimpleMatrix dL_dXRHt = candidateHidden.backward_dL_dY(t, dL_dH.elementMult(ZSeq.get(t).negative().plus(1)));
        SimpleMatrix dL_dRHt = dL_dXRHt.cols(nInputs, MLPInputDim);
        // dRHt_dRt = Ht-1
        // dHt_dZt = Ht-1 - cHt
        // dL_dZt = dL_dHt x dHt_dZt and dL_dRt = dL_dRHt x Ht-1
        SimpleMatrix dL_dXHt = updateGate.backward_dL_dY(t, dL_dH.elementMult(HSeq.get(t - 1).minus(cHSeq.get(t))))
                .plus(resetGate.backward_dL_dY(t, dL_dRHt.elementMult(HSeq.get(t - 1))));
        // dL_dprevHt = dL_dRHt x dRHt_dprevHt + dL_dHt x dHt_dprevHt and dRHt_dprevHt = Rt and dHt_dprevHt = Zt
        // compute gradient for Ht-1 and pass it to "previous" time
        SimpleMatrix dL_dHprev = dL_dXHt.cols(nInputs, MLPInputDim).plus(dL_dRHt.elementMult(RSeq.get(t)).plus(dL_dH.elementMult(ZSeq.get(t))));
        SimpleMatrix dL_dX = dL_dXHt.cols(0, nInputs).plus(dL_dXRHt.cols(0, nInputs));

        return new BPRet(dL_dHprev, dL_dX);

    }

}
