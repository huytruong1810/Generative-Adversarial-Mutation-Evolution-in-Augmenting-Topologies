package NEAT.DataStructures;

public class MemUnit {

    private final int _t /*time step*/;
    private int _r /*reward*/, _a /*INDEXED taken action*/;
    private double _aG /*actor gradient*/, _cG /*critic gradient*/;
    private final double[] _X /*input state*/, _h /*hidden state*/, _C /*cell state*/, _tanhC /*convenient cache*/;

    public MemUnit(int t, double[] X, double[] h, double[] C, double[] tanhC) {
        _t = t;
        _X = X;
        _h = h;
        _C = C;
        _tanhC = tanhC;
    }

    public void set_r(int r) { _r = r; }
    public void set_a(int a) { _a = a; }
    public void set_aG(double aG) { _aG = aG; }
    public void set_cG(double cG) { _cG = cG; }

    public int t() { return _t; }
    public int r() { return _r; }
    public int a() { return _a; }
    public double aG() { return _aG; }
    public double cG() { return _cG; }
    public double[] h() { return _h; }
    public double[] C() { return _C; }
    public double[] tanhC() { return _tanhC; }
    public double[] X() { return _X; }

    @Override
    public int hashCode() { return _t; }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("Time " + _t + ": X[ ");
        if (_X != null) for (double d : _X) str.append(d).append(" ");
        else str.append("null");
        str.append(" ] r[ ").append(_r).append(" ] a[ ").append(_a).append(" ] aG[ ").append(_aG).append(" ] cG[ ").append(_cG);
        str.append(" ] h[");
        for (double d : _h) str.append(d).append(" ");
        str.append("] C[ ");
        for (double d : _C) str.append(d).append(" ");
        return str.append("]").toString();
    }

}
