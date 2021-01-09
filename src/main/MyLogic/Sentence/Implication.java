package MyLogic.Sentence;

import MyLogic.InferenceRule.Rule;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Implication extends Sentence {

    public Sentence premise, conclusion;

    public Implication (Sentence premise, Sentence conclusion) {
        this.premise = premise;
        this.conclusion = conclusion;
    }

    @Override
    public boolean equals (Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Implication))
            return false;
        return premise.equals(((Implication) o).premise) && conclusion.equals(((Implication) o).conclusion);
    }

    @Override
    public boolean evaluate (HashMap<String, Boolean> model) {
        // (p -> q) is equivalent to (~p V q)
        return !(premise.evaluate(model)) || conclusion.evaluate(model);
    }

    @Override
    public String formula() {
        return Sentence.parenthesize(premise.formula()) + "->" + Sentence.parenthesize(conclusion.formula());
    }

    @Override
    public Set<String> symbols() {
        Set<String> set = new HashSet<>();
        set.addAll(premise.symbols());
        set.addAll(conclusion.symbols());
        return set;
    }

    @Override
    public Sentence pass (Rule r) {
        return r.scopeConditional(this);
    }

}
