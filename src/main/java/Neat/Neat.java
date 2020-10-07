package Neat;

import Neat.DataStructures.RandomHashSet;
import Neat.Genome.ConnectionGene;
import Neat.Genome.Genome;
import Neat.Genome.NodeGene;

import java.util.HashMap;

public class Neat {

    public static final int MAXNODE = (int)Math.pow(2, 20);
    public static final double PMAC = 0.4;
    public static final double PMAN = 0.4;
    public static final double PMWS = 0.4;
    public static final double PMWR = 0.4;
    public static final double PMCE = 0.4;
    public static final double wShiftStrength = 0.3;
    public static final double wRandomStrength = 1;
    public static final double C1 = 1, C2 = 1, C3 = 1;

    private HashMap<ConnectionGene, ConnectionGene> allConnections = new HashMap<>();
    private RandomHashSet<NodeGene> allNodes = new RandomHashSet<>();
    private int inputN, outputN, maxClients;

    public Neat(int inputN, int outputN, int clients) {
        this.reset(inputN, outputN, clients);
    }

    public Genome emptyGenome() {
        Genome g = new Genome(this);
        for (int i = 0; i < inputN+outputN; ++i) {
            g.getNodes().add(getNode(i+1));
        }
        return g;
    }

    public void reset(int inputN, int outputN, int clients) {
        this.inputN = inputN;
        this.outputN = outputN;
        this.maxClients = clients;

        allConnections.clear();
        allNodes.clear();

        for (int i = 0; i < inputN; ++i) {
            NodeGene n = getNode();
            n.setX(0);
            n.setY((i+1)/(double)(inputN+1)*1000);
        }

        for (int i = 0; i < outputN; ++i) {
            NodeGene n = getNode();
            n.setX(1000);
            n.setY((i+1)/(double)(outputN+1)*1000);
        }

    }

    public static ConnectionGene getConnection(ConnectionGene c) {
        ConnectionGene clone = new ConnectionGene(c.getFG(), c.getTG());
        clone.setWeight(c.getWeight());
        clone.setEnabled(c.isEnabled());
        return clone;
    }
    public ConnectionGene getConnection(NodeGene n1, NodeGene n2) {
        ConnectionGene cg = new ConnectionGene(n1, n2);
        if (allConnections.containsKey(cg))
            cg.setIN(allConnections.get(cg).getIN());
        else {
            cg.setIN(allConnections.size() + 1);
            allConnections.put(cg, cg);
        }
        return cg;
    }

    public NodeGene getNode() {
        NodeGene n = new NodeGene(allNodes.size() + 1);
        allNodes.add(n);
        return n;
    }
    public NodeGene getNode(int id) {
        if (id > 0 && id <= allNodes.size())
            return allNodes.get(id - 1);
        return getNode();
    }

}
