package MyLogic.InferenceRule;

import MyLogic.Sentence.*;

public class BicondElim extends Rule {

    public static Sentence eliminateBiconditional (Sentence s) {
        return s.pass(new BicondElim());
    }

    @Override
    public Sentence scopeConditional (Sentence s) {

        Sentence result = null;
        if (s instanceof Biconditional) {
            Sentence left = ((Biconditional) s).left.pass(this);
            Sentence right = ((Biconditional) s).right.pass(this);
            result = new And(new Implication(left, right), new Implication(right, left));
        }
        else if (s instanceof Implication)
            result = super.scopeConditional(s);
        return result;
    }

}
