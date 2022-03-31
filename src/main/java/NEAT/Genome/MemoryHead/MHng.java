package NEAT.Genome.MemoryHead;

import NEAT.DataStructures.GateTuple;
import NEAT.Genome.ConGene;
import NEAT.Genome.NodeGene;

import java.util.HashMap;

import static NEAT.Lambdas.Activations.*;
import static NEAT.NEAT.*;

public class MHng extends NodeGene {

    // keep track of time passage for BPTT
    private HashMap<Integer, Double> aforgetOutputs, ainputOutputs, acandidateOutputs, aoutputOutputs;
    private HashMap<Integer, Double> cforgetOutputs, cinputOutputs, ccandidateOutputs, coutputOutputs;
    private HashMap<Integer, Double> sforgetOutputs, sinputOutputs, scandidateOutputs, soutputOutputs;

    private GateTuple actorBiasTuple;
    private GateTuple criticBiasTuple;
    private GateTuple seerBiasTuple;

    public MHng(int IN) {
        super(IN);
        randomBias(0); // new random parameters
    }

    public MHng(int IN, char proxy) {
        super(IN, proxy);
    }

    public void randomBias(double p) {
        actorBiasTuple = new GateTuple(actorBiasTuple, p);
        criticBiasTuple = new GateTuple(criticBiasTuple, p);
        seerBiasTuple = new GateTuple(seerBiasTuple, p);
    }

    public MHng(NodeGene other) {
        super(other);
    }

    public MHng(NodeGene other, double x, double y) {
        super(other, x, y);
    }

    @Override
    public void episodePrep() {

        if (inCons == null) throw new IllegalStateException("Cannot use proxy.");

        aforgetOutputs = new HashMap<>();
        ainputOutputs = new HashMap<>();
        acandidateOutputs = new HashMap<>();
        aoutputOutputs = new HashMap<>();

        cforgetOutputs = new HashMap<>();
        cinputOutputs = new HashMap<>();
        ccandidateOutputs = new HashMap<>();
        coutputOutputs = new HashMap<>();

        sforgetOutputs = new HashMap<>();
        sinputOutputs = new HashMap<>();
        scandidateOutputs = new HashMap<>();
        soutputOutputs = new HashMap<>();

        originalWeights = new HashMap<>();
        for (ConGene c : inCons)
            originalWeights.put(c, new double[] {
                    ((MHcg) c).getWeight('f', 'a'), ((MHcg) c).getWeight('f', 'c'), ((MHcg) c).getWeight('f', 's'),
                    ((MHcg) c).getWeight('i', 'a'), ((MHcg) c).getWeight('i', 'c'), ((MHcg) c).getWeight('i', 's'),
                    ((MHcg) c).getWeight('c', 'a'), ((MHcg) c).getWeight('c', 'c'), ((MHcg) c).getWeight('c', 's'),
                    ((MHcg) c).getWeight('o', 'a'), ((MHcg) c).getWeight('o', 'c'), ((MHcg) c).getWeight('o', 's')
            });

    }

    @Override
    public void episodeDone() {

        aforgetOutputs.clear();
        ainputOutputs.clear();
        acandidateOutputs.clear();
        aoutputOutputs.clear();

        cforgetOutputs.clear();
        cinputOutputs.clear();
        ccandidateOutputs.clear();
        coutputOutputs.clear();

        sforgetOutputs.clear();
        sinputOutputs.clear();
        scandidateOutputs.clear();
        soutputOutputs.clear();

        originalWeights.clear();

    }

    private HashMap<Integer, Double> getForgetHashMap(char select) {
        switch (select) {
            case 'a': return aforgetOutputs;
            case 'c': return cforgetOutputs;
            case 's': return sforgetOutputs;
            default: throw new IllegalStateException("Unknown select.");
        }
    }
    private HashMap<Integer, Double> getInputHashMap(char select) {
        switch (select) {
            case 'a': return ainputOutputs;
            case 'c': return cinputOutputs;
            case 's': return sinputOutputs;
            default: throw new IllegalStateException("Unknown select.");
        }
    }
    private HashMap<Integer, Double> getCandidateHashMap(char select) {
        switch (select) {
            case 'a': return acandidateOutputs;
            case 'c': return ccandidateOutputs;
            case 's': return scandidateOutputs;
            default: throw new IllegalStateException("Unknown select.");
        }
    }
    private HashMap<Integer, Double> getOutputHashMap(char select) {
        switch (select) {
            case 'a': return aoutputOutputs;
            case 'c': return coutputOutputs;
            case 's': return soutputOutputs;
            default: throw new IllegalStateException("Unknown select.");
        }
    }

    private GateTuple getBiasTuple(char select) {
        switch (select) {
            case 'a': return actorBiasTuple;
            case 'c': return criticBiasTuple;
            case 's': return seerBiasTuple;
            default: throw new IllegalStateException("Unknown select.");
        }
    }

    public void feedAll(int t, char select) {

        if (inCons == null) throw new IllegalStateException("Cannot use proxy.");

        double fWeightedSum, iWeightedSum, cWeightedSum, oWeightedSum;
        fWeightedSum = iWeightedSum = cWeightedSum = oWeightedSum = 0;

        for (ConGene c : inCons) {
            if (c.isEnabled()) {
                MHcg con = (MHcg) c;
                MHng from = (MHng) con.getFG();
                fWeightedSum += (from.getForgetHashMap(select).get(t) * con.getWeight('f', select));
                iWeightedSum += (from.getInputHashMap(select).get(t) * con.getWeight('i', select));
                cWeightedSum += (from.getCandidateHashMap(select).get(t) * con.getWeight('c', select));
                oWeightedSum += (from.getOutputHashMap(select).get(t) * con.getWeight('o', select));
            }
        }

        getForgetHashMap(select).put(t, sig.apply(fWeightedSum + getBiasTuple(select).getVal('f')));
        getInputHashMap(select).put(t, sig.apply(iWeightedSum + getBiasTuple(select).getVal('i')));
        getCandidateHashMap(select).put(t, tanh.apply(cWeightedSum + getBiasTuple(select).getVal('c')));
        getOutputHashMap(select).put(t, sig.apply(oWeightedSum + getBiasTuple(select).getVal('o')));

    }

    public void feedAllAndPutInto(HashMap<Integer, double[]> gateOutputs, int t, char select) {
        feedAll(t, select);
        gateOutputs.put(IN, new double[] {
                getForgetHashMap(select).get(t), getInputHashMap(select).get(t),
                getCandidateHashMap(select).get(t), getOutputHashMap(select).get(t)
        });
    }

    public void backProp(double adL_df, double adL_di, double adL_dc, double adL_do,
                         double cdL_df, double cdL_di, double cdL_dc, double cdL_do,
                         double sdL_df, double sdL_di, double sdL_dc, double sdL_do,
                         int t, HashMap<Integer, double[]> backGrads) {

        if (inCons == null) throw new IllegalStateException("Cannot use proxy.");

        if (x == inputNodeX) {
            // accumulate back gradient for dL_dhprev_x
            double adL_da = adL_df + adL_di + adL_dc + adL_do;
            double cdL_da = cdL_df + cdL_di + cdL_dc + cdL_do;
            double sdL_da = sdL_df + sdL_di + sdL_dc + sdL_do;
            double[] gradTuple = backGrads.containsKey(IN) ? backGrads.get(IN) : new double[] {0.0, 0.0, 0.0};
            gradTuple[0] += adL_da;
            gradTuple[1] += cdL_da;
            gradTuple[2] += sdL_da;
            backGrads.put(IN, gradTuple);
            return;
        }

        double adf_dzf = dsig.apply(aforgetOutputs.get(t));
        double adi_dzi = dsig.apply(ainputOutputs.get(t));
        double adc_dzc = dtanh.apply(acandidateOutputs.get(t));
        double ado_dzo = dsig.apply(aoutputOutputs.get(t));

        double cdf_dzf = dsig.apply(cforgetOutputs.get(t));
        double cdi_dzi = dsig.apply(cinputOutputs.get(t));
        double cdc_dzc = dtanh.apply(ccandidateOutputs.get(t));
        double cdo_dzo = dsig.apply(coutputOutputs.get(t));

        double sdf_dzf = dsig.apply(sforgetOutputs.get(t));
        double sdi_dzi = dsig.apply(sinputOutputs.get(t));
        double sdc_dzc = dtanh.apply(scandidateOutputs.get(t));
        double sdo_dzo = dsig.apply(soutputOutputs.get(t));

        double adL_dzf = adL_df * adf_dzf;
        double adL_dzi = adL_di * adi_dzi;
        double adL_dzc = adL_dc * adc_dzc;
        double adL_dzo = adL_do * ado_dzo;

        double cdL_dzf = cdL_df * cdf_dzf;
        double cdL_dzi = cdL_di * cdi_dzi;
        double cdL_dzc = cdL_dc * cdc_dzc;
        double cdL_dzo = cdL_do * cdo_dzo;

        double sdL_dzf = sdL_df * sdf_dzf;
        double sdL_dzi = sdL_di * sdi_dzi;
        double sdL_dzc = sdL_dc * sdc_dzc;
        double sdL_dzo = sdL_do * sdo_dzo;

        // dL_db = dL_dz * 1
        actorBiasTuple.optimizeValues(adL_dzf, adL_dzi, adL_dzc, adL_dzo);
        criticBiasTuple.optimizeValues(cdL_dzf, cdL_dzi, cdL_dzc, cdL_dzo);
        seerBiasTuple.optimizeValues(sdL_dzf, sdL_dzi, sdL_dzc, sdL_dzo);

        for (ConGene c : inCons) {

            if (!c.isEnabled()) continue;
            MHcg con = (MHcg) c;
            MHng from = (MHng) c.getFG();

            con.optimizeACWeights(
                    adL_dzf * from.aforgetOutputs.get(t), adL_dzi * from.ainputOutputs.get(t),
                    adL_dzc * from.acandidateOutputs.get(t), adL_dzo * from.aoutputOutputs.get(t),
                    cdL_dzf * from.cforgetOutputs.get(t), cdL_dzi * from.cinputOutputs.get(t),
                    cdL_dzc * from.ccandidateOutputs.get(t), cdL_dzo * from.coutputOutputs.get(t),
                    sdL_dzf * from.sforgetOutputs.get(t), sdL_dzi * from.sinputOutputs.get(t),
                    sdL_dzc * from.scandidateOutputs.get(t), sdL_dzo * from.soutputOutputs.get(t));

            double[] W = originalWeights.get(c);
            from.backProp(
                    adL_dzf * W[0], adL_dzi * W[3], adL_dzc * W[6], adL_dzo * W[9],
                    cdL_dzf * W[1], cdL_dzi * W[4], cdL_dzc * W[7], cdL_dzo * W[10],
                    sdL_dzf * W[2], sdL_dzi * W[5], sdL_dzc * W[8], sdL_dzo * W[11],
                    t, backGrads
            );

        }

    }

    public void overrideGatesOutputs(int t, double val, char select) {
        switch (select) {
            case 'a':
                aforgetOutputs.put(t, val);
                ainputOutputs.put(t, val);
                acandidateOutputs.put(t, val);
                aoutputOutputs.put(t, val);
                break;
            case 'c':
                cforgetOutputs.put(t, val);
                cinputOutputs.put(t, val);
                ccandidateOutputs.put(t, val);
                coutputOutputs.put(t, val);
                break;
            case 's':
                sforgetOutputs.put(t, val);
                sinputOutputs.put(t, val);
                scandidateOutputs.put(t, val);
                soutputOutputs.put(t, val);
                break;
            default: throw new IllegalStateException("Unknown select.");
        }
    }

    public boolean equals(Object o) {
        if (!(o instanceof MHng)) return false;
        return super.equals(o);
    }

    @Override
    public String inspect() {
        return "\nGene Type: MRU-NodeGene" + super.inspect() +
                "\n*Parameters" +
                "\n**Biases" +
                "\nActor:" + actorBiasTuple.inspect() +
                "\nCritic:" + criticBiasTuple.inspect() +
                "\nSeer:" + seerBiasTuple.inspect();
    }

    @Override
    public MHng clone() throws CloneNotSupportedException {

        MHng theClone = new MHng(IN);

        theClone.x = x;
        theClone.y = y;

        theClone.actorBiasTuple = actorBiasTuple.clone();
        theClone.criticBiasTuple = criticBiasTuple.clone();
        theClone.seerBiasTuple = seerBiasTuple.clone();

        return theClone; // don't copy in-connections

    }

}
