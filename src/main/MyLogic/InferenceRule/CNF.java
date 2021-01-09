package MyLogic.InferenceRule;

import MyLogic.Sentence.*;

import java.util.ArrayList;

public class CNF {

    CNF (Sentence s) {

        while (!isInCNF(s)) {
            s = moveNotInward(impElim(bicondElim(s))); // convert s to NNF
        }

    }

    Sentence bicondElim (Sentence s) {
        if (!(s instanceof Biconditional))
            return s;
        // p <-> q = (p -> q) ^ (q -> p)
        return new And(new Implication(((Biconditional) s).left, ((Biconditional) s).right),
                new Implication(((Biconditional) s).right, ((Biconditional) s).left));
    }

    Sentence impElim (Sentence s) {
        if (!(s instanceof Implication))
            return s;
        // p -> q = ~p V q
        return new Or(new Not(((Implication) s).premise), ((Implication) s).conclusion);
    }

    Sentence doubleNegElim (Sentence s) {
        if (!(s instanceof Not))
            return s;
        if (!(((Not) s).operand instanceof Not))
            return s;
        return ((Not) ((Not) s).operand).operand;
    }

    Sentence moveNotInward (Sentence s) {
        Sentence result;
        if (!(s instanceof Not))
            return s;
        if (((Not) s).operand instanceof And) { // De Morgan with and
            result = new Or();
            ArrayList<Sentence> conjuncts = ((And) ((Not) s).operand).conjuncts;
            // ~(p ^ q) = ~p V ~q
            for (Sentence conj:conjuncts)
                result.add(doubleNegElim(new Not(conj))); // ~(~p) = p
        }
        else if (((Not) s).operand instanceof Or) { // De Morgan with or
            result = new And();
            ArrayList<Sentence> disjuncts = ((Or) ((Not) s).operand).disjuncts;
            // ~(p V q) = ~p ^ ~q
            for (Sentence disj:disjuncts)
                result.add(doubleNegElim(new Not(disj))); // ~(~p) = p
        }
        else // if inside s is not a conjunction or a disjunction, it can only be a single symbol
            result = s;
        // assuming that the KB always get maintained so ~(~(~(~p))) would never appear
        return result;
    }

    Sentence distributeOrOverAnd (Sentence s) {
        if (!(s instanceof Or))
            return s;
        return null;
    }

    boolean isInCNF (Sentence s) {
        if (s instanceof And) {
            ArrayList<Sentence> conjuncts = ((And) s).conjuncts;
            for (Sentence conj:conjuncts) {
                if (!(conj instanceof Or))
                    return false;
            }
            return true;
        }
        return false;
    }

}
