package MyLogic;

import MyLogic.InferenceRule.*;
import MyLogic.Sentence.*;

public class Main {

    public static void main (String args[]) {

        Sentence p = new Symbol("P");
        Sentence q = new Symbol("Q");
        Sentence r = new Symbol("R");
        Sentence m = new Symbol("M");
        Sentence n = new Symbol("N");
        Sentence p2 = new Symbol("P");

        Sentence KB = new And(

                new Biconditional(q, new And(r, new Biconditional(p, q))),
                new Or(m, new Biconditional(n, p), new Implication(p, new Biconditional(r, new Not(r)))),
                new Not(new Biconditional(n, new Biconditional(new Not(m), n)))

        );


        System.out.println(KB.formula());
        Sentence KB1 = BicondElim.eliminateBiconditional(KB);
        System.out.println(KB1.formula());
        Sentence KB2 = ImpElim.eliminateImplication(KB1);
        System.out.println(KB2.formula());
        Sentence KB3 = MoveNotInward.moveNotInward(KB2);
        System.out.println(KB3.formula());
        Sentence KB4 = DistributeOrOverAnd.distributeOrOverAnd(KB3);
        System.out.println(KB4.formula());

    }

}
