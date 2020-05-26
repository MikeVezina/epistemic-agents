package epistemic;

import org.junit.Before;
import org.junit.Test;
import utils.TestUtils;
import epistemic.wrappers.WrappedLiteral;

import static org.junit.Assert.*;
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


    @Before
    public void setUp() throws Exception {
        testedWorld = new World();

        assertEquals("Constructor should create empty world", testedWorld.size(), 0);

        testedWorld.putLiteral(ALICE_KEY, ALICE_AA_VALUE.getOriginalLiteral());
        testedWorld.putLiteral(BOB_KEY, BOB_A8_VALUE.getOriginalLiteral());
        testedWorld.putLiteral(CHARLIE_KEY, CHARLIE_A8_VALUE.getOriginalLiteral());


    }

    @Test
    public void testPutLiteral()
    {
        assertEquals(testedWorld.get(ALICE_KEY).getValueLiteral(), ALICE_AA_VALUE.getOriginalLiteral());
        assertEquals(testedWorld.get(BOB_KEY).getValueLiteral(), BOB_A8_VALUE.getOriginalLiteral());
        assertEquals(testedWorld.get(CHARLIE_KEY).getValueLiteral(), CHARLIE_A8_VALUE.getOriginalLiteral());
    }

    @Test
    public void testPutLiteralOverwrite() {
        var originalValue = testedWorld.get(ALICE_KEY);
        testedWorld.putLiteral(ALICE_KEY, ALICE_A8_VALUE.getOriginalLiteral());
        assertNotEquals(testedWorld.get(ALICE_KEY), originalValue);
    }

    @Test(expected = RuntimeException.class)
    public void testPutLiteralValueNotUnifyKey() {
        // Try to place a value that doesn't unify the key.
        testedWorld.putLiteral(ALICE_KEY, BOB_AA_VALUE.getOriginalLiteral());
    }

    @Test
    public void testClone() {
        World clone = testedWorld.clone();
        assertEquals(testedWorld, clone);

        // Worlds should not equal after modifying a value in the clone
        clone.putLiteral(ALICE_KEY, ALICE_A8_VALUE.getOriginalLiteral());
        assertNotEquals(testedWorld, clone);

    }

    @Test
    public void evaluate() {
        assertTrue("should evaluate to true", testedWorld.evaluate(ALICE_AA_VALUE.getOriginalLiteral()));
        assertFalse("evaluate false when not true in world", testedWorld.evaluate(ALICE_A8_VALUE.getOriginalLiteral()));
        assertFalse("key should not evaluate to true", testedWorld.evaluate(ALICE_KEY.getOriginalLiteral()));
        assertFalse("null literal should eval to false", testedWorld.evaluate(null));

        var fakeLiteral = createHandWithValue("Alice", "fake");
        assertFalse("should not evaluate true on a literal that is similar to other literals, but not a value", testedWorld.evaluate(fakeLiteral.getOriginalLiteral()));


        testedWorld.putLiteral(ALICE_KEY, ALICE_A8_VALUE.getOriginalLiteral());

        assertTrue("should evaluate to true", testedWorld.evaluate(ALICE_A8_VALUE.getOriginalLiteral()));
        assertFalse("evaluate false when not true in world", testedWorld.evaluate(ALICE_AA_VALUE.getOriginalLiteral()));
        assertFalse("key should not evaluate to true", testedWorld.evaluate(ALICE_KEY.getOriginalLiteral()));
        assertFalse("null literal should eval to false", testedWorld.evaluate(null));
    }

    @Test
    public void createAccessibility() {
        assertTrue("createAccessibility is not implemented", testedWorld.getAccessibleWorlds().isEmpty());
    }

    @Test
    public void getAccessibleWorlds() {
        assertTrue("getAccessibleWorlds is not implemented", testedWorld.getAccessibleWorlds().isEmpty());
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