package MyLogic.Sentence;

import MyLogic.InferenceRule.Rule;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Symbol extends Sentence {

    public String name;

    public Symbol (String name) {
        this.name = name;
    }

    @Override
    public boolean equals (Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Symbol))
            return false;
        return name.equals(((Symbol) o).name);
    }

    @Override
    public boolean evaluate (HashMap<String, Boolean> model) {
        return model.get(this.name);
    }

    @Override
    public String formula() {
        return name;
    }

    @Override
    public Set<String> symbols() {
        Set<String> set = new HashSet<>();
        set.add(name);
        return set;
    }

    @Override
    public Sentence pass (Rule r) {
        return r.scopeSymbol(this);
    }

}
