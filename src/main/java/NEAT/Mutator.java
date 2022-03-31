package NEAT;

import NEAT.DataStructures.SecuredList;
import NEAT.Genome.DecisionHead.DHcg;
import NEAT.Genome.DecisionHead.DHg;
import NEAT.Genome.DecisionHead.DHng;
import NEAT.Genome.MemoryHead.MHcg;
import NEAT.Genome.MemoryHead.MHg;
import NEAT.Genome.MemoryHead.MHng;
import NEAT.Genome.NodeGene;

import static NEAT.Lambdas.Graphics.midY;

public class Mutator {

    private static final double // all mutations happen independently between MRU and ACU
            // happens to offsprings every evolution step
            P_AC = 0.9, // Prob(add connection)
            P_AN = 0.5 /*0.01*/, // Prob(add node)
            ACS = 1, // add connection strength
            ANS = 0.035; // add node strength

    private final GenePool genePool;

    public Mutator(GenePool anc) {
        // should only be called by NEAT
        genePool = anc;
    }

    public void mutateStructure(Individual individual) {

        MHg mru = individual.getMH();
        DHg acu = individual.getDH();

        // child inherit parent's parameters at a rate
        mru.randomParams(0.6);
        acu.randomParams(0.6);

        // safe these so we do not over-add nodes or connections
        int numMRUCons = mru.getCons().size();
        int numACUCons = acu.getCons().size();
        int numMRUNodes = mru.getNodes().size();
        int numACUNodes = acu.getNodes().size();

        // add connection mutation (can be ancestral unique or not)
        for (int i = 0, m = (int) (ACS * (numMRUNodes * 2 - numMRUCons)); i < m; ++i) {
            if (P_AC > Math.random())
                addMHCon(mru.getNodes(), mru.getCons());
        }
        for (int i = 0, m = (int) (ACS * (numACUNodes * 2 - numACUCons)); i < m; ++i) {
            if (P_AC > Math.random())
                addDHCon(acu.getNodes(), acu.getCons());
        }

        // add new nodes (2 new connections per node added)
        for (int i = 0, m = (int) (ANS * numMRUCons); i < m; ++i) {
            if (P_AN > Math.random())
                addMHNode(mru.getNodes(), mru.getCons());
        }
        for (int i = 0, m = (int) (ANS * numACUCons); i < m; ++i) {
            if (P_AN > Math.random())
                addDHNode(acu.getNodes(), acu.getCons());
        }

    }

    private void addMHCon(SecuredList<MHng> nodes, SecuredList<MHcg> cons) {
        NodeGene a = nodes.getRandom(), b = nodes.getRandom(); // random-select the endpoint-nodes
        MHcg c = (MHcg) genePool.logCon(a, b, GenePool.ConType.MH);
        if (c == null || cons.contains(c)) return; // if logCon returns a new con, it will pass
        ((a.getX() < b.getX()) ? b : a).addInCons(c); // success so add c to rightmost gene
        cons.addInOrder(c);
    }

    private void addDHCon(SecuredList<DHng> nodes, SecuredList<DHcg> cons) {
        NodeGene a = nodes.getRandom(), b = nodes.getRandom();
        DHcg c = (DHcg) genePool.logCon(a, b, GenePool.ConType.DH);
        if (c == null || cons.contains(c)) return;
        ((a.getX() < b.getX()) ? b : a).addInCons(c);
        cons.addInOrder(c);
    }

    private void addMHNode(SecuredList<MHng> nodes, SecuredList<MHcg> cons) {

        MHcg c = cons.getRandom(); // sample a connection regardless of disability
        if (c == null) return; // only add when a connection exist

        // do not remove or disable c so we have a skip connection
        NodeGene f = c.getFG(), t = c.getTG(); // extract the endpoints
        // log node in between
        NodeGene m = genePool.logNode(-1, c, (f.getX() + t.getX()) / 2, midY.apply(f, t), GenePool.NodeType.MH, false);
        if (m == null || nodes.contains((MHng) m)) return; // if logNode returns a new con, it will pass
        MHcg f_m = (MHcg) genePool.logCon(f, m, GenePool.ConType.MH); // if m already exists in gene pool, then
        MHcg m_t = (MHcg) genePool.logCon(m, t, GenePool.ConType.MH); // f_m and m_t already exist in gene pool as well
        m.addInCons(f_m);
        t.addInCons(m_t);
        cons.addInOrder(f_m);
        cons.addInOrder(m_t);
        nodes.addInOrder((MHng) m);

    }

    private void addDHNode(SecuredList<DHng> nodes, SecuredList<DHcg> cons) {

        DHcg c = cons.getRandom();
        if (c == null) return;

        NodeGene f = c.getFG(), t = c.getTG();
        NodeGene m = genePool.logNode(-1, c, (f.getX() + t.getX()) / 2, midY.apply(f, t), GenePool.NodeType.DH, false);
        if (m == null || nodes.contains((DHng) m)) return;
        DHcg f_m = (DHcg) genePool.logCon(f, m, GenePool.ConType.DH);
        DHcg m_t = (DHcg) genePool.logCon(m, t, GenePool.ConType.DH);
        m.addInCons(f_m);
        t.addInCons(m_t);
        cons.addInOrder(f_m);
        cons.addInOrder(m_t);
        nodes.addInOrder((DHng) m);

    }

}
