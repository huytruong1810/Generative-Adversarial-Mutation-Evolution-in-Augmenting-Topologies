package NEAT;

import NEAT.DataStructures.SecuredList;
import NEAT.DataStructures.SecuredMap;
import NEAT.Genome.DecisionHead.DHcg;
import NEAT.Genome.DecisionHead.DHng;
import NEAT.Genome.ConGene;
import NEAT.Genome.MemoryHead.MHcg;
import NEAT.Genome.MemoryHead.MHng;
import NEAT.Genome.NodeGene;

public class GenePool { // only keep proxies, no references to functional genes

    public enum NodeType {MH, DH}
    public enum ConType {MH, DH}

    public static final int MAX_NODE = (int) Math.pow(2, 5);

    private int MH_cap;
    private int DH_cap;

    private final SecuredList<MHcg> MHConAnc; // keep record of IN of each con
    private final SecuredMap<ConGene, MHng> MHNodeAnc; // keep record of IN of each node on each connection, except bases

    private final SecuredList<DHcg> DHConAnc; // keep record of IN of each con
    private final SecuredMap<ConGene, DHng> DHNodeAnc; // keep record of IN of each node on each connection, except bases

    public GenePool() {

        MH_cap = DH_cap = 0; // IN starts at 1

        MHConAnc = new SecuredList<>();
        MHNodeAnc = new SecuredMap<>();

        DHConAnc = new SecuredList<>();
        DHNodeAnc = new SecuredMap<>();

    }

    public int getMH_cap() {
        return MH_cap;
    }

    public int getDH_cap() {
        return DH_cap;
    }

    public void clear() {
        MH_cap = DH_cap = 0;
        MHNodeAnc.clear();
        MHConAnc.clear();
        DHNodeAnc.clear();
        DHConAnc.clear();
    }

    /**
     * Retrieve the innovation number of node in between the connection.
     * If the connection does not have a node in between, returns -1
     * @param con - the connection
     * @param type - type of node gene
     * @return innovation number of node in between connection or -1
     */
    public int getBetweenNodeIN(ConGene con, NodeType type) {
        switch (type) {
            case MH: return (MHNodeAnc.containsKey(con)) ? MHNodeAnc.get(con).getIN() : -1;
            case DH: return (DHNodeAnc.containsKey(con)) ? DHNodeAnc.get(con).getIN() : -1;
            default: throw new IllegalStateException("Unknown node ancestry access.");
        }
    }

    /**
     * NOTE: Innovation numbers start from 1 but is indexed 0 in ancestry record
     * Make a new node gene with new innovation number and record it in the ancestry
     * @param IN - innovation number, should be >= 0 for base nodes
     * @param con - associated connection, should be non-null for non-base nodes
     * @param x - node gene x coor if i < 0, if not, ignore
     * @param y - node gene y coor if i < 0, if not, ignore
     * @param type - type of node gene
     * @param isBase - is this a base node
     * @return a new random node gene with recorded IN or null if adding a new node and maximum is reached
     */
    public NodeGene logNode(int IN, ConGene con, double x, double y, NodeType type, boolean isBase) {

        NodeGene n;
        NodeGene proxyNode;

        if (isBase) { // base nodes are the roots, amount of base nodes is never higher than maximum node allowed
            if (con != null) throw new IllegalStateException("Base nodes should not have associated connections.");
            switch (type) {
                case MH:
                    if (MHNodeAnc.containsRoot(IN)) { // base node already exists in ancestry
                        proxyNode = MHNodeAnc.getRoot(IN);
                        n = new MHng(proxyNode.getIN());
                        x = proxyNode.getX();
                        y = proxyNode.getY();
                    } else { // this node is a new root
                        n = new MHng(++MH_cap); // give it a unique IN
                        MHNodeAnc.putRoot(MH_cap, new MHng(n, x, y)); // save xy proxy
                    }
                    break;
                case DH:
                    if (DHNodeAnc.containsRoot(IN)) { // base node already exists in ancestry
                        proxyNode = DHNodeAnc.getRoot(IN);
                        n = new DHng(proxyNode.getIN());
                        x = proxyNode.getX();
                        y = proxyNode.getY();
                    } else { // this node is a new root
                        n = new DHng(++DH_cap); // give it a unique IN
                        DHNodeAnc.putRoot(DH_cap, new DHng(n, x, y)); // save xy proxy
                    }
                    break;
                default: throw new IllegalStateException("Unknown node ancestry access.");
            }
        } else {
            if (IN > 0) throw new IllegalStateException("IN should not be specified for non-base nodes.");
            if (con == null) throw new IllegalStateException("Non-base nodes must have associated connections.");
            switch (type) {
                case MH:
                    if (MHNodeAnc.containsKey(con)) { // node already exists in ancestry
                        proxyNode = MHNodeAnc.get(con);
                        n = new MHng(proxyNode.getIN());
                        x = proxyNode.getX();
                        y = proxyNode.getY();
                    } else { // this node is new in between this connection
                        if (MH_cap + 1 > MAX_NODE) return null;
                        n = new MHng(++MH_cap);
                        // save proxy key and proxy value
                        MHNodeAnc.put(new MHcg(new MHng(con.getFG()), new MHng(con.getTG()), 'p'), new MHng(n, x, y));
                    }
                    break;
                case DH:
                    if (DHNodeAnc.containsKey(con)) { // node already exists in ancestry
                        proxyNode = DHNodeAnc.get(con);
                        n = new DHng(proxyNode.getIN());
                        x = proxyNode.getX();
                        y = proxyNode.getY();
                    } else { // this node is new in between this connection
                        if (DH_cap + 1 > MAX_NODE) return null;
                        n = new DHng(++DH_cap);
                        // save proxy key and proxy value
                        DHNodeAnc.put(new DHcg(new DHng(con.getFG()), new DHng(con.getTG()), 'p'), new DHng(n, x, y));
                    }
                    break;
                default:
                    throw new IllegalStateException("Unknown node ancestry access.");
            }
        }
        n.setX(x); // these coordinates could have been changed due to retrieving
        n.setY(y);

        return n;

    }

    /**
     * NOTE: Innovation numbers start from 1 but index 0 in ancestry record
     * Make a new connection gene, if this gene has already existed in
     * the ancestry, give it its ancestral innovation number,
     * else, make a new ancestral entry
     * @param a - node gene
     * @param b - node gene
     * @return a new random connection gene with recorded IN or null if same level connection
     */
    public ConGene logCon(NodeGene a, NodeGene b, ConType type) {

        double ax = a.getX(), bx = b.getX();
        if (ax == bx) return null; // no same level connection allowed

        NodeGene f, t; // leftmost node comes first
        ConGene c;
        if (ax < bx) { f = a; t = b; }
        else { f = b; t = a; }

        switch (type) {
            case MH:
                c = new MHcg(f, t);
                if (MHConAnc.contains((MHcg) c)) { // basing on the endpoints
                    MHcg proxyCon = MHConAnc.get((MHcg) c);
                    c.setIN(proxyCon.getIN());
                } else { // connection don't exist in ancestry
                    int cIN = MHConAnc.size() + 1;
                    c.setIN(cIN);
                    MHcg proxyCon = new MHcg(new MHng(f), new MHng(t), 'p');
                    proxyCon.setIN(cIN);
                    MHConAnc.add(proxyCon); // save proxy
                }
                break;
            case DH:
                c = new DHcg(f, t);
                if (DHConAnc.contains((DHcg) c)) { // basing on the endpoints
                    DHcg proxyCon = DHConAnc.get((DHcg) c);
                    c.setIN(proxyCon.getIN());
                } else { // connection don't exist in ancestry
                    int cIN = DHConAnc.size() + 1;
                    c.setIN(cIN);
                    DHcg proxyCon = new DHcg(new DHng(f), new DHng(t), 'p');
                    proxyCon.setIN(cIN);
                    DHConAnc.add(proxyCon); // save proxy
                }
                break;
            default: throw new IllegalStateException("Unknown connection ancestry access.");
        }

        return c; // no need to clone

    }

}
