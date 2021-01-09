package MyLogic.InferenceRule;

import MyLogic.Sentence.*;

import java.util.ArrayList;

public class DistributeOrOverAnd extends Rule {

    public static Sentence distributeOrOverAnd (Sentence s) {
        return s.pass(new DistributeOrOverAnd());
    }

    private Sentence distributeOr (Sentence alpha, Sentence andPhrase) {

        Sentence result = new And();
        for (Sentence conj:((And) andPhrase).conjuncts) {
            result.add(new Or(alpha, conj));
        }
        return result;

    }

    @Override
    public Sentence scopeAndOr (Sentence s) {

        Sentence result = null;
        if (s instanceof Or) {

            ArrayList<Sentence> disjuncts = ((Or) s).disjuncts;
            int n = disjuncts.size();
            int i = 0;
            while (i < n) {

                if (n == 1) // if Or has only 1 operand, Or is neglected
                    return disjuncts.get(0);
                Sentence cur = disjuncts.get(i).pass(this);
                if (cur instanceof And) {
                    if (i == 0) { // (a ^ b ^...) V c
                        disjuncts.set(i, distributeOr(disjuncts.get(i+1), cur).pass(this));
                        disjuncts.remove(i+1);
                    }
                    else { // a V (b ^ c ^...) 
                        disjuncts.set(i, distributeOr(disjuncts.get(i-1), cur).pass(this));
                        disjuncts.remove(i-1);
                    }
                    n = disjuncts.size();
                    i = 0;
                }
                else
                    i++;

            }
            result = super.scopeAndOr(s);

        }
        else if (s instanceof And)
            result = super.scopeAndOr(s);
        return result;

    }

}
