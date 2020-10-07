package Neat.Genome;

import Neat.DataStructures.RandomHashSet;
import Neat.Neat;

public class Genome {

    private RandomHashSet<ConnectionGene> connections = new RandomHashSet<>();
    private RandomHashSet<NodeGene> nodes = new RandomHashSet<>();

    private Neat neat;

    public Genome(Neat neat) {
        this.neat = neat;
    }

    public double distanceTo(Genome other) {

        Genome g = this;
        RandomHashSet<ConnectionGene> c1 = connections;
        RandomHashSet<ConnectionGene> c2 = other.connections;
        int i = 0, j = 0;
        int D = 0, E = 0, S = 0;
        double W = 0;
        int s1 = c1.size(), s2 = c2.size();

        if (c1.get(s1-1).getIN() < c2.get(s2-1).getIN()) {
            Genome t = g;
            g = other;
            other = t;
        }

        while (i < s1 && j < s2) {
            ConnectionGene cg1 = c1.get(i);
            ConnectionGene cg2 = c2.get(j);
            int in1 = cg1.getIN();
            int in2 = cg2.getIN();
            if (in1 == in2) {
                S++;
                W += Math.abs(cg1.getWeight() - cg2.getWeight());
                i++;
                j++;
            }
            else if (in1 > in2) j++;
            else i++;
            W /= S;
            E = s1 - i;
        }

        double N = Math.max(s1, s2);
        if (N < 20) N = 1;
        return Neat.C1*D/N + Neat.C2*E/N + Neat.C3*W;

    }

    public static Genome crossOver(Genome g1, Genome g2) {

        Neat neat = g1.getNeat();
        Genome child = g1.getNeat().emptyGenome();
        RandomHashSet<ConnectionGene> c1 = g1.connections;
        RandomHashSet<ConnectionGene> c2 = g2.connections;
        int i = 0, j = 0;
        int s1 = c1.size(), s2 = c2.size();

        while (i < s1 && j < s2) {
            ConnectionGene cg1 = c1.get(i);
            ConnectionGene cg2 = c2.get(j);
            int in1 = cg1.getIN();
            int in2 = cg2.getIN();
            if (in1 == in2) {
                if (Math.random() > 0.5) child.connections.add(neat.getConnection(cg1));
                else child.connections.add(neat.getConnection(cg2));
                i++;
                j++;
            }
            else if (in1 > in2) j++;
            else {
                child.connections.add(neat.getConnection(cg1));
                i++;
            }
        }

        while (i < s1) child.connections.add(neat.getConnection(c1.get(i++)));

        for (ConnectionGene c: child.connections.getData()) {
            child.getNodes().add(c.getFG());
            child.getNodes().add(c.getTG());
        }

        return child;

    }

    public void mutate() {
        if (Neat.PMAC > Math.random()) mutateAddConnection();
        if (Neat.PMAN > Math.random()) mutateAddNode();
        if (Neat.PMWS > Math.random()) mutateWeightShift();
        if (Neat.PMWR > Math.random()) mutateWeightRand();
        if (Neat.PMCE > Math.random()) mutateConnectionEnable();
    }

    public void mutateAddConnection() {
        for (int i = 0; i < 100; ++i) {
            NodeGene a = nodes.getRandom();
            NodeGene b = nodes.getRandom();
            // no same level connection allowed
            if (a.getX() == b.getX()) continue;
            ConnectionGene c;
            if (a.getX() < b.getX()) c = new ConnectionGene(a, b);
            else c = new ConnectionGene(b, a);
            // don't make connection if already exist
            if (connections.contains(c)) continue;
            c = neat.getConnection(c.getFG(), c.getTG());
            c.setWeight(c.getWeight() + (Math.random()*2-1)*Neat.wShiftStrength);
            connections.addInOrder(c);
            return; // add only 1 new connection
        }
    }

    public void mutateAddNode() {
        ConnectionGene c = connections.getRandom();
        if (c == null) return;
        NodeGene a = c.getFG();
        NodeGene b = c.getTG();
        NodeGene m = neat.getNode();
        m.setX((a.getX()+b.getX())/2);
        m.setY((a.getY()+b.getY())/2 + Math.random()*500-250);
        ConnectionGene am = neat.getConnection(a, m);
        ConnectionGene mb = neat.getConnection(m, b);
        am.setWeight(1);
        mb.setWeight(c.getWeight());
        mb.setEnabled(c.isEnabled());
        connections.remove(c);
        connections.add(am);
        connections.add(mb);
        nodes.add(m);
    }

    public void mutateWeightShift() {
        ConnectionGene c = connections.getRandom();
        if (c != null) c.setWeight(c.getWeight() + (Math.random()*2-1)*Neat.wShiftStrength);
    }

    public void mutateWeightRand() {
        ConnectionGene c = connections.getRandom();
        if (c != null) c.setWeight((Math.random()*2-1)*Neat.wRandomStrength);
    }

    public void mutateConnectionEnable() {
        ConnectionGene c = connections.getRandom();
        if (c != null) c.setEnabled(!c.isEnabled());
    }

    public RandomHashSet<ConnectionGene> getConnections() { return connections; }
    public RandomHashSet<NodeGene> getNodes() { return nodes; }
    public Neat getNeat() { return neat; }

}
