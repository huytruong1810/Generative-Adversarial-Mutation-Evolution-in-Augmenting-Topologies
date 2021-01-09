package MyLogic.InferenceRule;

import MyLogic.Sentence.*;

public class MoveNotInward extends Rule {

    public static Sentence moveNotInward (Sentence s) {
        return s.pass(new MoveNotInward());
    }

    @Override
    public Sentence scopeNot (Sentence s) {

        Sentence result = null; // no biconditional or implication should exist when doing this step
        if (s instanceof Not) {
            Sentence inside = ((Not) s).operand;
            if (inside instanceof Or) { // DeMorgan Or
                result = new And();
                for (Sentence disj:((Or) inside).disjuncts)
                    result.add(new Not(disj.pass(this)).pass(this));
            }
            else if (inside instanceof And) { // DeMorgan And
                result = new Or();
                for (Sentence conj:((And) inside).conjuncts)
                    result.add(new Not(conj.pass(this)).pass(this));
            }
            else if (inside instanceof Not) // double negation elimination
                result = ((Not) inside).operand.pass(this);
            else if (inside instanceof Symbol)
                result = s;
        }
        return result;

    }

}
