package MyLogic.InferenceRule;

import MyLogic.Sentence.*;

public abstract class Rule {

    private void collectAnd (Sentence s, Sentence result) {
        if (s instanceof And) {
            for (Sentence conj:((And) s).conjuncts)
                collectAnd(conj.pass(this), result);
        }
        else
            result.add(s);
    }

    private void collectOr (Sentence s, Sentence result) {
        if (s instanceof Or) {
            for (Sentence disj:((Or) s).disjuncts)
                collectOr(disj.pass(this), result);
        }
        else
            result.add(s);
    }

    public Sentence scopeSymbol (Sentence s) {
        return s;
    }

    public Sentence scopeNot (Sentence s) {
        return new Not(((Not) s).operand.pass(this));
    }

    public Sentence scopeAndOr (Sentence s) {

        Sentence result = null;
        if (s instanceof And) {
            result = new And();
            collectAnd(s, result);
        }
        else if (s instanceof Or) {
            result = new Or();
            collectOr(s, result);
        }
        result.unique();
        return result;

    }

    public Sentence scopeConditional (Sentence s) {

        Sentence result = null;
        if (s instanceof Biconditional)
            result = new Biconditional(((Biconditional) s).left.pass(this), ((Biconditional) s).right.pass(this));
        else if (s instanceof Implication)
            result = new Implication(((Implication) s).premise.pass(this), ((Implication) s).conclusion.pass(this));
        return result;

    }

}
