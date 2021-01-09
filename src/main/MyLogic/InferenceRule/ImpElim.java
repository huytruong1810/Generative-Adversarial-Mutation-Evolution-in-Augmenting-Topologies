package MyLogic.InferenceRule;

import MyLogic.Sentence.*;

public class ImpElim extends Rule {

    public static Sentence eliminateImplication (Sentence s) {
        return s.pass(new ImpElim());
    }

    @Override
    public Sentence scopeConditional (Sentence s) {

        Sentence result = null; // no biconditional should exist when doing this step
        if (s instanceof Implication) {
            result = new Or(new Not(((Implication) s).premise.pass(this)), ((Implication) s).conclusion.pass(this));
        }
        return result;

    }

}
