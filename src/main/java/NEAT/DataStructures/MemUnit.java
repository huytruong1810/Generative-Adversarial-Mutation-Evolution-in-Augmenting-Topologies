package NEAT.DataStructures;

import static NEAT.NEAT.IMPOSSIBLE_VAL;

public class MemUnit {

    private boolean complete;
    private final int _t /*time step*/;

    private int _a /*INDEXED taken action*/;
    private double _r /*reward*/, _V /*state value*/;
    private double[] _P /*action probability distribution*/, _predXNext /*next state prediction*/;

    private double[] _aX /*input state*/, _ah /*hidden state*/, _aC /*cell state*/, _atanhC /*convenient cache*/;
    private double[] _cX /*input state*/, _ch /*hidden state*/, _cC /*cell state*/, _ctanhC /*convenient cache*/;
    private double[] _sX /*input state*/, _sh /*hidden state*/, _sC /*cell state*/, _stanhC /*convenient cache*/;

    public MemUnit(int t, double[] X, double[] h, double[] C, double[] tanhC, char select) {

        complete = false;
        _t = t;

        // these need to be logged as the time step roll out
        _r = IMPOSSIBLE_VAL; // only needed in training
        _a = IMPOSSIBLE_VAL; // is 5 when agent is dead
        _V = IMPOSSIBLE_VAL;
        _P = null;

        switch (select) {
            case 'a': _aX = X;_ah = h;_aC = C;_atanhC = tanhC;break;
            case 'c': _cX = X;_ch = h;_cC = C;_ctanhC = tanhC;break;
            case 's': _sX = X;_sh = h;_sC = C;_stanhC = tanhC;break;
            default: throw new IllegalStateException("Unknown select.");
        }

    }

    public void complete(MemUnit criticUnit, MemUnit seerUnit) { // should only be invoked on actor unit
        if (_t != criticUnit._t || _t != seerUnit._t)
            throw new IllegalStateException("Three units must be from the same time.");
        _cX = criticUnit._cX;_ch = criticUnit._ch;_cC = criticUnit._cC;_ctanhC = criticUnit._ctanhC;
        _sX = seerUnit._sX;_sh = seerUnit._sh;_sC = seerUnit._sC;_stanhC = seerUnit._stanhC;
        if (_ah != null && _aC != null && _ch != null && _cC != null && _sh != null && _sC != null) complete = true;
        else throw new IllegalStateException("Invalid preferences during completion of this memory unit.");
    }

    public void set_r(double r) { _r = r; }
    public void setOutputs(int a, double V, double[] P, double[] predXNext) { _a = a; _V = V; _P = P; _predXNext = predXNext; }

    public boolean isComplete() { return complete; }
    public int t() { return _t; }
    public int a() { return _a; }
    public double r() { return _r; }
    public double V() { return _V; }
    public double[] P() { return _P; }
    public double[] predXNext() { return _predXNext; }

    // do not need to clone because these won't be modify by any caller
    public double[] h(char select) {
        switch (select) {
            case 'a': return _ah;
            case 'c': return _ch;
            case 's': return _sh;
            default: throw new IllegalStateException("Unknown select.");
        }
    }
    public double[] C(char select) {
        switch (select) {
            case 'a': return _aC;
            case 'c': return _cC;
            case 's': return _sC;
            default: throw new IllegalStateException("Unknown select.");
        }
    }
    public double[] tanhC(char select) {
        switch (select) {
            case 'a': return _atanhC;
            case 'c': return _ctanhC;
            case 's': return _stanhC;
            default: throw new IllegalStateException("Unknown select.");
        }
    }
    public double[] X(char select) {
        switch (select) {
            case 'a': return _aX;
            case 'c': return _cX;
            case 's': return _sX;
            default: throw new IllegalStateException("Unknown select.");
        }
    }

    @Override
    public int hashCode() { return _t; }

}
