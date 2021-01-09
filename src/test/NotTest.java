import MyLogic.Sentence.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class NotTest {

    Sentence n;

    @BeforeEach
    void init() {
        n = new Not(new Symbol("Test"));
    }

    @Test
    void initTest() {
        assertEquals("MyLogic.Not", n.getClass().getName());
    }

    @Test
    void evaluateTrueTest() {
        HashMap<String,Boolean> model = new HashMap<>();
        model.put("Test", true);
        assertEquals(false, n.evaluate(model));
    }

    @Test
    void evaluateFalseTest() {
        HashMap<String,Boolean> model = new HashMap<>();
        model.put("Test", false);
        assertEquals(true, n.evaluate(model));
    }

    @Test
    void formulaTest() {
        assertEquals("~Test", n.formula());
    }

    @Test
    void symbolsTest() {
        assertEquals("[Test]", n.symbols().toString());
    }

}
