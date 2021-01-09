import MyLogic.Sentence.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class OrTest {

    Sentence o;

    @BeforeEach
    void init() {
        Sentence p = new Symbol("p");
        Sentence q = new Symbol("q");
        Sentence r = new Symbol("r");
        o = new Or(p, q, new Not(r));
    }

    @Test
    void initTest() {
        assertEquals("MyLogic.Or", o.getClass().getName());
    }

    @Test
    void evaluateAllTrueTest() {
        HashMap<String,Boolean> model = new HashMap<>();
        model.put("p", true);
        model.put("q", true);
        model.put("r", false);
        assertEquals(true, o.evaluate(model));
    }

    @Test
    void evaluateAllFalseTest() {
        HashMap<String,Boolean> model = new HashMap<>();
        model.put("p", false);
        model.put("q", false);
        model.put("r", true);
        assertEquals(false, o.evaluate(model));
    }

    @Test
    void evaluateOneTrueTest() {
        HashMap<String,Boolean> model = new HashMap<>();
        model.put("p", true);
        model.put("q", false);
        model.put("r", true);
        assertEquals(true, o.evaluate(model));
    }

    @Test
    void evaluateOneFalseTest() {
        HashMap<String,Boolean> model = new HashMap<>();
        model.put("p", false);
        model.put("q", true);
        model.put("r", false);
        assertEquals(true, o.evaluate(model));
    }

    @Test
    void formulaTest() {
        assertEquals("p V q V (~r)", o.formula());
    }

    @Test
    void symbolsTest() {
        assertEquals("[p, q, r]", o.symbols().toString());
    }

}
