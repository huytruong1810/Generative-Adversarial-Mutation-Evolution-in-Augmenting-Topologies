package NEAT;

import NEAT.DataStructures.SecuredList;
import NEAT.Genome.DecisionHead.DHcg;
import NEAT.Genome.DecisionHead.DHg;
import NEAT.Genome.DecisionHead.DHng;
import NEAT.Genome.MemoryHead.MHcg;
import NEAT.Genome.MemoryHead.MHg;
import NEAT.Genome.MemoryHead.MHng;

import java.util.ArrayList;

public class Incubator {

    private static final double FAVOR_P1 = 0.5; // above 0.5 is favor, below is not favor

    public Incubator() { }

    /**-----------------------------------------------------------------------------------------------------------------
     * NOTE: direct cloning of gene are allowed here
     * Align two parents and perform cross over on them to
     * produce an offspring. The offspring will randomly inherit
     * a trait from either one of its parents
     * until its genomes are completed
     * @param p1 - parent 1 (fitter than parent 2)
     * @param p2 - parent 2
     * @return the offspring
     */
    public Individual crossBreed(Individual p1, Individual p2) throws CloneNotSupportedException {

        // if asexual reproduction, just return the clone of any
        if (p1 == p2) return p1.clone(); // allow clone to have same IDs as p1

        if (p1.getScore() < p2.getScore()) { // p1 must be fitter
            Individual temp = p1;
            p1 = p2;
            p2 = temp;
        }

        // p1 gets the privilege
        MHg p1_mru = p1.getMRU();
        DHg p1_acu = p1.getACU();

        // build child's architecture, in this version, all parent params are passed down to child

        int inputN = p1_mru.getInputNum(), hiddenN = p1_mru.getOutputNum(), outputN = p1_acu.getOutputNum();

        MHg childMRU = new MHg(inputN, hiddenN);
        DHg childACU = new DHg(hiddenN, outputN);
        SecuredList<MHng> mru_nodes = childMRU.getNodes();
        SecuredList<MHcg> mru_cons = childMRU.getCons();
        SecuredList<DHng> acu_nodes = childACU.getNodes();
        SecuredList<DHcg> acu_cons = childACU.getCons();

        // build base frame by cloning base nodes from p1
        SecuredList<MHng> p1_mrun = p1_mru.getNodes();
        SecuredList<DHng> p1_acun = p1_acu.getNodes();
        for (int i = 0; i < (inputN + hiddenN); ++i) mru_nodes.add(p1_mrun.get(i).clone());
        for (int i = 0; i < (hiddenN + outputN); ++i) acu_nodes.add(p1_acun.get(i).clone());

        for (MHcg c : collectMRURefs(p1_mru, p2.getMRU())) {

            // basing on innovation number of proxy
            MHng n1 = new MHng(c.getFG());
            if (mru_nodes.contains(n1)) n1 = mru_nodes.get(n1); // because cloning might not be necessary
            else { n1 = (MHng) c.getFG().clone(); mru_nodes.add(n1); } // hidden node trained params are passed down

            MHng n2 = new MHng(c.getTG());
            if (mru_nodes.contains(n2)) n2 = mru_nodes.get(n2);
            else { n2 = (MHng) c.getTG().clone(); mru_nodes.add(n2); } // hidden node trained params are passed down

            MHcg childCon = c.redirectClone(n1, n2); // con trained params are passed down

            n2.addInCons(childCon);
            mru_cons.add(childCon); // parents already have their connections in order

        }

        for (DHcg c : collectACURefs(p1_acu, p2.getACU())) {

            DHng n1 = new DHng(c.getFG());
            if (acu_nodes.contains(n1)) n1 = acu_nodes.get(n1);
            else { n1 = (DHng) c.getFG().clone(); acu_nodes.add(n1); } // hidden node trained params are passed down

            DHng n2 = new DHng(c.getTG());
            if (acu_nodes.contains(n2)) n2 = acu_nodes.get(n2);
            else { n2 = (DHng) c.getTG().clone(); acu_nodes.add(n2); } // hidden node trained params are passed down

            DHcg childCon = c.redirectClone(n1, n2); // con trained params are passed down

            n2.addInCons(childCon);
            acu_cons.add(childCon);

        }

        return new Individual(childMRU, childACU);

    }

    private ArrayList<MHcg> collectMRURefs(MHg p1_mru, MHg p2_mru) {
        ArrayList<MHcg> mruRefs = new ArrayList<>();
        SecuredList<MHcg> p1_mruc = p1_mru.getCons(), p2_mruc = p2_mru.getCons();
        int p1_i, p2_i, p1_s, p2_s; // iterator and size for each parent
        p1_i = p2_i = 0;
        p1_s = p1_mruc.size();
        p2_s = p2_mruc.size();
        while (p1_i < p1_s && p2_i < p2_s) {
            MHcg p1_c = p1_mruc.get(p1_i), p2_c = p2_mruc.get(p2_i);
            int in1 = p1_c.getIN(), in2 = p2_c.getIN();
            if (in1 == in2) { mruRefs.add((FAVOR_P1 > Math.random()) ? p1_c : p2_c); p1_i++; p2_i++; }
            else if (in1 < in2) { mruRefs.add(p1_c); p1_i++; } // collect at p1's disjoint
            else p2_i++; // ignore p2's disjoint
        }
        while (p1_i < p1_s) mruRefs.add(p1_mruc.get(p1_i++)); // collect all p1's excess
        return mruRefs;
    }

    private ArrayList<DHcg> collectACURefs(DHg p1_acu, DHg p2_acu) {
        ArrayList<DHcg> acuRefs = new ArrayList<>();
        SecuredList<DHcg> p1_acuc = p1_acu.getCons(), p2_acuc = p2_acu.getCons();
        int p1_i, p2_i, p1_s, p2_s;
        p1_i = p2_i = 0;
        p1_s = p1_acuc.size();
        p2_s = p2_acuc.size();
        while (p1_i < p1_s && p2_i < p2_s) {
            DHcg p1_c = p1_acuc.get(p1_i), p2_c = p2_acuc.get(p2_i);
            int in1 = p1_c.getIN(), in2 = p2_c.getIN();
            if (in1 == in2) { acuRefs.add((FAVOR_P1 > Math.random()) ? p1_c : p2_c); p1_i++; p2_i++; }
            else if (in1 < in2) { acuRefs.add(p1_c); p1_i++; }
            else p2_i++;
        }
        while (p1_i < p1_s) acuRefs.add(p1_acuc.get(p1_i++));
        return acuRefs;
    }

}
