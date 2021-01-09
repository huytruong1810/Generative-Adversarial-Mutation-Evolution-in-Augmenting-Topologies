package MyLogic.Sentence;

import MyLogic.InferenceRule.MoveNotInward;
import MyLogic.InferenceRule.Rule;

import java.util.*;

public class Or extends Sentence {

    public ArrayList<Sentence> disjuncts;

    public Or (Sentence ...disjuncts) {
        this.disjuncts = new ArrayList<>(Arrays.asList(disjuncts));
    }

    @Override
    public boolean equals (Object o) {

        if (o == this)
            return true;
        if (!(o instanceof Or))
            return false;

        ArrayList<Sentence> ls = new ArrayList(((Or) o).disjuncts);
        if (disjuncts.size() != ls.size())
            return false;
        for (Sentence disj:disjuncts) {
            if (!(ls.contains(disj)))
                return false;
        }
        return true;

    }

    @Override
    public void unique() {

        ArrayList<Sentence> uniqueSet = new ArrayList<>();
        // p V p = p
        for (Sentence disj:disjuncts) {
            if (!(uniqueSet.contains(disj)))
                uniqueSet.add(disj);
        }/*
        // p V ~p = TRUE
        int n = uniqueSet.size();
        int i = 0;
        while (i < n) {
            Sentence cur = uniqueSet.get(i);
            if (cur instanceof Symbol || cur instanceof Not) {
                for (int j = i + 1; j < n; ++j) {
                    Sentence other = uniqueSet.get(j);
                    if (other instanceof Symbol || other instanceof Not) {
                        if (cur.equals(MoveNotInward.moveNotInward(new Not(other)))) {
                            uniqueSet.set(i, new Symbol("TRUE"));
                            uniqueSet.remove(j);
                            n = uniqueSet.size();
                            i = -1;
                            break;
                        }
                    }
                }
            }
            i++;
        }*/
        disjuncts = uniqueSet;

    }

    @Override
    public void add (Sentence disj) {
        disjuncts.add(disj);
    }

    @Override
    public boolean evaluate (HashMap<String, Boolean> model) {
        // if one sentence is true, the entire disjunction is true
        for (Sentence disj:disjuncts)
            if (disj.evaluate(model) == true)
                return true;
        return false;
    }

    @Override
    public String formula() {
        // assuming there are more than two sentence
        String s = Sentence.parenthesize(disjuncts.get(0).formula());
        int n = disjuncts.size();
        for (int i = 1; i < n; ++i)
            s += " V " + Sentence.parenthesize(disjuncts.get(i).formula());
        return s;
    }

    @Override
    public Set<String> symbols() {
        Set<String> set = new HashSet<>();
        for (Sentence disj:disjuncts)
            set.addAll(disj.symbols());
        return set;
    }

    @Override
    public Sentence pass (Rule r) {
        return r.scopeAndOr(this);
    }

}
