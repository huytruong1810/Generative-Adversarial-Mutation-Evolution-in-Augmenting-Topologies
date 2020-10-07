import MyLogic.Sentence.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class AndTest {

    Sentence a;

    @BeforeEach
    void init() {
        Sentence p = new Symbol("p");
        Sentence q = new Symbol("q");
        Sentence r = new Symbol("r");
        a = new And(p, q, new Not(r));
    }

    @Test
    void initTest() {
        assertEquals("MyLogic.And", a.getClass().getName());
    }

    @Test
    void evaluateAllTrueTest() {
        HashMap<String,Boolean> model = new HashMap<>();
        model.put("p", true);
        model.put("q", true);
        model.put("r", false);
        assertEquals(true, a.evaluate(model));
    }

    @Test
    void evaluateAllFalseTest() {
        HashMap<String,Boolean> model = new HashMap<>();
        model.put("p", false);
        model.put("q", false);
        model.put("r", true);
        assertEquals(false, a.evaluate(model));
    }

    @Test
    void evaluateOneTrueTest() {
        HashMap<String,Boolean> model = new HashMap<>();
        model.put("p", true);
        model.put("q", false);
        model.put("r", true);
        assertEquals(false, a.evaluate(model));
    }

    @Test
    void evaluateOneFalseTest() {
        HashMap<String,Boolean> model = new HashMap<>();
        model.put("p", false);
        model.put("q", true);
        model.put("r", false);
        assertEquals(false, a.evaluate(model));
    }

    @Test
    void formulaTest() {
        assertEquals("p ^ q ^ (~r)", a.formula());
    }

    @Test
    void symbolsTest() {
        assertEquals("[p, q, r]", a.symbols().toString());
    }

    @Test
    void addTest() throws Exception {
        a.add(new Or(new Symbol("m"), new Symbol("n")));
        assertEquals("p ^ q ^ (~r) ^ (m V n)", a.formula());
    }

}
