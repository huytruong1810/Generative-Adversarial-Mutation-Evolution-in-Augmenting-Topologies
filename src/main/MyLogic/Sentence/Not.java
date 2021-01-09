package MyLogic.Sentence;

import MyLogic.InferenceRule.Rule;

import java.util.HashMap;
import java.util.Set;

public class Not extends Sentence {

    public Sentence operand;

    public Not (Sentence operand) {
        this.operand = operand;
    }

    @Override
    public boolean equals (Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Not))
            return false;
        return operand.equals(((Not) o).operand);
    }

    @Override
    public boolean evaluate (HashMap<String, Boolean> model) {
        return !(operand.evaluate(model));
    }

    @Override
    public String formula() {
        return "~" + Sentence.parenthesize(operand.formula());
    }

    @Override
    public Set<String> symbols() {
        return operand.symbols();
    }

    @Override
    public Sentence pass (Rule r) {
        return r.scopeNot(this);
    }

}
