import MyLogic.Sentence.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class ImpTest {

    Sentence i;

    @BeforeEach
    void init() {
        Sentence p = new Symbol("p");
        Sentence q = new Symbol("q");
        Sentence r = new Symbol("r");
        i = new Implication(new And(p, q), new Not(r));
    }

    @Test
    void initTest() {
        assertEquals("MyLogic.Implication", i.getClass().getName());
    }

    @Test
    void evaluateTrueTrueTest() {
        HashMap<String,Boolean> model = new HashMap<>();
        model.put("p", true);
        model.put("q", true); // T and T = T
        model.put("r", false);
        assertEquals(true, i.evaluate(model));
    }

    @Test
    void evaluateFalseFalseTest() {
        HashMap<String,Boolean> model = new HashMap<>();
        model.put("p", false);
        model.put("q", true); // F and T = F
        model.put("r", true);
        assertEquals(true, i.evaluate(model));
    }

    @Test
    void evaluateTrueFalseTest() {
        HashMap<String,Boolean> model = new HashMap<>();
        model.put("p", true);
        model.put("q", true);
        model.put("r", true);
        assertEquals(false, i.evaluate(model));
    }

    @Test
    void evaluateFalseTrueTest() {
        HashMap<String,Boolean> model = new HashMap<>();
        model.put("p", false);
        model.put("q", false); // F and F = F
        model.put("r", false);
        assertEquals(true, i.evaluate(model));
    }

    @Test
    void formulaTest() {
        assertEquals("(p ^ q)->(~r)", i.formula());
    }

    @Test
    void symbolsTest() {
        assertEquals("[p, q, r]", i.symbols().toString());
    }

}