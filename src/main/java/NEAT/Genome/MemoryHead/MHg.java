package NEAT.Genome.MemoryHead;

import NEAT.DataStructures.SecuredList;
import NEAT.DataStructures.MemStream;
import NEAT.Genome.Genome;
import NEAT.DataStructures.MemUnit;

import java.util.HashMap;

public class MHg extends Genome {

    private LSTM phenotype;
    private SecuredList<MHcg> cons;
    private SecuredList<MHng> nodes;

    public MHg(int inN, int outN) {
        super(inN, outN);
        cons = new SecuredList<>();
        nodes = new SecuredList<>();
    }

    public SecuredList<MHng> getNodes() { return nodes; }
    public SecuredList<MHcg> getCons() { return cons; }

    @Override
    public void cast(Genome g) {
        super.cast(g);  // do not cast over phenotype because it will be express anyway
        cons = ((MHg) g).cons;
        nodes = ((MHg) g).nodes;
    }

    @Override
    public void express() { phenotype = new LSTM(this); }

    public void episodePrep() {
        for (MHng node : nodes.getData()) node.episodePrep();
    }

    public void episodeDone() {
        for (MHng node : nodes.getData()) node.episodeDone();
    }

    public void randomParams(double p) {
        for (MHng n : nodes.getData()) n.randomBias(p);
        for (MHcg c : cons.getData()) c.randomWeights(p);
    }

    public MemUnit feed(MemUnit prevUnit, double[] input, char select) { return phenotype.forward(prevUnit, input, select); }

    public void train(int t, MemStream memStream, double[] dL_dCnext, double[] dL_dhnext,
                      HashMap<Integer, Double> actorGradRet,
                      HashMap<Integer, Double> criticGradRet,
                      HashMap<Integer, Double> seerGradRet) {

        double[] dL_dh_actor = new double[outputNum];
        double[] dL_dh_critic = new double[outputNum];
        double[] dL_dh_seer = new double[outputNum];

        // these are innovation numbers at input layer of ACU, so IN = 1..hidN
        for (int in = 1, maxIN = outputNum + 1; in < maxIN; ++in) {
            int index = in - 1;
            dL_dh_actor[index] = actorGradRet.containsKey(in) ? actorGradRet.get(in) : 0;
            dL_dh_critic[index] = criticGradRet.containsKey(in) ? criticGradRet.get(in) : 0;
            dL_dh_seer[index] = seerGradRet.containsKey(in) ? seerGradRet.get(in) : 0;
        }

        phenotype.BPTT(memStream.get(t),
                t != 0 ? memStream.get(t - 1) : memStream.beginUnit(),
                dL_dh_actor, dL_dh_critic, dL_dh_seer, dL_dCnext, dL_dhnext);

    }

    @Override
    public String toString() {
        StringBuilder rep = new StringBuilder("<MRU(");
        rep.append(getID());
        rep.append(")/Nodes:");
        for (MHng n : nodes.getData()) rep.append(n);
        rep.append("/Cons:");
        for (MHcg c : cons.getData()) rep.append(c);
        return rep + "/MRU>";
    }

    @Override
    public MHg clone() throws CloneNotSupportedException {

        super.clone(); // push down global ID
        MHg theClone = new MHg(inputNum, outputNum); // push it up

        SecuredList<MHng> cloneNodes = theClone.nodes;
        SecuredList<MHcg> cloneCons = theClone.cons;

        for (MHng n : nodes.getData()) cloneNodes.add(n.clone());
        for (MHcg c : cons.getData()) {
            // cloned con need redirection to correct added nodes
            MHng f = cloneNodes.get((MHng) c.getFG());
            MHng t = cloneNodes.get((MHng) c.getTG());
            MHcg cloneCon = c.redirectClone(f, t);
            // node cloning do not replicate in cons
            t.addInCons(cloneCon);
            cloneCons.add(cloneCon);
        }
        theClone.ID = ID;

        return theClone;

    }

}
