package NEAT.Genome.DecisionHead;

import NEAT.DataStructures.SecuredList;
import NEAT.DataStructures.MemStream;
import NEAT.Genome.Genome;
import NEAT.DataStructures.MemUnit;

import java.util.HashMap;

import static NEAT.NEAT.obsII;

public class DHg extends Genome {

    private MLP phenotype;
    private SecuredList<DHcg> cons;
    private SecuredList<DHng> nodes;

    public DHg(int inN, int outN) {
        super(inN, outN);
        cons = new SecuredList<>();
        nodes = new SecuredList<>();
    }

    public SecuredList<DHng> getNodes() { return nodes; }
    public SecuredList<DHcg> getCons() { return cons; }

    @Override
    public void cast(Genome g) {
        super.cast(g); // do not cast over phenotype because it will be express anyway
        cons = ((DHg) g).cons;
        nodes = ((DHg) g).nodes;
    }

    @Override
    public void express() { phenotype = new MLP(this); }

    public void episodePrep() {
        for (DHng node : nodes.getData()) node.episodePrep();
    }

    public void episodeDone() {
        for (DHng node : nodes.getData()) node.episodeDone();
    }

    public void randomParams(double p) {
        for (DHng n : nodes.getData()) n.randomBias(p);
        for (DHcg c : cons.getData()) c.randomWeights(p);
    }

    public double[] feed(int t, double[] inputs, char select) {
        return phenotype.forward(t, inputs, select);
    }

    public void train(int t, double lambda, double gamma, MemStream memStream,
                      HashMap<Integer, Double> actorGradRet,
                      HashMap<Integer, Double> criticGradRet,
                      HashMap<Integer, Double> seerGradRet) {

        MemUnit curUnit = memStream.get(t);
        int takenActionIndex = curUnit.a();

        // 0 1 2 ... 17 18 19, done time at 19, lambda = 0.92, gamma = g
        // if t = 19, then k = (19 - 19) * 0.92 + 1 = 1, td target = r(19)
        // if t = 18, then k = (19 - 18) * 0.92 + 1 = 2, td target = r(18) + gr(19)
        // if t = 2, then k = (19 - 2) * 0.92 + 1 = 17, td target = r2 gr3 g^2*r4 ... g^15*r17 g^16*r18 + g^17*V19
        // if t = 1, then k = (19 - 1) * 0.92 + 1 = 18, td target = r1 gr2 g^2*r3 ... g^16*r17 g^17*r18 + g^18*V19
        // if t = 0, then k = (19 - 0) * 0.92 + 1 = 18, td target = r0 gr1 g^2*r2 ... g^16*r16 g^17*r17 + g^18*V18

        int doneTime = memStream.maxTime();
        int k = (int) Math.round((doneTime - t) * lambda + 1); // steps to-look-ahead
        double tdTarget = 0;
        for (int i = 0; i < k; ++i)
            tdTarget += (Math.pow(gamma, i) * memStream.get(t + i).r());
        if ((t + k) <= doneTime)
            tdTarget += (Math.pow(gamma, k) * memStream.get(t + k).V());

        // critic gradient is negative of gradient of MSE(target, critic's value) wrt critic's value
        // which also happens to be the advantage value A(s, a)
        double criticGrad = tdTarget - curUnit.V();

        // seer gradient is negative of gradient of MSE(true next state, seer's prediction) wrt seer's prediction
        int obsLength = obsII.length;
        double[] nextState = (t != doneTime ? memStream.get(t + 1) : memStream.endUnit()).X('s');
        double[] nextStatePred = curUnit.predXNext();
        double[] seerGrad = new double[obsLength];
        for (int i = 0; i < obsLength; ++i) seerGrad[i] = nextState[i] - nextStatePred[i];

        phenotype.actorBP(t, criticGrad, curUnit.P(), takenActionIndex, actorGradRet);
        phenotype.criticBP(t, criticGrad, criticGradRet);
        phenotype.seerBP(t, seerGrad, seerGradRet);

    }

    @Override
    public String toString() {
        StringBuilder rep = new StringBuilder("<ACU(");
        rep.append(getID());
        rep.append(")/Nodes:");
        for (DHng n : nodes.getData()) rep.append(n);
        rep.append("/Cons:");
        for (DHcg c : cons.getData()) rep.append(c);
        return rep + "/ACU>";
    }

    @Override
    public DHg clone() throws CloneNotSupportedException {

        super.clone(); // push down global ID
        DHg theClone = new DHg(inputNum, outputNum); // push it up

        SecuredList<DHng> cloneNodes = theClone.nodes;
        SecuredList<DHcg> cloneCons = theClone.cons;

        for (DHng n : nodes.getData()) cloneNodes.add(n.clone());
        for (DHcg c : cons.getData()) {
            // cloned con need redirection to correct added nodes
            DHng f = cloneNodes.get((DHng) c.getFG());
            DHng t = cloneNodes.get((DHng) c.getTG());
            DHcg cloneCon = c.redirectClone(f, t);
            // node cloning do not replicate in cons
            t.addInCons(cloneCon);
            cloneCons.add(cloneCon);
        }
        theClone.ID = ID;

        return theClone;

    }

}
