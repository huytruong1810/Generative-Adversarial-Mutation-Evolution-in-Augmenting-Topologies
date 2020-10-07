import MyLogic.Sentence.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class SymbolTest {

    Sentence s;

    @BeforeEach
    void init() {
        s = new Symbol("Test");
    }

    @Test
    void initTest() {
        assertEquals("MyLogic.Symbol", s.getClass().getName());
    }

    @Test
    void evaluateTrueTest() {
        HashMap<String,Boolean> model = new HashMap<>();
        model.put("Test", true);
        assertEquals(true, s.evaluate(model));
    }

    @Test
    void evaluateFalseTest() {
        HashMap<String,Boolean> model = new HashMap<>();
        model.put("Test", false);
        assertEquals(false, s.evaluate(model));
    }

    @Test
    void formulaTest() {
        assertEquals("Test", s.formula());
    }

    @Test
    void symbolsTest() {
        assertEquals("[Test]", s.symbols().toString());
    }

}
