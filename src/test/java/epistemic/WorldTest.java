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
        assertEquals(testedWorld.hashCode(), clone.hashCode());

        // Worlds should not equal after modifying a value in the clone
        clone.putLiteral(CHARLIE_KEY, CHARLIE_AA_VALUE.getCleanedLiteral());
        clone.putLiteral(BOB_KEY, BOB_AA_VALUE.getCleanedLiteral());
        assertNotEquals(testedWorld, clone);
        assertNotEquals(testedWorld.hashCode(), clone.hashCode());

        // Worlds should be equal again after setting values back to originals
        // This is a true test to make sure the cloning process fails to create clones of the inner objects
        clone.putLiteral(CHARLIE_KEY, CHARLIE_A8_VALUE.getCleanedLiteral());
        clone.putLiteral(BOB_KEY, BOB_A8_VALUE.getCleanedLiteral());
        assertEquals(testedWorld, clone);
        assertEquals(testedWorld.hashCode(), clone.hashCode());
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

}