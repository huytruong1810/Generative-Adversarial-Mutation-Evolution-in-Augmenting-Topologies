package NEAT;

import NEAT.DataStructures.SecuredList;
import NEAT.Genome.DecisionHead.DHcg;
import NEAT.Genome.DecisionHead.DHng;
import NEAT.Genome.ConGene;
import NEAT.Genome.MemoryHead.MHcg;
import NEAT.Genome.MemoryHead.MHng;
import NEAT.Genome.NodeGene;

public class GenePool { // only keep proxies, no references to full gene

    public enum NodeType { MRU, ACU }
    public enum ConType { MRU, ACU }

    public static final int MAX_NODE = (int) Math.pow(2, 5);

    private int MRU_cap;
    private int ACU_cap;

    private final SecuredList<MHcg> MRUConAnc; // keep record of IN of each con
    private final SecuredList<MHng> MRUNodeAnc; // only keep the original frame nodes

    private final SecuredList<DHcg> ACUConAnc; // keep record of IN of each con
    private final SecuredList<DHng> ACUNodeAnc; // only keep the original frame nodes

    public GenePool() {

        MRU_cap = ACU_cap = 0;

        MRUConAnc = new SecuredList<>();
        MRUNodeAnc = new SecuredList<>();

        ACUConAnc = new SecuredList<>();
        ACUNodeAnc = new SecuredList<>();

    }

    public int getMRU_cap() {
        return MRU_cap;
    }

    public int getACU_cap() {
        return ACU_cap;
    }

    public void clear() {
        MRU_cap = ACU_cap = 0;
        MRUNodeAnc.clear();
        MRUConAnc.clear();
        ACUNodeAnc.clear();
        ACUConAnc.clear();
    }

    /**
     * NOTE: Innovation numbers start from 1 but is indexed 0 in ancestry record
     * Make a new node gene with new innovation number and record it in the ancestry
     * @param i - node index if possible, if not, it is < 0
     * @param x - node gene x coor if i < 0, if not, ignore
     * @param y - node gene y coor if i < 0, if not, ignore
     * @param type - type of node gene
     * @param isBase - is this a base node
     * @return a new random node gene with recorded IN
     */
    public NodeGene logNode(int i, double x, double y, NodeType type, boolean isBase) {

        NodeGene n;

        if (i < 0) {
            switch (type) {
                case MRU:
                    n = new MHng(++MRU_cap);
                    if (isBase) MRUNodeAnc.add(new MHng(n, x, y)); // save xy proxy of base node
                    break;
                case ACU:
                    n = new DHng(++ACU_cap);
                    if (isBase) ACUNodeAnc.add(new DHng(n, x, y)); // save xy proxy of base node
                    break;
                default:
                    throw new IllegalStateException("Unknown node ancestry access.");
            }
            n.setX(x);
            n.setY(y);
        }
        else {
            if (!isBase) throw new IllegalStateException("Node ancestry does not keep non-base node.");
            NodeGene proxyNode;
            switch (type) {
                case MRU:
                    proxyNode = MRUNodeAnc.get(i);
                    n = new MHng(proxyNode.getIN());
                    break;
                case ACU:
                    proxyNode = ACUNodeAnc.get(i);
                    n = new DHng(proxyNode.getIN());
                    break;
                default:
                    throw new IllegalStateException("Unknown node ancestry access.");
            }
            n.setX(proxyNode.getX());
            n.setY(proxyNode.getY());
        }

        return n; // no need to clone

    }

    /**
     * NOTE: Innovation numbers start from 1 but index 0 in ancestry record
     * Make a new connection gene, if this gene has already existed in
     * the ancestry, give it its ancestral innovation number,
     * else, make a new ancestral entry
     * @param a - node gene
     * @param b - node gene
     * @return a new random connection gene with recorded IN
     */
    public ConGene logCon(NodeGene a, NodeGene b, ConType type) {

        double ax = a.getX(), bx = b.getX();
        if (ax == bx) return null; // no same level connection allowed

        NodeGene f, t; // leftmost node comes first
        ConGene c;
        if (ax < bx) { f = a; t = b; }
        else { f = b; t = a; }

        switch (type) {
            case MRU:
                c = new MHcg(f, t);
                if (MRUConAnc.contains((MHcg) c)) { // basing on the endpoints
                    MHcg proxyCon = MRUConAnc.get((MHcg) c);
                    c.setIN(proxyCon.getIN());
                }
                else { // connection don't exist in ancestry
                    int cIN = MRUConAnc.size() + 1;
                    c.setIN(cIN);
                    MHcg proxyCon = new MHcg(new MHng(f), new MHng(t), 'p');
                    proxyCon.setIN(cIN);
                    MRUConAnc.add(proxyCon); // save proxy
                }
                break;
            case ACU:
                c = new DHcg(f, t);
                if (ACUConAnc.contains((DHcg) c)) { // basing on the endpoints
                    DHcg proxyCon = ACUConAnc.get((DHcg) c);
                    c.setIN(proxyCon.getIN());
                }
                else { // connection don't exist in ancestry
                    int cIN = ACUConAnc.size() + 1;
                    c.setIN(cIN);
                    DHcg proxyCon = new DHcg(new DHng(f), new DHng(t), 'p');
                    proxyCon.setIN(cIN);
                    ACUConAnc.add(proxyCon); // save proxy
                }
                break;
            default: throw new IllegalStateException("Unknown connection ancestry access.");
        }

        return c; // no need to clone

    }

}
