package epistemic;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.TestUtils;
import epistemic.wrappers.WrappedLiteral;

import static org.junit.jupiter.api.Assertions.*;
import static utils.TestUtils.createHandWithValue;

public class WorldTest {

    private World testedWorld;
    private static final WrappedLiteral ALICE_KEY = TestUtils.createHandWithVariable("Alice");
    private static final WrappedLiteral ALICE_AA_VALUE = createHandWithValue("Alice", "AA");
    private static final WrappedLiteral ALICE_A8_VALUE = createHandWithValue("Alice", "A8");

    private static final WrappedLiteral BOB_KEY = TestUtils.createHandWithVariable("Bob");
    private static final WrappedLiteral BOB_AA_VALUE = createHandWithValue("Bob", "AA");
    private static final WrappedLiteral BOB_A8_VALUE = createHandWithValue("Bob", "A8");

    private static final WrappedLiteral CHARLIE_KEY = TestUtils.createHandWithVariable("Charlie");
    private static final WrappedLiteral CHARLIE_AA_VALUE = createHandWithValue("Charlie", "AA");
    private static final WrappedLiteral CHARLIE_A8_VALUE = createHandWithValue("Charlie", "A8");


    @BeforeEach
    public void setUp() throws Exception {
        testedWorld = new World();

        assertEquals(testedWorld.size(), 0, "Constructor should create empty world");

        testedWorld.putLiteral(ALICE_KEY, ALICE_AA_VALUE.getCleanedLiteral());
        testedWorld.putLiteral(BOB_KEY, BOB_A8_VALUE.getCleanedLiteral());
        testedWorld.putLiteral(CHARLIE_KEY, CHARLIE_A8_VALUE.getCleanedLiteral());


    }

    @Test
    public void testPutLiteral()
    {
        assertEquals(testedWorld.get(ALICE_KEY).getValueLiteral(), ALICE_AA_VALUE.getCleanedLiteral());
        assertEquals(testedWorld.get(BOB_KEY).getValueLiteral(), BOB_A8_VALUE.getCleanedLiteral());
        assertEquals(testedWorld.get(CHARLIE_KEY).getValueLiteral(), CHARLIE_A8_VALUE.getCleanedLiteral());
    }

    @Test
    public void testPutLiteralOverwrite() {
        var originalValue = testedWorld.get(ALICE_KEY);
        testedWorld.putLiteral(ALICE_KEY, ALICE_A8_VALUE.getCleanedLiteral());
        assertNotEquals(testedWorld.get(ALICE_KEY), originalValue);
    }

    @Test
    public void testPutLiteralValueNotUnifyKey() {
        // Try to place a value that doesn't unify the key.
        assertThrows(RuntimeException.class, () -> testedWorld.putLiteral(ALICE_KEY, BOB_AA_VALUE.getCleanedLiteral()));
    }

    @Test
    public void testClone() {
        World clone = testedWorld.clone();
        assertEquals(testedWorld, clone);

        // Worlds should not equal after modifying a value in the clone
        clone.putLiteral(ALICE_KEY, ALICE_A8_VALUE.getCleanedLiteral());
        assertNotEquals(testedWorld, clone);

    }

    @Test
    public void evaluate() {
        assertTrue(testedWorld.evaluate(ALICE_AA_VALUE.getCleanedLiteral()), "should evaluate to true");
        assertFalse(testedWorld.evaluate(ALICE_A8_VALUE.getCleanedLiteral()), "evaluate false when not true in world");
        assertFalse(testedWorld.evaluate(ALICE_KEY.getCleanedLiteral()), "key should not evaluate to true");
        assertFalse(testedWorld.evaluate(null), "null literal should eval to false");

        var fakeLiteral = createHandWithValue("Alice", "fake");
        assertFalse(testedWorld.evaluate(fakeLiteral.getCleanedLiteral()), "should not evaluate true on a literal that is similar to other literals, but not a value");


        testedWorld.putLiteral(ALICE_KEY, ALICE_A8_VALUE.getCleanedLiteral());

        assertTrue(testedWorld.evaluate(ALICE_A8_VALUE.getCleanedLiteral()), "should evaluate to true");
        assertFalse(testedWorld.evaluate(ALICE_AA_VALUE.getCleanedLiteral()), "evaluate false when not true in world");
        assertFalse(testedWorld.evaluate(ALICE_KEY.getCleanedLiteral()), "key should not evaluate to true");
        assertFalse(testedWorld.evaluate(null), "null literal should eval to false");
    }

    @Test
    public void createAccessibility() {
        assertTrue(testedWorld.getAccessibleWorlds().isEmpty(), "createAccessibility is not implemented");
    }

    @Test
    public void getAccessibleWorlds() {
        assertTrue(testedWorld.getAccessibleWorlds().isEmpty(), "getAccessibleWorlds is not implemented");
    }

    @Test
    public void putLiteral() {
    }

    @Test
    public void putProposition() {
    }

    @Test
    public void testClone1() {
    }

    @Test
    public void toLiteral() {
    }

    @Test
    public void wrappedValueSet() {
    }

    @Test
    public void valueSet() {
    }

    @Test
    public void keySet() {
    }

    @Test
    public void testToString() {
    }

    @Test
    public void testEvaluate() {
    }

    @Test
    public void testCreateAccessibility() {
    }

    @Test
    public void getUniqueName() {
    }

    @Test
    public void testGetAccessibleWorlds() {
    }

    @Test
    public void containsKey() {
    }

    @Test
    public void size() {
    }

    @Test
    public void get() {
    }

    @Test
    public void putAll() {
    }

    @Test
    public void testHashCode() {
    }

    @Test
    public void testEquals() {
    }
}