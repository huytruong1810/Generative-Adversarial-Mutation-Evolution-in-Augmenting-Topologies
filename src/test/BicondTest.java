import MyLogic.Sentence.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class BicondTest {

    Sentence b;

    @BeforeEach
    void init() {
        Sentence p = new Symbol("p");
        Sentence q = new Symbol("q");
        Sentence r = new Symbol("r");
        b = new Biconditional(new Or(p, q), new Not(r));
    }

    @Test
    void initTest() {
        assertEquals("MyLogic.Biconditional", b.getClass().getName());
    }

    @Test
    void evaluateTrueTrueTest() {
        HashMap<String,Boolean> model = new HashMap<>();
        model.put("p", true);
        model.put("q", false); // T or F = T
        model.put("r", false);
        assertEquals(true, b.evaluate(model));
    }

    @Test
    void evaluateFalseFalseTest() {
        HashMap<String,Boolean> model = new HashMap<>();
        model.put("p", false);
        model.put("q", false); // F or F = F
        model.put("r", true);
        assertEquals(true, b.evaluate(model));
    }

    @Test
    void evaluateTrueFalseTest() {
        HashMap<String,Boolean> model = new HashMap<>();
        model.put("p", true);
        model.put("q", true); // T or T = T
        model.put("r", true);
        assertEquals(false, b.evaluate(model));
    }

    @Test
    void evaluateFalseTrueTest() {
        HashMap<String,Boolean> model = new HashMap<>();
        model.put("p", false);
        model.put("q", false);
        model.put("r", false);
        assertEquals(false, b.evaluate(model));
    }

    @Test
    void formulaTest() {
        assertEquals("(p V q)<->(~r)", b.formula());
    }

    @Test
    void symbolsTest() {
        assertEquals("[p, q, r]", b.symbols().toString());
    }

}