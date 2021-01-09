import MyLogic.InferenceRule.BicondElim;
import MyLogic.Sentence.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class BicondElimTest {

    Sentence p, q, r;

    @BeforeEach
    void init() {
        p = new Symbol("P");
        q = new Symbol("Q");
        r = new Symbol("R");
    }

    @Test
    void test1() {
        // KB = ((~p)<->q) V (~p) V r
        Sentence KB = new Or(new Biconditional(new Not(p), q), new Not(p), r);
        Sentence bicondElimKB = BicondElim.eliminateBiconditional(KB);
        assertEquals("(((~p)->q) ^ (q->(~p))) V (~p) V r", bicondElimKB.formula());
    }

    @Test
    void test2() {
        // KB = (p<->(q ^ r ^ p)) ^ r
        Sentence KB = new And(new Biconditional(p, new And(q, r, p)));
        Sentence bicondElimKB = BicondElim.eliminateBiconditional(KB);
        assertEquals("((p->(q ^ r ^ p)) ^ ((q ^ r ^ p)->p) ^ r", bicondElimKB.formula());
    }

    @Test
    void test3() {
        // KB = q<->(r<->(~p))
        Sentence KB = new Biconditional(q, new Biconditional(r, new Not(p)));
        Sentence bicondElimKB = BicondElim.eliminateBiconditional(KB);
        assertEquals("(q->((r->(~p)) ^ ((~p)->r))) ^ (((r->(~p)) ^ ((~p)->r))->q)", bicondElimKB.formula());
    }

    @Test
    void test4() {
        // KB = ((q<->r)->p)<->r
        Sentence KB = new Biconditional(new Implication(new Biconditional(q, r),p),r);
        Sentence bicondElimKB = BicondElim.eliminateBiconditional(KB);
        assertEquals("((((q->r) ^ (r->q))->p)->r) ^ (r->(((q->r) ^ (r->q))->p))", bicondElimKB.formula());
    }

}
