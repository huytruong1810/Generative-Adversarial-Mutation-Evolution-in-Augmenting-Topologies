package MyLogic.Sentence;

import MyLogic.InferenceRule.Rule;

import java.util.*;

public class And extends Sentence {

    public ArrayList<Sentence> conjuncts;

    public And (Sentence ...conjuncts) {
        this.conjuncts = new ArrayList<>(Arrays.asList(conjuncts));
    }

    @Override
    public boolean equals (Object o) {

        if (o == this)
            return true;
        if (!(o instanceof And))
            return false;

        ArrayList<Sentence> ls = new ArrayList(((And) o).conjuncts);
        if (conjuncts.size() != ls.size())
            return false;
        for (Sentence conj:conjuncts) {
            if (!(ls.contains(conj)))
                return false;
        }
        return true;

    }

    @Override
    public void unique() {

        ArrayList<Sentence> uniqueSet = new ArrayList<>();
        for (Sentence conj:conjuncts) {
            if (!(uniqueSet.contains(conj)))
                uniqueSet.add(conj);
        }

        conjuncts = uniqueSet;

    }

    @Override
    public void add (Sentence conj) {
        conjuncts.add(conj);
    }

    @Override
    public boolean evaluate (HashMap<String, Boolean> model) {
        // if one sentence is false, the entire conjunction is false
        for (Sentence conj:conjuncts)
            if (conj.evaluate(model) == false)
                return false;
        return true;
    }

    @Override
    public String formula() {
        // assuming there are more than two sentence
        String s = Sentence.parenthesize(conjuncts.get(0).formula());
        int n = conjuncts.size();
        for (int i = 1; i < n; ++i)
            s += " ^ " + Sentence.parenthesize(conjuncts.get(i).formula());
        return s;
    }

    @Override
    public Set<String> symbols() {
        Set<String> set = new HashSet<>();
        for (Sentence conj:conjuncts)
            set.addAll(conj.symbols());
        return set;
    }

    @Override
    public Sentence pass (Rule r) {
        return r.scopeAndOr(this);
    }

}
