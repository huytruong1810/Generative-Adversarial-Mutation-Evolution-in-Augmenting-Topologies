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
            P_PR = 0.4, // Prob(parameter randomize)
            P_AC = 0.9, // Prob(add connection)
            P_AN = 0.01, // Prob(add node)
            ACS = 1, // add connection strength
            ANS = 0.035; // add node strength

    private final GenePool genePool;

    public Mutator(GenePool anc) {
        // should only be called by NEAT
        genePool = anc;
    }

    public void mutateStructure(Individual individual) throws CloneNotSupportedException {

        MHg mru = individual.getMRU();
        DHg acu = individual.getACU();

        // a chance for child to not inherit parent's parameters
        if (P_PR > Math.random()) {
            mru.randomParams();
            acu.randomParams();
        }

        // safe these so we do not over-add nodes or connections
        int numMRUCons = mru.getCons().size();
        int numACUCons = acu.getCons().size();
        int numMRUNodes = mru.getNodes().size();
        int numACUNodes = acu.getNodes().size();

        // add connection mutation (can be ancestral unique or not)
        for (int i = 0, m = (int) (ACS * (numMRUNodes * 2 - numMRUCons)); i < m; ++i) {
            if (P_AC > Math.random())
                addMRUCon(mru.getNodes(), mru.getCons());
        }
        for (int i = 0, m = (int) (ACS * (numACUNodes * 2 - numACUCons)); i < m; ++i) {
            if (P_AC > Math.random())
                addACUCon(acu.getNodes(), acu.getCons());
        }

        // add new nodes (2 new connections per node added)
        for (int i = 0, m = (int) (ANS * numMRUCons); i < m; ++i) {
            if (P_AN > Math.random())
                addMRUNode(mru.getNodes(), mru.getCons());
        }
        for (int i = 0, m = (int) (ANS * numACUCons); i < m; ++i) {
            if (P_AN > Math.random())
                addACUNode(acu.getNodes(), acu.getCons());
        }

    }

    private void addMRUCon(SecuredList<MHng> nodes, SecuredList<MHcg> cons) {
        NodeGene a = nodes.getRandom(), b = nodes.getRandom(); // random-select the endpoint-nodes
        MHcg c = (MHcg) genePool.logCon(a, b, GenePool.ConType.MRU);
        if (c == null || cons.contains(c)) return; // if logCon returns a new con, it will pass
        ((a.getX() < b.getX()) ? b : a).addInCons(c); // success so add c to rightmost gene
        cons.addInOrder(c);
    }

    private void addACUCon(SecuredList<DHng> nodes, SecuredList<DHcg> cons) {
        NodeGene a = nodes.getRandom(), b = nodes.getRandom();
        DHcg c = (DHcg) genePool.logCon(a, b, GenePool.ConType.ACU);
        if (c == null || cons.contains(c)) return;
        ((a.getX() < b.getX()) ? b : a).addInCons(c);
        cons.addInOrder(c);
    }

    private void addMRUNode(SecuredList<MHng> nodes, SecuredList<MHcg> cons) {

        MHcg c = cons.getRandom(); // sample a connection regardless of disability
        if (c == null) return; // only add when a connection exist

        // do not remove or disable c so we have a skip connection
        NodeGene f = c.getFG(), t = c.getTG(); // extract the endpoints
        // make a new node in between because we do not keep track of repeated node
        NodeGene m = genePool.logNode(-1, (f.getX() + t.getX()) / 2, midY.apply(f, t), GenePool.NodeType.MRU, false);
        MHcg f_m = (MHcg) genePool.logCon(f, m, GenePool.ConType.MRU); // this connection just got added to ancestry
        MHcg m_t = (MHcg) genePool.logCon(m, t, GenePool.ConType.MRU); // added to ancestry right after
        m.addInCons(f_m);
        t.addInCons(m_t);
        cons.add(f_m); // connection just got added to ancestry so it is already in ascending IN order
        cons.add(m_t);
        nodes.add((MHng) m);

    }

    private void addACUNode(SecuredList<DHng> nodes, SecuredList<DHcg> cons) {

        // this operation affects both actor and critic in ACU
        DHcg c = cons.getRandom();
        if (c == null) return;

        NodeGene f = c.getFG(), t = c.getTG();
        NodeGene m = genePool.logNode(-1, (f.getX() + t.getX()) / 2, midY.apply(f, t), GenePool.NodeType.ACU, false);
        DHcg f_m = (DHcg) genePool.logCon(f, m, GenePool.ConType.ACU);
        DHcg m_t = (DHcg) genePool.logCon(m, t, GenePool.ConType.ACU);
        m.addInCons(f_m);
        t.addInCons(m_t);
        cons.add(f_m);
        cons.add(m_t);
        nodes.add((DHng) m);

    }

}
