package MyLogic.Sentence;

import MyLogic.InferenceRule.Rule;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Biconditional extends Sentence {

    public Sentence left, right;

    public Biconditional (Sentence left, Sentence right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean equals (Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Biconditional))
            return false;
        Sentence otherLeft = ((Biconditional) o).left;
        Sentence otherRight = ((Biconditional) o).right;
        return (left.equals(otherLeft) && right.equals(otherRight)) || (left.equals(otherRight) && right.equals(otherLeft));
    }

    @Override
    public boolean evaluate (HashMap<String, Boolean> model) {
        // (p <-> q) is equivalent to (p -> q ^ q -> p)
        return (!left.evaluate(model) || right.evaluate(model)) &&
               (!right.evaluate(model) || left.evaluate(model));
    }

    @Override
    public String formula() {
        return Sentence.parenthesize(left.formula()) + "<->" + Sentence.parenthesize(right.formula());
    }

    @Override
    public Set<String> symbols() {
        Set<String> set = new HashSet<>();
        set.addAll(left.symbols());
        set.addAll(right.symbols());
        return set;
    }

    @Override
    public Sentence pass (Rule r) {
        return r.scopeConditional(this);
    }

}
