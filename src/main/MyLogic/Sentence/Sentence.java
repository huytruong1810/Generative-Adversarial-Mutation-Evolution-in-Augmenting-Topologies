package MyLogic.Sentence;

import MyLogic.InferenceRule.Rule;
import MyLogic.Utils;

import java.util.HashMap;
import java.util.Set;

public abstract class Sentence {

    abstract public boolean evaluate (HashMap<String,Boolean> model);

    abstract public String formula();

    abstract public Set<String> symbols();

    abstract public Sentence pass (Rule r);

    public void unique() {
        return;
    }

    public void add (Sentence conj) {
        return;
    }

    public static String parenthesize (String s) {
        int n = s.length();
        // if the string is not empty, not all alphabetical and not properly parenthesized
        if (n != 0 && !Utils.isAlpha(s) && !(s.charAt(0) == '(' && s.charAt(n-1) == ')' && Utils.isBalanced(s.substring(1, n-1))))
            return "("+s+")"; // return the parenthesized version
        // else if the string is empty or if it is all alphabetical or is already properly parenthesized
        return s; // don't do anything to it
    }


}
